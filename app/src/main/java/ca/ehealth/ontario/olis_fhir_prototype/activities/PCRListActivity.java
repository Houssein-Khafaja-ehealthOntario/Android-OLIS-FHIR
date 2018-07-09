package ca.ehealth.ontario.olis_fhir_prototype.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import java.util.ArrayList;

import ca.ehealth.ontario.olis_fhir_prototype.R;
import ca.ehealth.ontario.olis_fhir_prototype.adapters.PCRListAdapter;
import ca.ehealth.ontario.olis_fhir_prototype.models.PCRPatientModel;

/**
 * This activity will display a list of patients that are meant to be in an ER admissions list.
 *
 * An ArrayList of PCR patients will be fed into a custom adapter which handles how the
 * ListView will display the data.
 */
public class PCRListActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pcr_list);

        // get the patients sent from the async task
        ArrayList<PCRPatientModel> patients = getIntent().getExtras().getParcelableArrayList("patients");

        // initialise listview and data array
        ListView patientListView = findViewById(R.id.patients_list);

        // attach data to a the new adapter and then attach adapter to the ListView
        PCRListAdapter adapter = new PCRListAdapter(PCRListActivity.this, patients, patientListView);
        patientListView.setAdapter(adapter);
    }
}
