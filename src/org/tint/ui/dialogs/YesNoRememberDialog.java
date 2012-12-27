/*
 * Tint Browser for Android
 * 
 * Copyright (C) 2012 - to infinity and beyond J. Devauchelle and contributors.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package org.tint.ui.dialogs;

import org.tint.R;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class YesNoRememberDialog extends Dialog {

	protected Context mContext;
	
	protected TextView mMessageView;
	protected CheckBox mRemember;
	
	protected Button mPositiveButton;
	protected Button mNegativeButton;
	
	public YesNoRememberDialog(Context context) {
		super(context);
		
		mContext = context;
		
		setContentView(R.layout.yes_no_remember_dialog);
		
		mMessageView = (TextView) findViewById(R.id.DialogMessage);
		mRemember = (CheckBox) findViewById(R.id.DialogRemember);
		
		mPositiveButton = (Button) findViewById(R.id.DialogPositiveButton);
		mNegativeButton = (Button) findViewById(R.id.DialogNegativeButton);
	}
	
	public void setPositiveButtonText(String text) {
		mPositiveButton.setText(text);
	}
	
	public void setPositiveButtonText(int text) {
		mPositiveButton.setText(text);
	}
	
	public void setNegativeButtonText(String text) {
		mNegativeButton.setText(text);
	}
	
	public void setNegativeButtonText(int text) {
		mNegativeButton.setText(text);
	}
	
	public void setPositiveButtonListener(View.OnClickListener listener) {
		mPositiveButton.setOnClickListener(listener);
	}
	
	public void setNegativeButtonListener(View.OnClickListener listener) {
		mNegativeButton.setOnClickListener(listener);
	}
	
	public void setMessage(String message) {
		mMessageView.setText(message);
	}
	
	public void setMessage(int message) {
		mMessageView.setText(message);
	}
	
	public boolean isRememberChecked() {
		return mRemember.isChecked();
	}
	
	public void setRememberChecked(boolean checked) {
		mRemember.setChecked(checked);
	}

}
