package org.tint.ui.dialogs;

import org.tint.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.webkit.GeolocationPermissions.Callback;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class GeolocationPermissionsDialog extends Dialog {

	private Context mContext;
	
	private String mOrigin;
	private Callback mCallback;
	
	private TextView mMessageView;
	private CheckBox mRemember;
	
	private Button mAccept;
	private Button mDecline;
	
	public GeolocationPermissionsDialog(Context context) {
		super(context);
		mContext = context;
		
		setContentView(R.layout.geolocation_permissions_prompt);
		
		setTitle(R.string.GeolocationTitle);
		
		mMessageView = (TextView) findViewById(R.id.GeolocationMessage);
		mRemember = (CheckBox) findViewById(R.id.GeolocationRemember);
		
		mAccept = (Button) findViewById(R.id.GeolocationAccept);
		mDecline = (Button) findViewById(R.id.GeolocationDecline);
		
		mAccept.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				if (mCallback != null) {
					mCallback.invoke(mOrigin, true, mRemember.isChecked());
				}
				
				dismiss();
			}
		});
		
		mDecline.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				if (mCallback != null) {
					mCallback.invoke(mOrigin, false, mRemember.isChecked());
				}
				
				dismiss();
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	}
	
	public void initialize(String origin, Callback callback) {
		mOrigin = origin;
		mCallback = callback;
		
		mMessageView.setText(String.format(mContext.getString(R.string.GeolocationMessage), mOrigin));
		mRemember.setChecked(false);
	}

}
