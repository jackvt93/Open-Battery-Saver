/*
 * Copyright (C) 2014 IUH €yber$oft Team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package vn.cybersoft.obs.android.dialog;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;

import vn.cybersoft.obs.android.R;
import vn.cybersoft.obs.android.provider.OptimalMode;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @author Luan Vu (hlvu.cybersoft@gmail.com)
 *
 */
@SuppressLint("NewApi") 
public class SwitchModeConfirmDialog extends Dialog implements DialogInterface {
	
    private final Context mContext;
    private final DialogInterface mDialogInterface;
    private final Window mWindow;
    
    
    private CharSequence mTitle;
    
    private OptimalMode mOptimalMode;
    
    private TextView mTitleView;
	
	private TextView mScreenBrightnessText;
	
	private TextView mScreenTimeoutText;
	
	private TextView mVibrateText;
	
	private TextView mWifiText;
	
	private TextView mBluetoothText;
	
	private TextView mSyncText;
	
	private TextView mHapticFeedbackText;
	
	private Button mPositiveButton;
	
	private DialogInterface.OnClickListener mPositiveButtonListener;

    private CharSequence mPositiveButtonText;
	
	private Message mPositiveButtonMessage;
	
	private Button mNegativeButton;
	
	private DialogInterface.OnClickListener mNegativeButtonListener;
	
    private CharSequence mNegativeButtonText;
	
    private Message mNegativeButtonMessage;
	
    private Handler mHandler;
	
	View.OnClickListener mButtonHandler = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Message m = null;
			if (v == mPositiveButton && mPositiveButtonMessage != null) {
				m = Message.obtain(mPositiveButtonMessage);
			} else if (v == mNegativeButton && mNegativeButtonMessage != null) {
				m = Message.obtain(mNegativeButtonMessage);
			}
			
			if (m != null) {
				m.sendToTarget();
			}
			
            // Post a message so we dismiss after the above handlers are executed
            mHandler.obtainMessage(ButtonHandler.MSG_DISMISS_DIALOG, mDialogInterface)
                    .sendToTarget();
		}
	};
	
    private static final class ButtonHandler extends Handler {
        // Button clicks have Message.what as the BUTTON{1,2,3} constant
        private static final int MSG_DISMISS_DIALOG = 1;
        
        private WeakReference<DialogInterface> mDialog;

        public ButtonHandler(DialogInterface dialog) {
            mDialog = new WeakReference<DialogInterface>(dialog);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                
                case DialogInterface.BUTTON_POSITIVE:
                case DialogInterface.BUTTON_NEGATIVE:
                case DialogInterface.BUTTON_NEUTRAL:
                    ((DialogInterface.OnClickListener) msg.obj).onClick(mDialog.get(), msg.what);
                    break;
                    
                case MSG_DISMISS_DIALOG:
                    ((DialogInterface) msg.obj).dismiss();
            }
        }
    }
	

	/**
	 * @param context
	 */
	public SwitchModeConfirmDialog(Context context, OptimalMode mode) {
		this(context, 0, mode);
	}

	/**
	 * @param context
	 * @param theme
	 */
	public SwitchModeConfirmDialog(Context context, int theme, OptimalMode mode) {
		super(context, theme);
		
        Context themeContext = getContext();
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        LayoutInflater inflater =
                (LayoutInflater) themeContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.switch_mode_confirm_layout, null);
        setContentView(view);
        
		mScreenBrightnessText = (TextView) view.findViewById(R.id.screenBrightnessText);
		mScreenTimeoutText = (TextView) view.findViewById(R.id.screenTimeoutText);
		mVibrateText = (TextView) view.findViewById(R.id.vibrateText);
		mWifiText = (TextView) view.findViewById(R.id.wifiText);
		mBluetoothText = (TextView) view.findViewById(R.id.bluetoothText);
		mSyncText = (TextView) view.findViewById(R.id.syncText);
		mHapticFeedbackText = (TextView) view.findViewById(R.id.hapticFeedbackText);
        
        mContext = getContext();
        mDialogInterface = this;
        mHandler = new ButtonHandler(mDialogInterface);
        mWindow = getWindow();
        mOptimalMode = mode;
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		installContent();
	}
	
	public void setOptimalMode(OptimalMode mode) {
		mOptimalMode = mode;
		
		if (mOptimalMode != null) {
			//mScreenBrightnessText.setText(getContext().getString(R.string.percent_value, mode.screenBrightness * 100 / 255)); 
			mScreenBrightnessText.setText(getBrightnessPercentage(mode.screenBrightness)); 
			//mScreenTimeoutText.setText(getContext().getString(R.string.seconds, mode.screenTimeout));
			mScreenTimeoutText.setText(getReadableScreenTimeout(mode.screenTimeout));
			mVibrateText.setText(mode.vibrate ? getContext().getString(R.string.on) : getContext().getString(R.string.off));
			mWifiText.setText(mode.wifi ? getContext().getString(R.string.on) : getContext().getString(R.string.off));
			mBluetoothText.setText(mode.bluetooth ? getContext().getString(R.string.on) : getContext().getString(R.string.off));
			mSyncText.setText(mode.sync ? getContext().getString(R.string.on) : getContext().getString(R.string.off));
			mHapticFeedbackText.setText(mode.hapticFeedback ? getContext().getString(R.string.on) : getContext().getString(R.string.off)); 
		}
	}
	
    public void setButton(int whichButton, CharSequence text,
            DialogInterface.OnClickListener listener, Message msg) {

        if (msg == null && listener != null) {
            msg = mHandler.obtainMessage(whichButton, listener);
        }
        
        switch (whichButton) {

            case DialogInterface.BUTTON_POSITIVE:
                mPositiveButtonText = text;
                mPositiveButtonMessage = msg;
                break;
                
            case DialogInterface.BUTTON_NEGATIVE:
                mNegativeButtonText = text;
                mNegativeButtonMessage = msg;
                break;
                
            default:
                throw new IllegalArgumentException("Button does not exist");
        }
    }
    
	public void setPositiveButton(int resId, final OnClickListener listener) {
		mPositiveButtonText = mContext.getText(resId);
		mPositiveButtonListener = listener;
	}
	
	public void setPositiveButton(CharSequence text, final OnClickListener listener) {
		mPositiveButtonText = text;
		mPositiveButtonListener = listener;
	}
	
	public void setNegativeButton(int resId, final OnClickListener listener) {
		mNegativeButtonText = mContext.getText(resId);
		mNegativeButtonListener = listener;
	}
	
	public void setNegativeButton(CharSequence text, final OnClickListener listener) {
		mNegativeButtonText = text;
		mNegativeButtonListener = listener;
	}
	
	public void setTitle(CharSequence title) {
		mTitle = title;
	}
	
    public void installContent() {
        setupView();
    }
    
    private void setupView() {
        boolean hasButtons = setupButtons();
        
        LinearLayout topPanel = (LinearLayout) mWindow.findViewById(R.id.topPanel);
        setupTitle(topPanel);
        
        setupOptimalMode();
        
        View buttonPanel = mWindow.findViewById(R.id.buttonPanel);
        if (!hasButtons) {
            buttonPanel.setVisibility(View.GONE);
        }
    }
    
    private String getBrightnessPercentage(int value) {
    	int round =   Math.round((float)value * 100 / 255);
    	float unRound = Math.round((float)round / 10);
    	int ret = (int) (unRound * 10);
    	return getContext().getString(R.string.percentage, ret == 0 ? 10 : ret);
    }
    
private String getReadableScreenTimeout(int millis) {
	String[] texts = getContext().getResources().getStringArray(R.array.screen_timeout_entries);
	String[] values = getContext().getResources().getStringArray(R.array.screen_timeout_values);
	
	List<String> vals = Arrays.asList(values);
	int idx = vals.indexOf(Integer.toString(millis));
	return idx != -1 ? texts[idx] : "None"; 
}
    
    
	private boolean setupTitle(LinearLayout topPanel) {
		boolean hasTitle = true;
		final boolean hasTextTitle = !TextUtils.isEmpty(mTitle);

		if (hasTextTitle) {
			/* Display the title if a title is supplied, else hide it */
			mTitleView = (TextView) mWindow.findViewById(R.id.titleText);
			mTitleView.setText(mTitle);
		} else {
			// Hide the title template
			View titleTemplate = mWindow.findViewById(R.id.title_template);
			titleTemplate.setVisibility(View.GONE);
			topPanel.setVisibility(View.GONE);
			hasTitle = false;
		}
		return hasTitle;
	}
	
	private void setupOptimalMode() {
		mScreenBrightnessText = (TextView) mWindow.findViewById(R.id.screenBrightnessText);
		mScreenTimeoutText = (TextView) mWindow.findViewById(R.id.screenTimeoutText);
		mVibrateText = (TextView) mWindow.findViewById(R.id.vibrateText);
		mWifiText = (TextView) mWindow.findViewById(R.id.wifiText);
		mBluetoothText = (TextView) mWindow.findViewById(R.id.bluetoothText);
		mSyncText = (TextView) mWindow.findViewById(R.id.syncText);
		mHapticFeedbackText = (TextView) mWindow.findViewById(R.id.hapticFeedbackText);
	}
	
	private boolean setupButtons() {
        int BIT_BUTTON_POSITIVE = 1;
        int BIT_BUTTON_NEGATIVE = 2;
        int whichButtons = 0;
        mPositiveButton = (Button) mWindow.findViewById(R.id.button1);
        mPositiveButton.setOnClickListener(mButtonHandler);

        if (TextUtils.isEmpty(mPositiveButtonText)) {
            mPositiveButton.setVisibility(View.GONE);
        } else {
            mPositiveButton.setText(mPositiveButtonText);
            mPositiveButton.setVisibility(View.VISIBLE);
            whichButtons = whichButtons | BIT_BUTTON_POSITIVE;
        }

        mNegativeButton = (Button) mWindow.findViewById(R.id.button2);
        mNegativeButton.setOnClickListener(mButtonHandler);

        if (TextUtils.isEmpty(mNegativeButtonText)) {
            mNegativeButton.setVisibility(View.GONE);
        } else {
            mNegativeButton.setText(mNegativeButtonText);
            mNegativeButton.setVisibility(View.VISIBLE);

            whichButtons = whichButtons | BIT_BUTTON_NEGATIVE;
        }
        
        return whichButtons != 0;
    }
	
	public void create() {
		if (mTitle != null) {
			setTitle(mTitle);
		}
		
		if (mOptimalMode != null) {
			setOptimalMode(mOptimalMode);
		}
		
		if (mPositiveButtonText != null) {
			setButton(DialogInterface.BUTTON_POSITIVE, mPositiveButtonText, mPositiveButtonListener, null);
		}
		
		if (mNegativeButtonText != null) {
			setButton(DialogInterface.BUTTON_NEGATIVE, mNegativeButtonText, mNegativeButtonListener, null);
		}
	}
	
	@Override
	public void show() {
		create();
		super.show();
	}
	
	

}
