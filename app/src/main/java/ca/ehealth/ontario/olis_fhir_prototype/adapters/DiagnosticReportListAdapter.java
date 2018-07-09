package ca.ehealth.ontario.olis_fhir_prototype.adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import ca.ehealth.ontario.olis_fhir_prototype.R;
import ca.ehealth.ontario.olis_fhir_prototype.activities.DiagnosticReportDetailsActivity;
import ca.ehealth.ontario.olis_fhir_prototype.models.OLISDiagnosticReportModel;

/**
 * This adapter was meant for the listview responsible for displaying a list of reports from OLIS
 * for a given patient. Displayed in PatientSummaryActivity.
 */
public class DiagnosticReportListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener
{
    private ArrayList<OLISDiagnosticReportModel> diagnosticReports;
    private LayoutInflater layoutInflater;
    private Activity inActivity;

    /**
     * Nothing fancy about this constructor. Just need to save the data and context,
     * while also inflating our row layout view in order to inject data into it.
     *
     * @param activity          The activity that created this adapter (should be PatientSummaryActivity)
     * @param diagnosticReports Is an arraylist of patient lab reports, with each row item consisting of:
     *                          -Practitioner
     *                          -Organization
     *                          -Date
     * @param listview          the listview to load data into
     */
    public DiagnosticReportListAdapter(Activity activity, ArrayList<OLISDiagnosticReportModel> diagnosticReports, ListView listview)
    {
        this.diagnosticReports = diagnosticReports;
        listview.setOnItemClickListener(this);

        // needed for onClickItem
        inActivity = activity;

        // needed for view holder
        layoutInflater = LayoutInflater.from(activity.getApplicationContext());
    }

    @Override
    public int getCount()
    {
        return diagnosticReports.size();
    }

    /**
     * Gets an item based on a given position.
     *
     * @param position
     * @return the requested list item object
     */
    @Override
    public Object getItem(int position)
    {
        return diagnosticReports.get(position);
    }

    /**
     * Get list item id with for an item in a certain position.
     * <p>
     * For now, the item ID is also the position number.
     *
     * @param position
     * @return the ID of the requested item.
     */
    @Override
    public long getItemId(int position)
    {
        return position;
    }

    /**
     * Here is where we actually insert the data into a row layout.
     * We use a custom class called ViewHolder which will hold all of the text views that hold the data.
     * <p>
     * First we check to see if we already have an inflated row view.
     * If we do, then just reuse our viewHolder and save the previous instance (with getTag). This way we are not calling findViewById again.
     * If we don't, then we initialize it and get fresh text views for our view holder (setTag).
     * <p>
     * Once we've finished setting/getting the view tag, use the text view references in our ViewHolder to 'setText' the data into the text views
     *
     * @param position     of the list item in the data
     * @param inflatedView the inflated row layout view
     * @param parent
     * @return the inflated row view with data inserted
     */
    @Override
    public View getView(int position, View inflatedView, ViewGroup parent)
    {
        ViewHolder holder;

        if (inflatedView == null)
        {
            inflatedView = layoutInflater.inflate(R.layout.list_row_lab_report, parent, false);
            holder = new ViewHolder();

            holder.practitionerName = inflatedView.findViewById(R.id.practitionerItem);
            holder.organizationName = inflatedView.findViewById(R.id.organizationItem);
            holder.testDate = inflatedView.findViewById(R.id.dateItem);

            inflatedView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) inflatedView.getTag();
        }

        holder.practitionerName.setText(diagnosticReports.get(position).getPractitionerName());
        holder.organizationName.setText(diagnosticReports.get(position).getOrganizationName());
        holder.testDate.setText(diagnosticReports.get(position).getTestReleaseDate());

        // set the color of the row for alternating effect
        if (position % 2 == 1)
        {
            inflatedView.setBackgroundColor(inflatedView.getResources().getColor(R.color.colorRowBackground));
        }

        return inflatedView;
    }

    /**
     * This method will send the user to the DiagnosticReportDetailsActivity when they click on a
     * lab report from the listview of lab reports (in PatientSummary)
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Intent intent = new Intent(inActivity.getApplicationContext(), DiagnosticReportDetailsActivity.class);
        intent.putExtra("olisReport", diagnosticReports.get(position));
        inActivity.startActivity(intent);
    }

    static class ViewHolder
    {
        TextView practitionerName;
        TextView organizationName;
        TextView testDate;
    }
}
