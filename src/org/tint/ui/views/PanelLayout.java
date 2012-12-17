package org.tint.ui.views;

import org.tint.R;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class PanelLayout extends RelativeLayout {
	
	private static final int ANIMATION_DURATION = 150;
	private static final int BEZEL_SIZE = 20;
	private static final int BEZEL_SIZE_OPEN = 100;

	private Animator mAnimator;

	private boolean mPanelShown;

	private AnimatorListener mShowListener;
	private AnimatorListener mHideListener;

	private RelativeLayout mContent;
	private RelativeLayout mPanel;
	
	private TabsScroller mTabsScroller;

	private boolean mInSlide;
	private float mBezelTopDelta;
	private float mBezelSize;
	private float mBezelSizeOpen;
	private float mLastX;
	private float mTranslation;
	private float mAlpha;
	private boolean mLastMoveOpen;

	public PanelLayout(Context context) {
		this(context, null);
	}

	public PanelLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PanelLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mInSlide = false;
		mLastMoveOpen = false;
		mTranslation = 0;
		mAlpha = 0;

		mPanelShown = false;
		mAnimator = null;

		if (!isInEditMode()) {
			
			TypedValue tv = new TypedValue();
			context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
			
			mBezelTopDelta = getResources().getDimension(tv.resourceId);
			mBezelSize = BEZEL_SIZE * context.getResources().getDisplayMetrics().density + 0.5f;
			mBezelSizeOpen = BEZEL_SIZE_OPEN * context.getResources().getDisplayMetrics().density + 0.5f;
			
			LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = layoutInflater.inflate(R.layout.panel_layout, this);

			mContent = (RelativeLayout) v.findViewById(R.id.main_content);
			mPanel = (RelativeLayout) v.findViewById(R.id.panel);

			mTabsScroller = (TabsScroller) v.findViewById(R.id.tabs_scroller);

			mShowListener = new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mAnimator = null;
					mPanel.requestLayout();
					mPanelShown = true;
					mTranslation = mPanel.getWidth();
					mAlpha = 1;
				}               
			};

			mHideListener = new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mAnimator = null;
					mPanelShown = false;
					mTranslation = 0;
					mAlpha = 0;
				}
			};
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			float x = ev.getX();
			float y = ev.getY();

			float bezelSize = mPanelShown ? mBezelSizeOpen : mBezelSize;
			
			if ((y > mBezelTopDelta) &&
					(x >= mTranslation) &&
					(x <= mTranslation + bezelSize)) {
				return true;
			}

			break;

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_MOVE:
			if (mInSlide) {
				return true;
			}			

			break;
		}

		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			float x = event.getX();
			float y = event.getY();

			float bezelSize = mPanelShown ? mBezelSizeOpen : mBezelSize;
			
			if ((y > mBezelTopDelta) &&
					(x >= mTranslation) &&
					(x <= mTranslation + bezelSize)) {

				mInSlide = true;
				mLastX = event.getX();

				return true;
			}

			break;

		case MotionEvent.ACTION_UP:
			if (mInSlide) {
				mInSlide = false;
				
				if (mLastMoveOpen) {
					if (mTranslation >= 0.2 * mPanel.getWidth()) {
						showPanel();
					} else {
						hidePanel();
					}
				} else {
					if (mTranslation <= 0.9 * mPanel.getWidth()) {
						hidePanel();
					} else {
						showPanel();
					}
				}

				return true;
			}

			break;

		case MotionEvent.ACTION_MOVE:
			if (mInSlide) {
				
				float translation = event.getX() - mLastX;
				
				mLastMoveOpen = translation >= 0;
				
				mTranslation += translation;
				mAlpha = mTranslation / mPanel.getWidth();

				if (mTranslation > mPanel.getWidth()) {
					mTranslation = mPanel.getWidth();
					mAlpha = 1;
					mPanelShown = true;
				}

				if (mTranslation < 0) {
					mTranslation = 0;
					mAlpha = 0;
					mPanelShown = false;
				}

				mLastX = event.getX();

				mContent.setTranslationX(mTranslation);
				mPanel.setAlpha(mAlpha);

				return true;
			}

			break;
		}

		return super.onTouchEvent(event);
	}

	public TabsScroller getTabsScroller() {
		return mTabsScroller;
	}
	
	public ImageView getAddTabButton() {
		return (ImageView) mPanel.findViewById(R.id.BtnAddTab);
	}
	
	public void togglePanel() {
		if (mPanelShown) {
			hidePanel();
		} else {
			showPanel();
		}
	}

	public void showPanel() {
		if (mAnimator != null) {
			mAnimator.end();
		}

		mPanel.setAlpha(mAlpha);

		AnimatorSet animator = new AnimatorSet();		

		AnimatorSet.Builder b = animator.play(ObjectAnimator.ofFloat(mPanel, "alpha", 1));

		b.with(ObjectAnimator.ofFloat(mContent, "translationX", mPanel.getWidth()));

		animator.addListener(mShowListener);

		mAnimator = animator;
				
		mAnimator.setDuration((long) (ANIMATION_DURATION * ((mPanel.getWidth() - mTranslation) / mPanel.getWidth())));
		mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

		mAnimator.start();
	}

	public void hidePanel() {
		if (mAnimator != null) {
			mAnimator.end();
		}

		mPanel.setAlpha(mAlpha);

		AnimatorSet animator = new AnimatorSet();
		AnimatorSet.Builder b = animator.play(ObjectAnimator.ofFloat(mPanel, "alpha", 0));

		b.with(ObjectAnimator.ofFloat(mContent, "translationX", 0));

		animator.addListener(mHideListener);

		mAnimator = animator;

		mAnimator.setDuration((long) (ANIMATION_DURATION * (mTranslation / mPanel.getWidth())));
		mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

		mAnimator.start();
	}

}
