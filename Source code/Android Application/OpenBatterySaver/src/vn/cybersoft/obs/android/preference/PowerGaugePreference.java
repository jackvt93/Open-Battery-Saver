package vn.cybersoft.obs.android.preference;

import vn.cybersoft.obs.andriod.batterystats2.service.UidInfo;
import vn.cybersoft.obs.android.R;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


/**
 * Custom preference for displaying power consumption as a bar and an icon on
 * the left for the subsystem/app type.
 */
public class PowerGaugePreference extends Preference {
	
	private Drawable mIcon;
    private UidInfo mInfo;
    private int mProgress;
    private CharSequence mProgressText;

    public PowerGaugePreference(Context context, Drawable icon, UidInfo info) {
        super(context);
        setLayoutResource(R.layout.app_percentage_item);
        //setIcon(icon != null ? icon : new ColorDrawable(0));
        setAppIcon(icon != null ? icon : new ColorDrawable(0)); 
        mInfo = info;
    }

    public void setPercent(double percentOfMax, double percentOfTotal) {
        mProgress = (int) Math.ceil(percentOfMax);
        mProgressText = getContext().getResources().getString(
                R.string.percentage, (int) Math.ceil(percentOfTotal));
        notifyChanged();
    }

    UidInfo getInfo() {
        return mInfo;
    }
    
    public void setAppIcon(Drawable icon) { 
        if ((icon == null && mIcon != null) || (icon != null && mIcon != icon)) {
            mIcon = icon;

            notifyChanged();
        }
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        
        ImageView imageView = (ImageView) view.findViewById(android.R.id.icon);
        if (imageView != null) {
        	if (mIcon != null) {
        		imageView.setImageDrawable(mIcon);
        	}
        }
        imageView.setVisibility(mIcon != null ? View.VISIBLE : View.GONE);

        final ProgressBar progress = (ProgressBar) view.findViewById(android.R.id.progress);
        progress.setProgress(mProgress);

        final TextView text1 = (TextView) view.findViewById(android.R.id.text1);
        text1.setText(mProgressText);
    }
}
