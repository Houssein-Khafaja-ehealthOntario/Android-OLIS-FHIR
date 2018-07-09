package ca.ehealth.ontario.olis_fhir_prototype.services;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.OperationOutcome;

import java.lang.ref.WeakReference;

import ca.ehealth.ontario.olis_fhir_prototype.activities.PatientSummaryActivity;
import ca.ehealth.ontario.olis_fhir_prototype.customdialogs.ExceptionErrorDialog;
import ca.ehealth.ontario.olis_fhir_prototype.customdialogs.ProgressCircleDialog;
import ca.ehealth.ontario.olis_fhir_prototype.models.PCRPatientModel;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;

/**
 * The purpose of this class is to run an OLIS query as an asynchronous task (in the background)
 * and return a result to be displayed.
 */
public class OLISAsyncTask extends AsyncTask<String, Void, Bundle>
{
    // We want to keep a WeakReference to the activity context first, and then when we need it we check to see if it is still valid.
    // This is done to prevent memory leaks which would be caused by using something like: private Context myContext;
    private final WeakReference<Activity> weakReference;
    private PCRPatientModel patientToQuery;
    private ProgressCircleDialog progressCircleDialog;
    private ExceptionErrorDialog errorDialog;
    private int exceptionCodeHolder = -69;
    private boolean isUpdatingData = false; // flag indicating whether this query is replacing old data with new data

    /**
     * Simple constructor that saves a weak reference of launching activity and the patient object to be queried.
     * Also does some initializations with custom dialog objects.
     * @param inActivity the activity that started the async task
     * @param patientToQuery the patient to query
     */
    public OLISAsyncTask(Activity inActivity, PCRPatientModel patientToQuery)
    {
        weakReference = new WeakReference<>(inActivity);
        this.patientToQuery = patientToQuery;
        progressCircleDialog = new ProgressCircleDialog(inActivity);
        errorDialog = new ExceptionErrorDialog(inActivity);
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();

        progressCircleDialog.show();
    }

    /**
     * This function will start a new instance of the OLISService and use that to query OLIS.
     * It will use the health card number from the patientToQuery object
     * @param strings an array of string parameters which are passed in with the .execute() function from the async task.
     * @return a FHIR Bundle containing the medication dispenses
     */
    protected Bundle doInBackground(String... strings)
    {
        // OLISService will be used to make the REST call
        OLISService olisService = new OLISService();

        // Our response will come back as a FHIR Bundle
        Bundle olisQueryResults = null;

        // Try to query OLIS
        try
        {
            String healthCardNumber = patientToQuery.getHealthCardNumber();
            String gender = patientToQuery.getGender().toLowerCase();
            String birthDate = patientToQuery.getDateOfBirthForQuery();

            // if we have start & end date query strings
            if(strings.length == 2)
            {
                olisQueryResults = olisService.executeQuery(healthCardNumber, birthDate, gender, strings[0], strings[1]);
                isUpdatingData = true;
            }
            else
            {
                olisQueryResults = olisService.executeQuery(healthCardNumber, birthDate, gender);
            }

            if (olisQueryResults.getEntry() == null /*|| olisQueryResults.getEntry().isEmpty()*/)
            {
                // data was not found
                exceptionCodeHolder = 404;
            }
        }
        // catch the FHIR exceptions and save their codes
        catch (BaseServerResponseException e)
        {
            Log.d("silly", e.toString());
            exceptionCodeHolder = e.getStatusCode();
        }
        catch (Exception e)
        {
            Log.d("silly", e.toString());
            exceptionCodeHolder = 17438; // a random code to indicate a general exception was caught
        }

        // return the result bundle for onPostExecute
        return olisQueryResults;
    }

    /**
     * @param result this was retrieved from doInBackround()
     */
    protected void onPostExecute(Bundle result)
    {
        // Use weak reference of the launching activity to get a strong reference
        //if its no longer valid, then end this task
        Activity activity = weakReference.get();
        if (activity == null || activity.isFinishing() || activity.isDestroyed())
        {
            // activity is no longer valid, don't do anything!
            return;
        }

        // check if we got any exceptions during doInBackground()
        if (exceptionCodeHolder != -69)
        {
            switch (exceptionCodeHolder)
            {
                case 401: // AuthenticationException
                case 403: // ForbiddenOperationException
                    errorDialog.showErrorMessage
                            ("Client Authentication Error! User needs to provide credentials, has provided invalid credentials, or is not permitted to perform the request operation.\nCode: " + exceptionCodeHolder );
                    break;

                case 500:// InternalErrorException
                    errorDialog.showErrorMessage
                            ("Server Error! The server failed to successfully process the request. This generally means that the server is misbehaving or is misconfigured in some way.\nCode: " + exceptionCodeHolder);
                    break;

                case 400: // InvalidRequestException
                case 405: // MethodNotAllowedException
                case 501: // NotImplementedOperationException
                case 422: // UnprocessableEntityException
                    errorDialog.showErrorMessage("Client Error! The client's message was not valid, or the requested method has been disabled/not implemented by the server.\nCode: " + exceptionCodeHolder);
                    break;

                case 410: // ResourceGoneException
                case 404: // ResourceNotFoundException
                    errorDialog.showErrorMessage
                            ("Not found Error! Attempt to locate a resource that has been deleted or did not exist in the first place.\nCode: " + exceptionCodeHolder);
                    break;

                case 0: // FhirClientConnectionException --> SocketTimeOutException
                    errorDialog.showErrorMessage
                            ("Connection Error! Connection timed out. Possible reasons can widely vary. Please try again and ensure you  have a proper internet connection. ");
                    break;

                default:
                    errorDialog.showErrorMessage("Unexpected error! Status code: " + exceptionCodeHolder);
                    break;
            }

            // We had an error if we get here
            // So lets end the task
            return;
        }

        // handling the result in here
        try
        {
            // Our PatientSummaryActivity will show PCR data along with a list of OLIS reports
            Intent patientSummaryIntent = new Intent(activity, PatientSummaryActivity.class);

            // check operation outcome
            if(result.getEntry() instanceof OperationOutcome)
            {
                // get Issues array, check each code
                for(OperationOutcome.OperationOutcomeIssueComponent issue : ((OperationOutcome) result.getEntry()).getIssue())
                {
                    // clear the listview if no results are found
                    if (issue.getCode().equals("not-found"))
                    {
                        if (isUpdatingData)
                        {
                            ((PatientSummaryActivity) activity).setListViewNoResults();
                        }

                        return; // no point in moving forward
                    }
                }
            }

            // if not updating data, then start a new activity with the result
            if(!isUpdatingData)
            {
                // sometimes BundleToString takes awhile to finish, so show the progress bar

                // Our OLIS service has a bundleToString method which makes it easier to move the data to a new activity
                patientSummaryIntent.putExtra("olisReport", OLISService.BundleToString(result));
                patientSummaryIntent.putExtra("patient", patientToQuery); // we need to display the demographics data in the next activity

                activity.startActivity(patientSummaryIntent);
            }
            // since we're updating data, no need to start a new activity. Just call setListViewData to update the data
            else
            {
                ((PatientSummaryActivity) activity).setListViewData(result);
            }
        }
        catch (Exception e)
        {
            Log.d("test", "An unhandled exception got got.");
        }

        if (progressCircleDialog.isShowing())
        {
            // no longer loading, close progress circle dialog
            progressCircleDialog.dismiss();
        }
    }
}
