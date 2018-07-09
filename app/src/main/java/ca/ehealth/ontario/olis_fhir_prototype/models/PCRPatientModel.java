package ca.ehealth.ontario.olis_fhir_prototype.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * This model represents a single patient returned by PCR.
 * Implements parcelable so that it can be moved between activities.
 */
public class PCRPatientModel implements Parcelable
{
    private String name = "n/a";
    private String gender = "n/a";
    private String dateOfBirth = "n/a";
    private String healthCardNumber = "n/a";
    private int labTotal = 0;
    
    public PCRPatientModel()
	{
        // use default values
    }

    // simple constructor for manually creating a PCR patient
    public PCRPatientModel(String name, String gender, String dateOfBirth, String healthCardNumber, int labTotal)
	{
        setName(name);
        setGender(gender);
        setDateOfBirth(dateOfBirth);
        setHealthCardNumber(healthCardNumber);
        setLabTotal(labTotal);
    }

    //getters
    public String getName()
	{
        return name;
    }
    public String getGender()
	{
        return gender;
    }
    public String getDateOfBirth()
	{
        return dateOfBirth;
    }
    public String getDateOfBirthForQuery()
    {
        String dateOfBirthForQuery = "n/a";

        try
        {
            // format pattern that is returned by PCR (after being formatted by PCRService): 24-May-1987
            SimpleDateFormat formatReceived = new SimpleDateFormat("dd-MMM-yyyy", Locale.CANADA);

            // parse out the Date object from the birth date using the format object
            Date birthDate = formatReceived.parse(dateOfBirth);

            // apply a format like: 05-Feb-1987 to the string
            formatReceived.applyPattern("yyyy-MM-dd");

            // finally, we can save the date with new format applied
            dateOfBirthForQuery =  formatReceived.format(birthDate);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

        return dateOfBirthForQuery;
    }
    public String getHealthCardNumber()
    {
        return healthCardNumber;
    }
    public int getLabTotal()
    {
        return labTotal;
    }

    // setters
    public void setName(String firstName)
	{
        this.name = firstName;
    }
    public void setGender(String gender)
	{
        this.gender = gender;
    }
    public void setDateOfBirth(String dateOfBirth)
	{
        this.dateOfBirth = dateOfBirth;
    }
    public void setHealthCardNumber(String healthCardNumber) { this.healthCardNumber = healthCardNumber; }
    public void setLabTotal(int labTotal)
    {
        this.labTotal = labTotal;
    }

    /* **************************************************************************************************************************
     * Parcelable stuff goes under here
     * *************************************************************************************************************************/

    /**
     * Constructor when a parcel object is being given for the Parcelable implementation.
     * @param in the parcel coming in
     */
    public PCRPatientModel(Parcel in)
    {
        setName(in.readString());
        setGender(in.readString());
        setDateOfBirth(in.readString());
        setHealthCardNumber(in.readString());
        setLabTotal(in.readInt());
    }

    /**
     * Required by parcelable
     */
    public static final Creator<PCRPatientModel> CREATOR = new Creator<PCRPatientModel>()
    {
        @Override
        public PCRPatientModel createFromParcel(Parcel in)
        {
            return new PCRPatientModel(in);
        }

        @Override
        public PCRPatientModel[] newArray(int size)
        {
            return new PCRPatientModel[size];
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

    /**
     * Auto generated method stub (with added getters)
     * @param dest
     * @param flags
     */
    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(getName());
        dest.writeString(getGender());
        dest.writeString(getDateOfBirth());
        dest.writeString(getHealthCardNumber());
        dest.writeInt(getLabTotal());
    }
}