package org.tint.ui.activities;

import org.tint.R;
import org.tint.providers.BookmarksWrapper;
import org.tint.utils.Constants;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditBookmarkActivity extends Activity {

	private long mId = -1;
	
	private EditText mLabel;
	private EditText mUrl;
	
	private Button mOk;
	private Button mCancel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_bookmark_activity);
		setTitle(R.string.AddBookmarkTitle);
		
		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setHomeButtonEnabled(true);
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
        
        mLabel = (EditText) findViewById(R.id.EditBookmarkActivity_LabelEdit);
        mUrl = (EditText) findViewById(R.id.EditBookmarkActivity_UrlEdit);
        
        mOk = (Button) findViewById(R.id.EditBookmarkActivity_OK);
        mOk.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				if (save()) {
					setResult(RESULT_OK);
					finish();
				}
			}
		});
        
        mCancel = (Button) findViewById(R.id.EditBookmarkActivity_Cancel);
        mCancel.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				setResult(RESULT_CANCELED);
	            finish();
			}
		});
        
        Bundle extras = getIntent().getExtras();
    	if (extras != null) {
    		String label = extras.getString(Constants.EXTRA_LABEL);
    		if (!TextUtils.isEmpty(label)) {
    			mLabel.setText(label);
    		}
    		
    		String url = extras.getString(Constants.EXTRA_URL);
    		if (!TextUtils.isEmpty(url)) {
    			mUrl.setText(url);
    		}
    		
    		mId = extras.getLong(Constants.EXTRA_ID);
    	}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			setResult(RESULT_CANCELED);
            finish();
			return true;
		default: return super.onContextItemSelected(item);
		 }
	}
	
	private boolean save() {
		String label = mLabel.getText().toString();
		String url = mUrl.getText().toString();
		
		if ((!TextUtils.isEmpty(label)) &&
				(!TextUtils.isEmpty(url))) {
			BookmarksWrapper.setAsBookmark(getContentResolver(), mId, label, url, true);
			return true;
		} else {
			Toast.makeText(this, R.string.AddBookmarkLabelOrUrlEmpty, Toast.LENGTH_SHORT).show();
			return false;
		}
	}

}
