package ca.ehealth.ontario.olis_fhir_prototype.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.DiagnosticReport;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ca.ehealth.ontario.olis_fhir_prototype.R;
import ca.ehealth.ontario.olis_fhir_prototype.adapters.DiagnosticReportListAdapter;
import ca.ehealth.ontario.olis_fhir_prototype.models.OLISDiagnosticReportModel;
import ca.ehealth.ontario.olis_fhir_prototype.models.PCRPatientModel;
import ca.ehealth.ontario.olis_fhir_prototype.services.OLISAsyncTask;
import ca.ehealth.ontario.olis_fhir_prototype.services.OLISService;

/**
 * This view is responsible for listing a summary for a specific patient and also showing a list of OLIS Diagnostic Reports.
 */
public class PatientSummaryActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener
{
    Activity activity;

    private ImageView lastDatePickerClicked; // will save the last date picker that was clicked on
    private DatePickerHolder datePickerHolder = new DatePickerHolder();
    private PCRPatientModel patientQueried;

    /**
     * This onCreate is a little big, but here is the breakdown:
     * 1) Set up the OLIS Diagnostic Reports list view and the data that goes into it via an adapter.
     * 2) Set the start and end query dates. For the start date, get the current date minus 30 days. The end date will be set to today's date.
     * 3) Set the text views for the patient demographics section using the PCR patient data
     * @param savedInstanceState auto generated stub
     */
    @Override
    protected void onCreate(android.os.Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_summary);
        activity = this;

        /* *************************************************************************************************************************
         * 1)  Set up the list view and the data that goes into it via an adapter(which is dynamically selected based on userRole).
         * ************************************************************************************************************************/
        Intent intent = getIntent();
        patientQueried = intent.getExtras().getParcelable("patient");
        String olisResultBundleString = intent.getStringExtra("olisReport");

        // use OLIS service to convert bundle string to a Bundle
        setListViewData(OLISService.StringToBundle(olisResultBundleString));

        /* *********************************************************************************************************************************************
         * 2) Set the start and end query dates. For the start date, get the current date minus 120 days. The end date will be set to today's date.
         * ********************************************************************************************************************************************/
        // get today's date as a calendar object
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        // get the date formats for year, month, month to display, and day
        SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy", Locale.CANADA);
        SimpleDateFormat sdfMonth = new SimpleDateFormat("MM",  Locale.CANADA);
        SimpleDateFormat sdfMonthDisplay = new SimpleDateFormat("MMM",  Locale.CANADA); //for display purposes
        SimpleDateFormat sdfDay = new SimpleDateFormat("dd",  Locale.CANADA);

        // apply date formats and then turn them into int values
        // then set them as the end query dates
        datePickerHolder.queryEndYear = Integer.parseInt(sdfYear.format(calendar.getTime()));
        datePickerHolder.queryEndMonth = Integer.parseInt(sdfMonth.format(calendar.getTime())) - 1; // .getTime gets us a 1 indexed month, but datePickerDialog wants a 0 index month
        datePickerHolder.queryEndMonthDisplay = sdfMonthDisplay.format(calendar.getTime());
        datePickerHolder.queryEndDay = Integer.parseInt(sdfDay.format(calendar.getTime()));

        // get patient's birthdate Date Object using format for patient's birthdate
        SimpleDateFormat sdfBirthDate = new SimpleDateFormat("yyyy-MM-dd", Locale.CANADA);
        Date birthDate = new Date();

        try
        {
            birthDate = sdfBirthDate.parse(patientQueried.getDateOfBirthForQuery());
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

        calendar.setTime(birthDate);

        // reapply date formats with new calendar and get int values
        // then set them as the start query dates
        datePickerHolder.queryStartYear = Integer.parseInt(sdfYear.format(calendar.getTime()));
        datePickerHolder.queryStartMonth = Integer.parseInt(sdfMonth.format(calendar.getTime())) - 1; // .getTime gets us a 1 indexed month, but datePickerDialog wants a 0 index month
        datePickerHolder.queryStartMonthDisplay = sdfMonthDisplay.format(calendar.getTime());
        datePickerHolder.queryStartDay = Integer.parseInt(sdfDay.format(calendar.getTime()));

        // now lets set the textViews to reflect the dates
        setNewDates();

        /* *********************************************************************************************************************************************
         *  3) Set the text views for the patient demographics section using the PCR patient data
         * ********************************************************************************************************************************************/
        patientQueried = intent.getExtras().getParcelable("patient");

        // get TextView references
        TextView patientName = findViewById(R.id.nameItem);
        TextView healthCardNumber = findViewById(R.id.hcnItem);
        TextView gender = findViewById(R.id.genderItem);
        TextView dateOfBirth = findViewById(R.id.dobItem);

        //set the data
        if (patientQueried != null)
        {
            patientName.setText(patientQueried.getName());
            healthCardNumber.setText(patientQueried.getHealthCardNumber());
            gender.setText(patientQueried.getGender());
            dateOfBirth.setText(patientQueried.getDateOfBirth());
        }
    }

    /**
     * This method executes when a new date is selected from the dateDialogPicker.
     * Here is where we want to set the new date and execute a new query to the OLIS repository.
     * @param view the datePickerDialog instance
     * @param year the new year that was selected
     * @param month the new month that was selected (0-indexed)
     * @param dayOfMonth the new day that was selected
     */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth)
    {
        // convert new month into a 3 letter string
        String monthLong = new DateFormatSymbols(Locale.CANADA).getMonths()[month];
        String monthShort = monthLong.substring(0, 3);

        // set new the date vars
        if (lastDatePickerClicked.getId() == R.id.startCalendarIcon)
        {
            datePickerHolder.queryStartYear = year;
            datePickerHolder.queryStartMonth = month;
            datePickerHolder.queryStartMonthDisplay = monthShort;
            datePickerHolder.queryStartDay = dayOfMonth;
        }
        else if(lastDatePickerClicked.getId() == R.id.endCalendarIcon)
        {
            datePickerHolder.queryEndYear = year;
            datePickerHolder.queryEndMonth = month;
            datePickerHolder.queryEndMonthDisplay = monthShort;
            datePickerHolder.queryEndDay = dayOfMonth;
        }

        // change our textviews to reflect new dates
        setNewDates();

        // initialize the whenHandedOver dates for  new OLIS query
        // make sure to use %02d for month and day so that it is formatted properly for the query (incldues leading zeroes)
        String startDate = String.format(Locale.CANADA, "%d-%02d-%02d", datePickerHolder.queryStartYear, datePickerHolder.queryStartMonth + 1, datePickerHolder.queryStartDay);
        String endDate = String.format(Locale.CANADA, "%d-%02d-%02d", datePickerHolder.queryEndYear, datePickerHolder.queryEndMonth + 1, datePickerHolder.queryEndDay);

        // start the new OLIS query
        OLISAsyncTask OLISAsyncTask = new OLISAsyncTask(this, patientQueried);
        OLISAsyncTask.execute(startDate, endDate);
    }

    /**
     * This method uses the current values for queryStart and queryEnd dates and sets the values to their respective text view.
     * The dates are shown in an alphanumeric format: e.g. 25-May-2018
     */
    public void setNewDates()
    {
        TextView startDateTextView = findViewById(R.id.startDateItem);
        TextView endDateTextView = findViewById(R.id.endDateItem);
        startDateTextView.setText(String.format(Locale.CANADA, "%d-%s-%d", datePickerHolder.queryStartDay, datePickerHolder.queryStartMonthDisplay, datePickerHolder.queryStartYear));
        endDateTextView.setText(String.format(Locale.CANADA,"%d-%s-%d", datePickerHolder.queryEndDay, datePickerHolder.queryEndMonthDisplay, datePickerHolder.queryEndYear));
    }

    /**
     * This method saves the last date picker that was clicked on, and then launches the datePickerDialog with the current selected date values.
     * @param view
     */
    public void showDatePickerDialog(View view)
    {
        // save the last date picker that was clicked
        lastDatePickerClicked = (ImageView) view;

        // these are the initial values that will be used by the DatePickerDialog when it first loads a date picker
        int dialogInitialYear = 1996;
        int dialogInitialMonth = 5;
        int dialogInitialDay = 9;

        // Use the current queryStart values if we clicked on the start query icon,
        // otherwise use the queryEnd values
        if (lastDatePickerClicked.getId() == R.id.startCalendarIcon)
        {
            dialogInitialYear = datePickerHolder.queryStartYear;
            dialogInitialMonth = datePickerHolder.queryStartMonth;
            dialogInitialDay = datePickerHolder.queryStartDay;
        }
        else if (lastDatePickerClicked.getId() == R.id.endCalendarIcon)
        {
            dialogInitialYear = datePickerHolder.queryEndYear;
            dialogInitialMonth = datePickerHolder.queryEndMonth;
            dialogInitialDay = datePickerHolder.queryEndDay;
        }

        // initialize our DatePickerDialog and launch it
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, this, dialogInitialYear, dialogInitialMonth, dialogInitialDay);
        datePickerDialog.show();
    }

    /**
     * This method takes in a OLIS Bundle object and populates the Reports list view with its data
     * Mainly used to update old data with new data when a new date range is provided by the user
     * @param resultBundle the new data to fill the list view
     */
    public void setListViewData(Bundle resultBundle)
    {
        // initialise listview and data array
        ListView diagnosticReportList = findViewById(R.id.diagnostic_report_list);
        ArrayList<OLISDiagnosticReportModel> diagnosticReports = new ArrayList<>();

        if (resultBundle.getTotal() > 0)
        {
            //populate array list with OLIS Diagnostic Reports
            for (Bundle.BundleEntryComponent entry : resultBundle.getEntry())
            {
                DiagnosticReport diagnosticReportResource = null;

                // get OLIS Diagnostic Reports resource from our result bundle
                if (entry.getResource() instanceof DiagnosticReport)
                {
                    diagnosticReports.add(new OLISDiagnosticReportModel((DiagnosticReport) entry.getResource()));
                }
            }
        }

        // attach data to a the new adapter and then attach adapter to the ListView
        DiagnosticReportListAdapter diagnosticReportListAdapter = new DiagnosticReportListAdapter(this, diagnosticReports, diagnosticReportList);
        diagnosticReportList.setAdapter(diagnosticReportListAdapter);
    }

    /**
     * Clear the OLIS Diagnostic Reports listView
     */
    public void setListViewNoResults()
    {
        ListView diagnosticReportList = findViewById(R.id.diagnostic_report_list);
        List<String> noDispensesList = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, noDispensesList);
        diagnosticReportList.setAdapter(adapter);
    }

    /**
     * The DatePicker dialog wants each part of the date given separately, so this object will hold those values for us.
     */
    private class DatePickerHolder
    {
        // query start dates
        private int queryStartYear;
        private int queryStartMonth; //0 indexed month (e.g. Jan = 0)
        private String queryStartMonthDisplay; // Alphabet version of the month (Three letters)
        private int queryStartDay;

        //query end dates
        private int queryEndYear;
        private int queryEndMonth; //0 indexed month (e.g. Jan = 0)
        private String queryEndMonthDisplay; // Alphabetical version of the month (Three letters)
        private int queryEndDay;

    }
}
