package ca.ehealth.ontario.olis_fhir_prototype.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import ca.ehealth.ontario.olis_fhir_prototype.R;
import ca.ehealth.ontario.olis_fhir_prototype.models.PCRPatientModel;
import ca.ehealth.ontario.olis_fhir_prototype.services.OLISAsyncTask;

/**
 * This adapter was meant for the listview responsible for displaying a list of patients from PCR.
 * Displayed in PCRListActivity
 */
public class PCRListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener
{
    private ArrayList<PCRPatientModel> patientList;
    private LayoutInflater layoutInflater;
    private Activity inActivity;

    /**
     * Nothing fancy about this constructor. Just need to save the data and context,
     * while also inflating our row view in order to inject data into it.
     *
     * @param activity    the inActivity that created this adapter (should be PCRListActivity)
     * @param patientData is an arraylist of patient demographics(Name, Gender, Date of birth)
     * @param listview    the listview using this adapter
     */
    public PCRListAdapter(Activity activity, ArrayList<PCRPatientModel> patientData, ListView listview)
    {
        patientList = patientData;
        listview.setOnItemClickListener(this);

        // needed for onClickItem
        inActivity = activity;

        // needed for view holder
        layoutInflater = LayoutInflater.from(activity.getApplicationContext());
    }

    @Override
    public int getCount()
    {
        return patientList.size();
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
        return patientList.get(position);
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
            inflatedView = layoutInflater.inflate(R.layout.list_row_pcr_patientlist, null);
            holder = new ViewHolder();

            holder.name = (TextView) inflatedView.findViewById(R.id.nameItem);
            holder.gender = (TextView) inflatedView.findViewById(R.id.genderItem);
            holder.dateOfBirth = (TextView) inflatedView.findViewById(R.id.dobItem);
            holder.labTotal = (TextView) inflatedView.findViewById(R.id.resultsItem);

            inflatedView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) inflatedView.getTag();
        }

        // insert data into the text views
        holder.name.setText(patientList.get(position).getName());
        holder.dateOfBirth.setText(patientList.get(position).getDateOfBirth());
        holder.gender.setText(patientList.get(position).getGender());
        holder.labTotal.setText(String.valueOf(patientList.get(position).getLabTotal()));


        // set the color of the row for alternating effect
        if (position % 2 == 1)
        {
            // id for the even-row background color
            int rowBackgroundColor = inflatedView.getResources().getColor(R.color.colorRowBackground);
            inflatedView.setBackgroundColor(rowBackgroundColor);
        }

        return inflatedView;
    }

    /**
     * This method will start a new OLIS Async task which will take the user to PatientSummaryActivity
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        OLISAsyncTask olisAsyncTask = new OLISAsyncTask(inActivity, patientList.get(position));
        olisAsyncTask.execute();
    }

    static class ViewHolder
    {
        TextView name;
        TextView gender;
        TextView dateOfBirth;
        TextView labTotal;
    }
}