package ca.ehealth.ontario.olis_fhir_prototype.services;

import android.util.Log;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.DiagnosticReport;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.client.interceptor.AdditionalRequestHeadersInterceptor;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class OLISService
{
    private final String endPointBase = "http://lite.innovation-lab.ca:9443/consumer/v1";
    private final String diagnosticReportSearchURL = "DiagnosticReport?patient.identifier=https://fhir.infoway-inforoute.ca/NamingSystem/ca-on-patient-hcn|";
    private final String senderId = "your unique identifier";
    static private final FhirContext fhirContext = FhirContext.forDstu3();
    private IGenericClient client;

    /**
     * Initializes FHIR client,
     * Generates pin for Immunization_Context, sets required HTTP headers and registers headers to client
     * ****SENDER ID MUST BE REPLACED WITH YOUR UNIQUE SENDER ID, FOUND AT https://www.innovation-lab.ca/Test-Portal****
     */
    OLISService()
    {
        client = fhirContext.newRestfulGenericClient(endPointBase);

        // The HAPI FHIR library sends an initial metadata query for validation any time a client preforms a query.
        // In order to query this must be disabled on the client:
        fhirContext.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);

        String jwt = "";

        // generate a jwt token for our request header
        // jwt properties are usually provided by ONE ID or another source,
        // for sake of simplicity we will be hard coding known values
        try
        {
            jwt = Jwts.builder()
                    .claim("jti", "4165641290")
                    .claim("organization", "Mohawk MEDIC")
                    .claim("application", "Test Harness")
                    .claim("app_version", "V0.0.0.10")
                    .claim("uid", senderId)
                    .claim("idp", "ONE ID")
                    .claim("username", "Innovation Lab")
                    .claim("usertype", "P")
                    .claim("registrationorganization", "Innovation Lab")
                    .signWith(
                            SignatureAlgorithm.HS256,
                            "secret".getBytes("UTF-8")
                    )
                    .compact();
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        AdditionalRequestHeadersInterceptor headers = new AdditionalRequestHeadersInterceptor();
        headers.addHeaderValue("X-Sender-Id", senderId);
        headers.addHeaderValue("X-License-Text", "I hereby accept the service agreement here: https://innovation-lab.ca/media/1147/innovation-lab-terms-of-use.pdf");
        headers.addHeaderValue("ClientTxID", UUID.randomUUID().toString());
        headers.addHeaderValue("jwt-token", jwt);


        client.registerInterceptor(headers);
    }

    /**
     * Performs a GET operation, querying the OLIS server by a single HCN, birthdate, gender, and specimenCollectionDate.
     *
     * @param healthCardNumber
     * @return returns the query results
     */
    public Bundle executeQuery(String healthCardNumber, String birthDate, String gender, String specimenCollectionStartDate, String specimenCollectionEndDate)
    {
        return client.search()
                .byUrl(buildQueryUrl(healthCardNumber, birthDate, gender, specimenCollectionStartDate, specimenCollectionEndDate))
                .returnBundle(Bundle.class)
                .execute();
    }

    /**
     * Performs a GET operation, querying the OLIS server by a single HCN, birthdate, and gender.
     * SpecimenCollectionDate is set to 30 days prior to today.
     *
     * @param healthCardNumber
     * @return returns the query results
     */
    public Bundle executeQuery(String healthCardNumber, String birthDate, String gender)
    {
        return client.search()
                .byUrl(buildQueryUrl(healthCardNumber, birthDate, gender, null, null))
                .returnBundle(Bundle.class)
                .execute();
    }

    /**
     * This method will take in query parameters and build a query string for the .search().byUrl() method.
     */
    private String buildQueryUrl(String healthCardNumber, String birthDate, String gender, String specimenCollectionStartDate, String specimenCollectionEndDate)
    {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(diagnosticReportSearchURL).append(healthCardNumber)
        .append("&patient.birthdate=").append(birthDate).append("&patient.gender=")
        .append(gender);

        if (specimenCollectionStartDate != null && !"".equals(specimenCollectionStartDate))
        {
            stringBuilder.append("&specimen.collected=ge").append(specimenCollectionStartDate);
        }
        // if no start date is given, then we will not use a default date because start dates would need to go too far back (in the 90s).
        // Instead, use the birthdate since that will always give us some records
        else
        {
            stringBuilder.append("&specimen.collected=ge").append(birthDate);
        }

        if (specimenCollectionEndDate != null && !"".equals(specimenCollectionEndDate))
        {
            stringBuilder.append("&specimen.collected=le").append(specimenCollectionEndDate);
        }

        return stringBuilder.toString();
    }

    /**
     * This method returns today's date minus the number of days supplied as a parameter.
     * The date is generated using the Calendar and SimpleDateFormat objects.
     *
     * @param dayCount the number of days to count backwards from today.
     * @return a date string in the format: 2018-02-24(yyyy-MM-dd)
     */
    private String getDate(int dayCount)
    {
        //date to return
        String myDate;

        // get today's date as a calendar object
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        // get today's date minus 120 days
        calendar.add(Calendar.DAY_OF_MONTH, dayCount);

        // get the date format: e.g. 2018-02-24
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CANADA);

        // apply date format with new calendar and set as the start query date
        myDate = dateFormat.format(calendar.getTime());

        return myDate;
    }

    /**
     * Example of how to convert a FHIR resource into a raw JSON string
     *
     * @param resource resource to parse
     * @return raw JSON representation of Immunization object
     */
    public String parse(DiagnosticReport resource)
    {
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(resource);
    }

    /**
     * Example of how to convert a raw JSON string into a FHIR object
     *
     * @param jsonResource resource to parse
     * @return FHIR object parsed from raw JSON
     */
    public DiagnosticReport parse(String jsonResource)
    {
        return fhirContext.newJsonParser().parseResource(DiagnosticReport.class, jsonResource);
    }

    /**
     * Example of how to convert a dstu3.model.Bundle to a String
     *
     * @param dataBundle a FHIR bundle representing a patient with dispensed medication
     * @return the dataBundle encoded as a String
     */
    static public String BundleToString(Bundle dataBundle)
    {
        return fhirContext.newJsonParser().encodeResourceToString(dataBundle);
    }

    /**
     * Example of how to convert a String to a dstu3.model.Bundle
     *
     * @param dataString the dataBundle encoded as a String
     * @return a FHIR bundle representing a patient with dispensed medication
     */
    static public Bundle StringToBundle(String dataString)
    {
        return (Bundle) fhirContext.newJsonParser().parseResource(dataString);
    }
}
