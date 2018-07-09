package ca.ehealth.ontario.olis_fhir_prototype.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import ca.ehealth.ontario.olis_fhir_prototype.R;
import ca.ehealth.ontario.olis_fhir_prototype.models.OLISDiagnosticReportModel;

/**
 * A simple class that takes in a OLISDiagnosticReportModel object and inserts the containing data into the
 * Lab Report Details view.
 */
public class DiagnosticReportDetailsActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnostic_report_details);

        // get diagnostic report object that was sent from PatientSummaryActivity
        OLISDiagnosticReportModel labReport = getIntent().getParcelableExtra("olisReport");

        //get the text views for medication dispense details
        TextView practitionerTextView = findViewById(R.id.practitionerItem);
        TextView organizationTextView = findViewById(R.id.organizationItem);
        TextView testTextView = findViewById(R.id.testItem);
        TextView acceptableRangeTextView = findViewById(R.id.acceptableRangeItem);
        TextView dateTextView = findViewById(R.id.dateItem);
        TextView resultTextView = findViewById(R.id.resultItem);

        // set data to text views
        practitionerTextView.setText(labReport.getPractitionerName());
        organizationTextView.setText(labReport.getOrganizationName());
        testTextView.setText(labReport.getTestPerformed());
        acceptableRangeTextView.setText(labReport.getAcceptableRange());
        dateTextView.setText(labReport.getTestReleaseDate());
        resultTextView.setText(labReport.getTestResult());

    }
}
