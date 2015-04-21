/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Omada Health, Inc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.omadahealth.slidepager.lib.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.daimajia.easing.Glider;
import com.daimajia.easing.Skill;
import com.github.omadahealth.slidepager.lib.R;
import com.github.omadahealth.slidepager.lib.interfaces.OnSlidePageChangeListener;
import com.github.omadahealth.slidepager.lib.interfaces.OnSlideListener;
import com.github.omadahealth.slidepager.lib.utils.ProgressAttr;
import com.github.omadahealth.typefaceview.TypefaceTextView;
import com.github.omadahealth.typefaceview.TypefaceType;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by stoyan on 4/7/15.
 */
public class SlideView extends LinearLayout {
    /**
     * The tag for logging
     */
    private static final String TAG = "SlideView";

    /**
     * The duration for the animation for {@link #mSelectedImageView}
     */
    private static final int SELECTION_ANIMATION_DURATION = 500;

    /**
     * An array that holds all the {@link ProgressView} for this layout
     */
    private List<ProgressView> mProgressList = new ArrayList<>(7);

    /**
     * The left textview
     */
    private TypefaceTextView mLeftTextView;

    /**
     * The right textview
     */
    private TypefaceTextView mRightTextView;

    /**
     * True of we want to show {@link #mLeftTextView}
     */
    private boolean mShowLeftText;

    /**
     * True of we want to show {@link #mRightTextView}
     */
    private boolean mShowRightText;

    /**
     * The current day sliding {@link android.widget.ImageView} we display
     * below the currently selected {@link ProgressView} from {@link #mProgressList}
     */
    private SelectedImageView mSelectedImageView;

    /**
     * The callback listener for when views are clicked
     */
    private OnSlideListener mCallback;

    /**
     * The default animation time
     */
    private static final int DEFAULT_PROGRESS_ANIMATION_TIME = 1000;

    /**
     * The xml attributes of this view
     */
    private TypedArray mAttributes;

    /**
     * The {@link AnimatorSet} used to animate the Slider selected day
     */
    private AnimatorSet mAnimationSet;

    /**
     * The int tag of the selected {@link ProgressView} we store so that we can
     * translate the {@link #mSelectedImageView} to the same day when we transition between
     * different instances of this class
     */
    private static int mSelectedView;

    /**
     * The position of this page within the {@link com.github.omadahealth.slidepager.lib.SlidePager}
     */
    private int mPagePosition;

    /**
     * The text for the {@link #mLeftTextView}
     */
    private String mLeftText;

    /**
     * The text for the {@link #mRightTextView}
     */
    private String mRightText;

    /**
     * The animation time in milliseconds that we animate the progress
     */
    private int mProgressAnimationTime = DEFAULT_PROGRESS_ANIMATION_TIME;

    /**
     * A user defined {@link ViewPager.OnPageChangeListener}
     */
    private OnSlidePageChangeListener mUserPageListener;

    public SlideView(Context context) {
        this(context, null, -1, null, null, null);
    }

    public SlideView(Context context, TypedArray attributes, int pagePosition, OnSlidePageChangeListener pageListener, String leftText, String rightText) {
        super(context, null);
        this.mLeftText = leftText;
        this.mRightText = rightText;
        init(context, attributes, pagePosition, pageListener);
    }

    private void loadStyledAttributes(TypedArray attributes) {
        mAttributes = attributes;
        if (mAttributes != null) {
            mShowLeftText = attributes.getBoolean(R.styleable.SlidePager_slide_show_week, true);
            mShowRightText = attributes.getBoolean(R.styleable.SlidePager_slide_show_date, true);

            mLeftTextView.setVisibility(mShowLeftText && mLeftText != null ? VISIBLE : GONE);
            mRightTextView.setVisibility(mShowRightText && mRightText != null? VISIBLE : GONE);

            if(mShowLeftText && mLeftTextView != null && mLeftText != null){
                mLeftTextView.setText(mLeftText, true);
            }

            if(mShowRightText && mRightTextView != null && mRightText != null){
                mRightTextView.setText(mRightText);
            }
        }
    }

    /**
     * Initiate the view and start butterknife injection
     *
     * @param context
     */
    private void init(Context context, TypedArray attributes, int pagePosition, OnSlidePageChangeListener pageListener) {
        if (!isInEditMode()) {
            this.mPagePosition = pagePosition;
            this.mUserPageListener = pageListener;

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.view_slide, this);
            ButterKnife.inject(this, view);

            mAnimationSet = new AnimatorSet();
            injectViews();
            setListeners();
            loadStyledAttributes(attributes);
        }
    }

    /**
     * Inject the views into {@link #mProgressList}
     */
    private void injectViews() {
        mProgressList.add(ButterKnife.<ProgressView>findById(this, R.id.progress_1).loadStyledAttributes(mAttributes, mUserPageListener != null ? mUserPageListener.getDayProgress(mPagePosition, 0) : null));
        mProgressList.add(ButterKnife.<ProgressView>findById(this, R.id.progress_2).loadStyledAttributes(mAttributes, mUserPageListener != null ? mUserPageListener.getDayProgress(mPagePosition, 1) : null));
        mProgressList.add(ButterKnife.<ProgressView>findById(this, R.id.progress_3).loadStyledAttributes(mAttributes, mUserPageListener != null ? mUserPageListener.getDayProgress(mPagePosition, 2) : null));
        mProgressList.add(ButterKnife.<ProgressView>findById(this, R.id.progress_4).loadStyledAttributes(mAttributes, mUserPageListener != null ? mUserPageListener.getDayProgress(mPagePosition, 3) : null));
        mProgressList.add(ButterKnife.<ProgressView>findById(this, R.id.progress_5).loadStyledAttributes(mAttributes, mUserPageListener != null ? mUserPageListener.getDayProgress(mPagePosition, 4) : null));
        mProgressList.add(ButterKnife.<ProgressView>findById(this, R.id.progress_6).loadStyledAttributes(mAttributes, mUserPageListener != null ? mUserPageListener.getDayProgress(mPagePosition, 5) : null));
        mProgressList.add(ButterKnife.<ProgressView>findById(this, R.id.progress_7).loadStyledAttributes(mAttributes, mUserPageListener != null ? mUserPageListener.getDayProgress(mPagePosition, 6) : null));

        mLeftTextView = ButterKnife.findById(this, R.id.left_textview);
        mRightTextView = ButterKnife.findById(this, R.id.right_textview);

        mLeftTextView.setTypeface(TypefaceTextView.getFont(getContext(), TypefaceType.ROBOTO_LIGHT.getAssetFileName()));
        mRightTextView.setTypeface(TypefaceTextView.getFont(getContext(), TypefaceType.ROBOTO_LIGHT.getAssetFileName()));

        mSelectedImageView = ButterKnife.findById(this, R.id.selected_day_image_view);
        mSelectedImageView.setSelectedViewId(mProgressList.get(SlideView.getSelectedView()).getId());
    }

    /**
     * Animates the translation of the {@link #mSelectedImageView}
     *
     * @param view          The view to use to set the animation position
     * @param startPosition The starting x position for the animated view
     */
    public void animateSelectedTranslation(final View view, float startPosition) {
        final Float offset = -1 * this.getWidth() + view.getWidth() / 2 + view.getX();
        mSelectedImageView.setTag(R.id.selected_day_image_view, offset);
        mSelectedImageView.setSelectedViewId(view.getId());

        mAnimationSet = new AnimatorSet();
        mAnimationSet.playSequentially(Glider.glide(Skill.QuadEaseInOut, SELECTION_ANIMATION_DURATION, ObjectAnimator.ofFloat(mSelectedImageView, "x", startPosition, offset)));
        mAnimationSet.setDuration(SELECTION_ANIMATION_DURATION);
        mAnimationSet.removeAllListeners();
        mAnimationSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                toggleSelectedViews(((ProgressView) view).getIntTag());
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mAnimationSet.start();
    }

    /**
     * Calls {@link #animateSelectedTranslation(View, float)} with the start
     * position set to the {@link #mSelectedImageView#getX()}
     *
     * @param view The view to use to set the animation position
     */
    public void animateSelectedTranslation(View view) {
        animateSelectedTranslation(view, mSelectedImageView.getX());
    }

    /**
     * Set up listeners for all the views in {@link #mProgressList}
     */
    private void setListeners() {
        for (final ProgressView progressView : mProgressList) {
            if (progressView != null) {

                progressView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int index = progressView.getIntTag();

                        toggleSelectedViews(index);

                        animateSelectedTranslation(view);
                        if (mCallback != null) {
                            mCallback.onDaySelected(mPagePosition, index);
                        }
                    }
                });
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void animatePage(OnSlidePageChangeListener listener, TypedArray attributes) {
        final List<View> children = (List<View>) getTag();
        if (children != null) {
            for (final View child : children) {
                if (child instanceof ProgressView) {
                    ((ProgressView) child).loadStyledAttributes(attributes, listener.getDayProgress(mPagePosition, ((ProgressView) child).getIntTag()));
                    animateProgress((ProgressView) child, children, listener);
                    animateSelectedTranslation(mProgressList.get(mSelectedView));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void animateSeries(boolean show) {
        final List<View> children = (List<View>) getTag();
        if (children != null) {
            for (final View child : children) {
                if (child instanceof ProgressView) {
                    final ProgressView progressView = (ProgressView) child;
                    progressView.showStreak(show, ProgressView.STREAK.RIGHT_STREAK);
                    progressView.showStreak(show, ProgressView.STREAK.LEFT_STREAK);
                    progressView.showCheckMark(show);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void resetPage(TypedArray mAttributes) {
        this.setVisibility(View.VISIBLE);
        this.setAlpha(1f);

        loadStyledAttributes(mAttributes);
        animateSeries(false);
        getSelectedImageView().resetView();
        final List<View> children = (List<View>) getTag();
        if (children != null) {
            for (final View child : children) {
                if (child instanceof ProgressView) {
                    ProgressView progressView = (ProgressView) child;
                    progressView.reset();
                }
            }
        }
    }

    private void animateProgress(ProgressView view, List<View> children, OnSlidePageChangeListener listener) {
        if (listener != null) {
            ProgressAttr progress = listener.getDayProgress(mPagePosition, view.getIntTag());
            view.animateProgress(0, progress, mProgressAnimationTime, children);
        }
    }

    private void toggleSelectedViews(int selected) {
        mSelectedView = selected;
        for (ProgressView day : mProgressList) {
            if (day.getIntTag() == mSelectedView) {
                day.isSelected(true);
                continue;
            }
            day.isSelected(false);
        }
    }

    /**
     * Sets the listener for click events in this view
     *
     * @param listener
     */
    public void setListener(OnSlideListener listener) {
        this.mCallback = listener;
    }

    public SelectedImageView getSelectedImageView() {
        return mSelectedImageView;
    }

    public synchronized static int getSelectedView() {
        return mSelectedView;
    }

    public synchronized static void setSelectedView(int selectedView) {
        SlideView.mSelectedView = selectedView;
    }
}