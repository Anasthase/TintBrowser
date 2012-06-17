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

package org.tint.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Scroller;

public class ToolbarsAnimator2 {
	
	private ViewGroup mLateralBar;
	private ViewGroup mContainer;
	
	private Animator mAnimator;	
	
	private AnimatorListener mShowListener;
	private AnimatorListener mHideListener;
	
	private boolean mToolbarsVisible;
	
	public ToolbarsAnimator2(ViewGroup lateralBar, ViewGroup container) {
		mLateralBar = lateralBar;
		mContainer = container;
		
		mShowListener = new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mAnimator = null;
//				mLateralBar.requestLayout();
				mContainer.requestLayout();
			}
		};
		
		mHideListener = new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mAnimator = null;
//				mLateralBar.setVisibility(View.GONE);
				mContainer.requestLayout();
			}
		};
		
		mToolbarsVisible = false;
		mAnimator = null;
	}
	
	public void startShowAnimation() {
//		if (mAnimator != null) {
//			mAnimator.end();
//		}
//		
////		mLateralBar.setVisibility(View.VISIBLE);
//		mLateralBar.setAlpha(0);
//		
//		AnimatorSet animator = new AnimatorSet();
//		AnimatorSet.Builder b = animator.play(ObjectAnimator.ofFloat(mLateralBar, "alpha", 1));
////		b.with(ObjectAnimator.ofFloat(mLateralBar, "translationX", 0));
//		b.with(ObjectAnimator.ofFloat(mContainer, "translationX", mLateralBar.getWidth()));
//		
//		animator.addListener(mShowListener);		
//		mAnimator = animator;		
//		animator.start();
		
		new ScrollerRunnable().startShow();
//		new AlphaScrollerRunnable().start();
		
		mToolbarsVisible = true;
	}
	
	public void startHideAnimation() {
//		if (mAnimator != null) {
//			mAnimator.end();
//		}
//		
//		mLateralBar.setAlpha(1);
//		
//		AnimatorSet animator = new AnimatorSet();
//		AnimatorSet.Builder b = animator.play(ObjectAnimator.ofFloat(mLateralBar, "alpha", 0));
//		
////		b.with(ObjectAnimator.ofFloat(mLateralBar, "translationX", - mLateralBar.getWidth()));
//		b.with(ObjectAnimator.ofFloat(mContainer, "translationX", 0));
//		
//		animator.addListener(mHideListener);		
//		mAnimator = animator;
//		animator.start();
		
		new ScrollerRunnable().startHide();
		
		mToolbarsVisible = false;
	}
	
	public void toggle() {
		if (mToolbarsVisible) {
			startHideAnimation();
		} else {
			startShowAnimation();
		}
	}
	
	public boolean isToolbarsVisible() {
		return mToolbarsVisible;
	}
	
	private class AlphaScrollerRunnable implements Runnable {
		
		private Scroller mScroller;
		
		public AlphaScrollerRunnable() {
			mScroller = new Scroller(mLateralBar.getContext());
		}
		
		public void start() {
			mScroller.startScroll(0, 0, 100, 0);
			mLateralBar.post(this);
		}

		@Override
		public void run() {
			if (mScroller.isFinished()) {
				return;
			}
			
			boolean hasMore = mScroller.computeScrollOffset();
			int alpha = mScroller.getCurrX();
			
			Log.d("Alpha", Integer.toString(alpha));
			
			mLateralBar.setAlpha(alpha / 100f);
			
			if (hasMore) {
				mLateralBar.post(this);
			}
		}
		
	}
	
	private class ScrollerRunnable implements Runnable {
		
		private Scroller mScroller;
		
		private int mLastX;
		
		public ScrollerRunnable() {
			mScroller = new Scroller(mContainer.getContext());
		}
		
		public void startShow() {
			doStart(true);
		}
		
		public void startHide() {
			doStart(false);
		}

		@Override
		public void run() {
			if (mScroller.isFinished()) {
				return;
			}
			
			boolean hasMore = mScroller.computeScrollOffset();
			int x = mScroller.getCurrX();
			
			int diff = mLastX - x;
			if (diff != 0) {	
				mContainer.offsetLeftAndRight(diff);
				mLastX = x;
			}
			
			if (hasMore) {
				mContainer.post(this);
			}
		}
		
		private void doStart(boolean show) {
			int initialX = (int) mContainer.getX();
			
			if (show) {
				mScroller.startScroll(initialX, 0, initialX - mLateralBar.getWidth(), 0);
			} else {
				mScroller.startScroll(initialX, 0, initialX, 0);
			}
			
			mLastX = initialX;			
			mContainer.post(this);
		}
		
	}

}
