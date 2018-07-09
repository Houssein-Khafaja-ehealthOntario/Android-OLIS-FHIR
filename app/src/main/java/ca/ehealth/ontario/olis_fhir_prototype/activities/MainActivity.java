package ca.ehealth.ontario.olis_fhir_prototype.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import ca.ehealth.ontario.olis_fhir_prototype.R;
import ca.ehealth.ontario.olis_fhir_prototype.services.PCRAsyncTask;

/**
 * This Activity is for the landing screen of this prototype.
 * Its only purpose is to display buttons and define the onClick logic of those buttons.
 * Currently there is only one button: Clinician View.
 * Each button should lead to a prompt or a list from which a patient can be selected.
 */
public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void showClinicianView(View view)
    {
        PCRAsyncTask pcrAsyncTask = new PCRAsyncTask(this);
        pcrAsyncTask.execute();
    }
}
