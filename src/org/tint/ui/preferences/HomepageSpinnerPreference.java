package org.tint.ui.preferences;

import org.tint.R;
import org.tint.utils.Constants;

import android.content.Context;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.AttributeSet;

public class HomepageSpinnerPreference extends BaseSpinnerPreference {

	public HomepageSpinnerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected int getTitleArray() {		
		return R.array.HomepageTitles;
	}
	
	@Override
	protected void setEditInputType() {
		mEditText.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
	}
	
	@Override
	protected void setSpinnerValueFromPreferences() {
		String currentHomepage = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(Constants.PREFERENCE_HOME_PAGE, Constants.URL_ABOUT_START);
	
		if (currentHomepage.equals(Constants.URL_ABOUT_START)) {
			mSpinner.setSelection(0);
			mEditText.setEnabled(false);
			mEditText.setText(Constants.URL_ABOUT_START);
		} else if (currentHomepage.equals(Constants.URL_ABOUT_BLANK)) {
			mSpinner.setSelection(1);
			mEditText.setEnabled(false);
			mEditText.setText(Constants.URL_ABOUT_BLANK);
		} else {
			mSpinner.setSelection(2);
			mEditText.setEnabled(true);
			mEditText.setText(currentHomepage);					
		}
	}
	
	@Override
	protected void onSpinnerItemSelected(int position) {
		switch(position) {
		case 0:
			mEditText.setText(Constants.URL_ABOUT_START);
			mEditText.setEnabled(false);
			break;
		case 1:
			mEditText.setText(Constants.URL_ABOUT_BLANK);
			mEditText.setEnabled(false);
			break;
		case 2:
			mEditText.setEnabled(true);
			
			if ((mEditText.getText().toString().equals(Constants.URL_ABOUT_START)) ||
					(mEditText.getText().toString().equals(Constants.URL_ABOUT_BLANK))) {					
				mEditText.setText(null);
			}
			
			mEditText.selectAll();
			showKeyboard();
			
			break;
		default:
			mEditText.setText(Constants.URL_ABOUT_START);
			mEditText.setEnabled(false);
			break;
		}
	}

}
