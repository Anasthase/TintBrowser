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
import android.view.ViewGroup;

public class ToolbarsAnimator2 {
	
	private ViewGroup mLateralBar;
	
	private Animator mAnimator;	
	
	private AnimatorListener mShowListener;
	private AnimatorListener mHideListener;
	
	private boolean mToolbarsVisible;
	
	public ToolbarsAnimator2(ViewGroup lateralBar) {
		mLateralBar = lateralBar;
		
		mShowListener = new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mAnimator = null;
				mLateralBar.requestLayout();
			}
		};
		
		mHideListener = new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mAnimator = null;
				mLateralBar.setVisibility(View.GONE);
			}
		};
		
		mToolbarsVisible = false;
		mAnimator = null;
	}
	
	public void startShowAnimation() {
		if (mAnimator != null) {
			mAnimator.end();
		}
		
		mLateralBar.setVisibility(View.VISIBLE);
		mLateralBar.setAlpha(1);
		
		AnimatorSet animator = new AnimatorSet();
		AnimatorSet.Builder b = animator.play(ObjectAnimator.ofFloat(mLateralBar, "alpha", 1));
		b.with(ObjectAnimator.ofFloat(mLateralBar, "translationX", 0));
		
		animator.addListener(mShowListener);		
		mAnimator = animator;		
		animator.start();
		
		mToolbarsVisible = true;
	}
	
	public void startHideAnimation() {
		if (mAnimator != null) {
			mAnimator.end();
		}
		
		mLateralBar.setAlpha(1);
		
		AnimatorSet animator = new AnimatorSet();
		AnimatorSet.Builder b = animator.play(ObjectAnimator.ofFloat(mLateralBar, "alpha", 1));
		
		b.with(ObjectAnimator.ofFloat(mLateralBar, "translationX", - mLateralBar.getWidth()));
		
		animator.addListener(mHideListener);		
		mAnimator = animator;
		animator.start();
		
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

}
