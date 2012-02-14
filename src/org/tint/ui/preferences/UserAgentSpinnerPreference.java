package org.tint.ui.preferences;

import org.tint.R;
import org.tint.utils.Constants;

import android.content.Context;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.AttributeSet;

public class UserAgentSpinnerPreference extends BaseSpinnerPreference {

	public UserAgentSpinnerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected int getTitleArray() {
		return R.array.UserAgentsTitles;
	}

	@Override
	protected void setEditInputType() {
		mEditText.setInputType(InputType.TYPE_CLASS_TEXT);
	}

	@Override
	protected void setSpinnerValueFromPreferences() {
		String currentUserAgent = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(
				Constants.PREFERENCE_USER_AGENT,
						Constants.USER_AGENT_ANDROID);
		
		if (currentUserAgent.equals(Constants.USER_AGENT_ANDROID)) {
			mSpinner.setSelection(0);
			mEditText.setEnabled(false);
			mEditText.setText(Constants.USER_AGENT_ANDROID);
		} else if (currentUserAgent.equals(Constants.USER_AGENT_DESKTOP)) {
			mSpinner.setSelection(1);
			mEditText.setEnabled(false);
			mEditText.setText(Constants.USER_AGENT_DESKTOP);
		} else {
			mSpinner.setSelection(2);
			mEditText.setEnabled(true);
			mEditText.setText(currentUserAgent);					
		}
	}

	@Override
	protected void onSpinnerItemSelected(int position) {
		switch(position) {
		case 0:
			mEditText.setText(Constants.USER_AGENT_ANDROID);
			mEditText.setEnabled(false);
			break;
		case 1:
			mEditText.setText(Constants.USER_AGENT_DESKTOP);
			mEditText.setEnabled(false);
			break;
		case 2:
			mEditText.setEnabled(true);
			
			if ((mEditText.getText().toString().equals(Constants.USER_AGENT_ANDROID)) ||
					(mEditText.getText().toString().equals(Constants.USER_AGENT_DESKTOP))) {					
				mEditText.setText(null);
			}
			
			mEditText.selectAll();
			showKeyboard();
			
			break;
		default:
			mEditText.setText(Constants.USER_AGENT_ANDROID);
			mEditText.setEnabled(false);
			break;
		}
	}

}
