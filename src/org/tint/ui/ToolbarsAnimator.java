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
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ToolbarsAnimator {
	
	private LinearLayout mTopBar;
	private LinearLayout mBottomBar;
	private ImageView mShowPreviousTab;
	private ImageView mShowNextTab;
	
	private Animator mAnimator;	
	
	private AnimatorListener mShowListener;
	private AnimatorListener mHideListener;
	
	private boolean mToolbarsVisible;
	
	public ToolbarsAnimator(LinearLayout topBar, LinearLayout bottomBar, ImageView showPreviousTab, ImageView showNextTab) {
		mTopBar = topBar;
		mBottomBar = bottomBar;
		mShowPreviousTab = showPreviousTab;
		mShowNextTab = showNextTab;
		
		mShowListener = new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mAnimator = null;
				mTopBar.requestLayout();
				mBottomBar.requestLayout();				
				mShowPreviousTab.requestLayout();
				mShowNextTab.requestLayout();
			}		
		};
		
		mHideListener = new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mAnimator = null;
				mTopBar.setVisibility(View.GONE);
				mBottomBar.setVisibility(View.GONE);
				mShowPreviousTab.setVisibility(View.GONE);
				mShowNextTab.setVisibility(View.GONE);
			}
		};
		
		mToolbarsVisible = true;
		mAnimator = null;
	}	
	
	public void startShowAnimation(boolean showPreviousTabButton, boolean showNextTabButton) {
		if (mAnimator != null) {
			mAnimator.end();
		}
				
		mTopBar.setVisibility(View.VISIBLE);
		mBottomBar.setVisibility(View.VISIBLE);
		
		if (showPreviousTabButton) {
			mShowPreviousTab.setVisibility(View.VISIBLE);
		}
		
		if (showNextTabButton) {
			mShowNextTab.setVisibility(View.VISIBLE);
		}
		
		mTopBar.setAlpha(1);
		mBottomBar.setAlpha(1);
		
		AnimatorSet animator = new AnimatorSet();
		AnimatorSet.Builder b = animator.play(ObjectAnimator.ofFloat(mBottomBar, "alpha", 1));
		b.with(ObjectAnimator.ofFloat(mTopBar, "alpha", 1));
		
		//mTopBar.setTranslationY(- mTopBar.getHeight());
		mBottomBar.setTranslationY(mBottomBar.getHeight());
		
		b.with(ObjectAnimator.ofFloat(mTopBar, "translationY", 0));
		b.with(ObjectAnimator.ofFloat(mBottomBar, "translationY", 0));		
		
		if (showPreviousTabButton) {
			b.with(ObjectAnimator.ofFloat(mShowPreviousTab, "translationX", 0));
		}
		
		if (showNextTabButton) {
			b.with(ObjectAnimator.ofFloat(mShowNextTab, "translationX", mShowNextTab.getWidth(), 0));
		}
		
		animator.addListener(mShowListener);
		
		mAnimator = animator;
		
		animator.start();
		
		mToolbarsVisible = true;
	}
	
	public void startHideAnimation() {
		if (mAnimator != null) {
			mAnimator.end();
		}
		
		mTopBar.setAlpha(1);
		mBottomBar.setAlpha(1);
		
		AnimatorSet animator = new AnimatorSet();
		AnimatorSet.Builder b = animator.play(ObjectAnimator.ofFloat(mBottomBar, "alpha", 1));
		b.with(ObjectAnimator.ofFloat(mBottomBar, "translationY", 0, mBottomBar.getHeight()));
		
		b.with(ObjectAnimator.ofFloat(mTopBar, "alpha", 1));
		b.with(ObjectAnimator.ofFloat(mTopBar, "translationY", 0, - mTopBar.getHeight()));
		
		b.with(ObjectAnimator.ofFloat(mShowPreviousTab, "translationX", - mShowPreviousTab.getWidth()));
		b.with(ObjectAnimator.ofFloat(mShowNextTab, "translationX", 0, mShowNextTab.getWidth()));
		
		animator.addListener(mHideListener);
		
		mAnimator = animator;
		
		animator.start();
		
		mToolbarsVisible = false;
	}
	
	public boolean isToolbarsVisible() {
		return mToolbarsVisible;
	}

}
