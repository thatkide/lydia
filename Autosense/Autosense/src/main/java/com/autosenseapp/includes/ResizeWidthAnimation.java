package com.autosenseapp.includes;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by eric on 2014-07-19.
 */
public class ResizeWidthAnimation extends Animation
{
	private int mWidth;
	private int mStartWidth;
	private View mView;

	public ResizeWidthAnimation(View view, int width) {
		mView = view;
		mWidth = width;
		mStartWidth = view.getWidth();
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		mView.getLayoutParams().width = mStartWidth + (int) ((mWidth - mStartWidth) * interpolatedTime);
		mView.requestLayout();
	}

	@Override
	public void initialize(int width, int height, int parentWidth, int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);
	}

	@Override
	public boolean willChangeBounds()
	{
		return true;
	}
}
