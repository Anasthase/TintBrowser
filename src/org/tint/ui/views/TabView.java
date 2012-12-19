package org.tint.ui.views;

import org.tint.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Picture;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TabView extends LinearLayout {

	private ImageView mClose;
	private TextView mTitle;
	private View mTitleBar;
	private ImageView mImage;
	
	private int mImageWidth;
	private int mImageHeight;
	
	private OnClickListener mClickListener;

	public TabView(Context context) {
		super(context);
		init(context);
	}

	public TabView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}    

	public TabView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		LayoutInflater.from(context).inflate(R.layout.tab_view, this);

		mClose = (ImageView) findViewById(R.id.closetab);
		mTitle = (TextView) findViewById(R.id.title);
		mTitleBar = findViewById(R.id.titlebar);
		mImage = (ImageView) findViewById(R.id.tab_view);
		
		mImageWidth = (int) (200 * context.getResources().getDisplayMetrics().density);
		mImageHeight = (int) (120 * context.getResources().getDisplayMetrics().density);
	}

	public boolean isClose(View v) {
		return v == mClose;
	}

	public boolean isTitle(View v) {
		return v == mTitleBar;
	}

	public boolean isWebView(View v) {
		return v == mImage;
	}
	
	public void setImage(Picture picture) {
		Bitmap bm = Bitmap.createBitmap(mImageWidth, mImageHeight, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bm);
		
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
		p.setColor(0xFFFFFFFF);
		canvas.drawRect(0, 0, mImageWidth, mImageHeight, p);
		
		if (picture != null) {
			float scale = mImageWidth / (float) picture.getWidth();
			canvas.scale(scale, scale);

			picture.draw(canvas);			
		}
		
		mImage.setImageBitmap(bm);
	}
	
	public void setImageResource(int resource) {
		mImage.setImageResource(resource);
	}

//	private void setTitle() {
//		if (mTab == null) return;
//		if (mHighlighted) {
//			mTitle.setText(mTab.getUrl());
//		} else {
//			String txt = mTab.getTitle();
//			if (txt == null) {
//				txt = mTab.getUrl();
//			}
//			mTitle.setText(txt);
//		}
//		if (mTab.isSnapshot()) {
//			setTitleIcon(R.drawable.ic_history_holo_dark);
//		} else if (mTab.isPrivateBrowsingEnabled()) {
//			setTitleIcon(R.drawable.ic_incognito_holo_dark);
//		} else {
//			setTitleIcon(0);
//		}
//	}
	
	public void setTitle(String title) {
		mTitle.setText(title);
	}
	
	public void setTitle(int title) {
		mTitle.setText(title);
	}

//	protected Long getWebViewId(){
//		if(mTab == null) return null;
//		return new Long(mTab.getId());
//	}
//
//	protected void setWebView(Tab tab) {
//		mTab = tab;
//		setTitle();
//		Bitmap image = tab.getScreenshot();
//		if (image != null) {
//			mImage.setImageBitmap(image);
//			if (tab != null) {
//				mImage.setContentDescription(tab.getTitle());
//			}
//		}
//	}

	@Override
	public void setOnClickListener(OnClickListener listener) {
		mClickListener = listener;
		mTitleBar.setOnClickListener(mClickListener);
		mClose.setOnClickListener(mClickListener);
		if (mImage != null) {
			mImage.setOnClickListener(mClickListener);
		}
	}

}
