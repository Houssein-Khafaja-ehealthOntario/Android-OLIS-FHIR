package ca.ehealth.ontario.olis_fhir_prototype.services;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.hl7.fhir.dstu3.model.Bundle;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import ca.ehealth.ontario.olis_fhir_prototype.activities.PCRListActivity;
import ca.ehealth.ontario.olis_fhir_prototype.customdialogs.ProgressCircleDialog;
import ca.ehealth.ontario.olis_fhir_prototype.models.PCRPatientModel;

/**
 * This class handles our async task to retrieve the data from the PCR web service
 */
public class PCRAsyncTask extends AsyncTask<String, Void, ArrayList<PCRPatientModel>>
{
    private LocalSQLOpenHelper sqLiteOpenHelper;
    private ProgressCircleDialog progressCircleDialog;

    // We want to keep a WeakReference to the activity context first, and then when we need it we check to see if it is still valid.
    // This is done to prevent memory leaks which would be caused be using something like: private Context myContext;
    private final WeakReference<Activity> weakReference;

    public PCRAsyncTask(Activity inActivity)
    {
        this.weakReference = new WeakReference<>(inActivity);
        sqLiteOpenHelper = new LocalSQLOpenHelper(inActivity.getApplicationContext());
        progressCircleDialog = new ProgressCircleDialog(inActivity);
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();

        progressCircleDialog.show();
    }

    protected ArrayList<PCRPatientModel> doInBackground(String... strings)
    {
        // initialize the local DB, the results array and save the userRole value
        SQLiteDatabase db = sqLiteOpenHelper.getReadableDatabase();
        ArrayList<PCRPatientModel> results = new ArrayList<>(); // patients received from PCR to be returned by this method

        // lets loop through the database with a cursor, which requires a projection (our columns)
        String[] projection = {"_id, name, hcn"};
        Cursor cursor = db.query("patients", projection, null, null, null, null, null);

        while (cursor.moveToNext())
        {
            String healthCardNumber = cursor.getString(cursor.getColumnIndex("hcn"));

            // query PCR and parse xml data
            HttpResponse pcrHttpResponse = PCRService.executeQuery(healthCardNumber);
            PCRPatientModel pcrPatient = PCRService.parseHttpResponse(pcrHttpResponse);
            pcrPatient.setHealthCardNumber(healthCardNumber);

            // query OLIS for lab totals
            OLISService olisService = new OLISService();
            Bundle olisResults = olisService.executeQuery(pcrPatient.getHealthCardNumber(), pcrPatient.getDateOfBirthForQuery(), pcrPatient.getGender());
            pcrPatient.setLabTotal(olisResults.getTotal());

            results.add(pcrPatient);
        }

        cursor.close();

        // return the result bundle for onPostExecute
        return results;
    }

    /**
     * This method takes the result and sends it to the ResultFragment.
     * Replaces RequestFragment with ResultFragment.
     * @param results this was retrieved in doInBackround()
     */
    protected void onPostExecute(ArrayList<PCRPatientModel> results)
    {
        Activity activity = weakReference.get();

        // use weak reference to get a strong reference
        //if its no longer valid, then end this task
        if (activity == null || activity.isFinishing() || activity.isDestroyed())
        {
            // activity is no longer valid, don't do anything!
            return;
        }

        Intent pcrListIntent = new Intent(activity, PCRListActivity.class);
        pcrListIntent.putExtra("patients", results);
        activity.startActivity(pcrListIntent);

        if (progressCircleDialog.isShowing())
        {
            // no longer loading, close progress circle
            progressCircleDialog.dismiss();
        }
    }
}