package ca.ehealth.ontario.olis_fhir_prototype.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.dstu3.model.Quantity;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.SimpleQuantity;
import org.hl7.fhir.exceptions.FHIRException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * This model represents a single DiagnosticReport resource returned by OLIS.
 * Implements parcelable so that it can be moved between activities.
 */
public class OLISDiagnosticReportModel implements Parcelable
{
    private String practitionerName;
    private String organizationName;
    private String testPerformed;
    private String testResult;
    private String acceptableRange;
    private String testReleaseDate;

    public OLISDiagnosticReportModel()
    {
        // use defaults
    }

    /**
     *  simple constructor for manually creating a DiagnosticReport
     */
    public OLISDiagnosticReportModel(String practitionerName, String organizationName, String testPerformed, String testResult, String acceptableRange, String testReleaseDate)
    {
        setPractitionerName(practitionerName);
        setOrganizationName(organizationName);
        setTestPerformed(testPerformed);
        setTestResult(testResult);
        setAcceptableRange(acceptableRange);
        setTestReleaseDate(testReleaseDate);
    }

    /**
     * This constructor takes in a given DiagnosticReport resource and extracts the data with mapper methods
     */
    public OLISDiagnosticReportModel(DiagnosticReport diagnosticReportResult)
    {
        /* *******************************************************************************************
         *  DiagnosticReport - Loop through contained resources
         * *******************************************************************************************/
        if (diagnosticReportResult.getContained() != null && !diagnosticReportResult.getContained().isEmpty())
        {
            for (Resource containedResource : diagnosticReportResult.getContained())
            {
                /* *******************************************************************************************
                 *  The Practitioner resource
                 * *******************************************************************************************/
                if (containedResource instanceof Practitioner)
                {
                    mapPractitioner((Practitioner) containedResource);
                }

                /* *******************************************************************************************
                 *  The Organization resource
                 * *******************************************************************************************/
                else if (containedResource instanceof Organization)
                {
                    mapOrganization((Organization) containedResource);
                }

                /* *******************************************************************************************
                 *  The ProcedureRequest resource
                 * *******************************************************************************************/
                else if (containedResource instanceof ProcedureRequest)
                {
                    mapProcedureRequest((ProcedureRequest) containedResource);
                }

                /* *******************************************************************************************
                 *  The Observation resource
                 * *******************************************************************************************/
                else if (containedResource instanceof Observation)
                {
                    mapObservation((Observation) containedResource);
                }
            }
        }
    }

    /* *******************************************************************************************
     * Resource mappings
     * *******************************************************************************************/
    private void mapPractitioner(Practitioner practitioner)
    {
        // Extract practitionerName name
        // OLIS FHIR Specification: practioner.name[x].given[x], practioner.name[x].family
        if (practitioner.getName() != null
                && !practitioner.getName().isEmpty()
                && practitioner.getName().get(0).getGiven() != null
                && !practitioner.getName().get(0).getGiven().isEmpty()
                && practitioner.getName().get(0).getFamily() != null)

        {
            HumanName name = practitioner.getName().get(0);
            setPractitionerName(name.getGiven().get(0) + " " + name.getFamily());
        }
    }

    private void mapOrganization(Organization organization)
    {
        // Extract organizationName name
        // OLIS FHIR Specification: organizationName.name
        if (organization.getName() != null )
        {
            setOrganizationName(organization.getName()); ;
        }
    }

    private void mapProcedureRequest(ProcedureRequest procedureRequest)
    {

        // Extract test performed
        // OLIS FHIR Specification: procedure.code.coding[x].code, procedure.code.coding[x].display
        if (procedureRequest.getCode() != null
                && procedureRequest.getCode().getCoding() != null
                && !procedureRequest.getCode().getCoding().get(0).isEmpty()
                && procedureRequest.getCode().getCoding().get(0).getCode() != null
                && procedureRequest.getCode().getCoding().get(0).getDisplay() != null)
        {
            Coding testCoding = procedureRequest.getCode().getCoding().get(0);
            setTestPerformed(testCoding.getDisplay() + " (" + testCoding.getCode() + ")");
        }
    }

    private void mapObservation(Observation observation)
    {
        // Extract acceptable quantity range
        // OLIS FHIR Specification: observation.referenceRange[x].low.value, observation.referenceRange[x].low.unit
        //                          observation.referenceRange[x].high.value, observation.referenceRange[x].high.unit
        if (observation.getReferenceRange() != null
                && !observation.getReferenceRange().isEmpty()
                && observation.getReferenceRange().get(0).getLow() !=null
                && observation.getReferenceRange().get(0).getHigh() !=null)
        {
            SimpleQuantity lowObject = observation.getReferenceRange().get(0).getLow();
            SimpleQuantity highObject = observation.getReferenceRange().get(0).getHigh();
            setAcceptableRange(lowObject.getValue() + " " + lowObject.getUnit() + " -- " + highObject.getValue() + " " + highObject.getUnit());
        }

        try
        {
            // Extract test testResult
            // OLIS FHIR Specification: observation.valueQuantity.value, observation.interpretation.coding[x].display
            //                          observation.valueQuantity.unit
            if (observation.getValueQuantity() != null
                    && observation.getValueQuantity().getValue() != null
                    && observation.getValueQuantity().getUnit() != null
                    && observation.getInterpretation() != null
                    && observation.getInterpretation().getCoding() !=null
                    && !observation.getInterpretation().getCoding().isEmpty()
                    && observation.getInterpretation().getCoding().get(0).getDisplay() != null)
            {
                Quantity valueQuantity = observation.getValueQuantity();
                String resultInterpretation = observation.getInterpretation().getCoding().get(0).getDisplay();
                setTestResult(valueQuantity.getValue() + " " + valueQuantity.getUnit() + " (" + resultInterpretation + ")");
            }

            if (observation.getIssued() != null)
            {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(observation.getIssued());
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyy", Locale.CANADA);

                setTestReleaseDate(dateFormat.format(observation.getIssued().getTime()));
            }
        }
        catch (FHIRException e)
        {
            // Only using a try-catch because we're forced to
            e.printStackTrace();
        }

    }

    /* *******************************************************************************************
     *  Getters
     * *******************************************************************************************/
    public String getPractitionerName()
    {
        return practitionerName;
    }
    public String getOrganizationName()
    {
        return organizationName;
    }
    public String getTestPerformed()
    {
        return testPerformed;
    }
    public String getTestResult()
    {
        return testResult;
    }
    public String getAcceptableRange()
    {
        return acceptableRange;
    }
    public String getTestReleaseDate()
    {
        return testReleaseDate;
    }

    /* *******************************************************************************************
     *  Setters
     * *******************************************************************************************/
    public void setPractitionerName(String practitionerName)
    {
        this.practitionerName = practitionerName;
    }
    public void setOrganizationName(String organizationName)
    {
        this.organizationName = organizationName;
    }
    public void setTestPerformed(String testPerformed)
    {
        this.testPerformed = testPerformed;
    }
    public void setTestResult(String testResult)
    {
        this.testResult = testResult;
    }
    public void setAcceptableRange(String acceptableRange)
    {
        this.acceptableRange = acceptableRange;
    }
    public void setTestReleaseDate(String testReleaseDate)
    {
        this.testReleaseDate = testReleaseDate;
    }

    /* *******************************************************************************************
     *  Parcelable stuff goes under here
     * *******************************************************************************************/
    /**
     * Constructor when a parcel object is being given for the Parcelable implementation.
     * the in.XXXX() statements must occur in the same order as the writeXXXX() statements in writeToParcel().
     * @param in the parcel coming in
     *
     */
    private OLISDiagnosticReportModel(Parcel in)
    {
        setPractitionerName(in.readString());
        setOrganizationName(in.readString());
        setTestReleaseDate(in.readString());
        setTestPerformed(in.readString());
        setAcceptableRange(in.readString());
        setTestResult(in.readString());
    }

    /**
     * Auto generated method stub (with added getters)
     * @param dest
     * @param flags
     */
    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(getPractitionerName());
        dest.writeString(getOrganizationName());
        dest.writeString(getTestReleaseDate());
        dest.writeString(getTestPerformed());
        dest.writeString(getAcceptableRange());
        dest.writeString(getTestResult());
    }

    /**
     * Auto generated method stub required by parcelable
     */
    public static final Creator<OLISDiagnosticReportModel> CREATOR = new Creator<OLISDiagnosticReportModel>()
    {
        @Override
        public OLISDiagnosticReportModel createFromParcel(Parcel in)
        {
            return new OLISDiagnosticReportModel(in);
        }

        @Override
        public OLISDiagnosticReportModel[] newArray(int size)
        {
            return new OLISDiagnosticReportModel[size];
        }
    };

    /**
     * Auto generated method stub
     * @return
     */
    @Override
    public int describeContents()
    {
        return 0;
    }
}
