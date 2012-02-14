package org.tint.ui.preferences;

import org.tint.R;
import org.tint.utils.Constants;

import android.content.Context;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.AttributeSet;

public class SearchUrlSpinnerPreference extends BaseSpinnerPreference {

	public SearchUrlSpinnerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected int getTitleArray() {		
		return R.array.SearchUrlTitles;
	}

	@Override
	protected void setEditInputType() {
		mEditText.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
	}

	@Override
	protected void setSpinnerValueFromPreferences() {
		String currentSearchUrl = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(
				Constants.PREFERENCE_SEARCH_URL,
						getContext().getString(R.string.SearchUrlGoogle));
		
		if (currentSearchUrl.equals(getContext().getString(R.string.SearchUrlGoogle))) {
			mSpinner.setSelection(0);
			mEditText.setEnabled(false);
			mEditText.setText(getContext().getString(R.string.SearchUrlGoogle));
		} else if (currentSearchUrl.equals(getContext().getString(R.string.SearchUrlWikipedia))) {
			mSpinner.setSelection(1);
			mEditText.setEnabled(false);
			mEditText.setText(getContext().getString(R.string.SearchUrlWikipedia));
		} else {
			mSpinner.setSelection(2);
			mEditText.setEnabled(true);
			mEditText.setText(currentSearchUrl);					
		}
	}

	@Override
	protected void onSpinnerItemSelected(int position) {
		switch(position) {
		case 0:
			mEditText.setText(getContext().getString(R.string.SearchUrlGoogle));
			mEditText.setEnabled(false);
			break;
		case 1:
			mEditText.setText(getContext().getString(R.string.SearchUrlWikipedia));
			mEditText.setEnabled(false);
			break;
		case 2:
			mEditText.setEnabled(true);
			
			if ((mEditText.getText().toString().equals(getContext().getString(R.string.SearchUrlGoogle))) ||
					(mEditText.getText().toString().equals(getContext().getString(R.string.SearchUrlWikipedia)))) {					
				mEditText.setText(null);
			}
			
			mEditText.selectAll();
			showKeyboard();
			
			break;
		default:
			mEditText.setText(getContext().getString(R.string.SearchUrlGoogle));
			mEditText.setEnabled(false);
			break;
		}
	}

}
