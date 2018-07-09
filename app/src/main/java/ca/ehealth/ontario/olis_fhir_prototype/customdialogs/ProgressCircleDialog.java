package ca.ehealth.ontario.olis_fhir_prototype.customdialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Window;

import ca.ehealth.ontario.olis_fhir_prototype.R;

/**
 * Very simple dialog for showing a rotating progress circle.
 */
public class ProgressCircleDialog extends Dialog
{
    public ProgressCircleDialog(Activity activity)
    {
        super(activity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // no title required
        setContentView(R.layout.custom_dialog_progress_bar);
    }
}
