package com.github.omadahealth.slidepager;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;

import com.github.omadahealth.demo.R;
import com.github.omadahealth.slidepager.lib.SlidePager;
import com.nineoldandroids.animation.Animator;

import java.util.List;

public class MainActivity extends ActionBarActivity {

    private SlidePager mSlidePager;
    
    private String[] days = {"S", "M", "T", "W", "T", "F", "S"};

    /**
     * The animation time in milliseconds that we take to display the steps taken
     */
    private static final int BAR_ANIMATION_TIME = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mSlidePager = (SlidePager) findViewById(R.id.slidepager);
        View[] demoViews = initDummyViews();
        mSlidePager.setAdapter(new DemoPagerAdapter(demoViews));
        mSlidePager.setPageTransformer(false, new SlideTransformerImpl());
        mSlidePager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override

            public void onPageSelected(final int position) {
                animatePage(position);
            }

            @Override
            @SuppressWarnings("unchecked")
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    View selectedView = ((DemoPagerAdapter) mSlidePager.getAdapter()).getCurrentView(mSlidePager.getCurrentItem());

                    List<View> children = (List<View>) selectedView.getTag();
                    animateSeries(children);
                }
                if (state == ViewPager.SCROLL_STATE_SETTLING) {
                    onPageSelected(mSlidePager.getCurrentItem());
                }
            }
        });
        animatePage(0);
    }



    @SuppressWarnings("unchecked")
    private void animatePage(final int position) {
        View selectedView = ((DemoPagerAdapter) mSlidePager.getAdapter()).getCurrentView(mSlidePager.getCurrentItem());
        List<View> children = (List<View>) selectedView.getTag();
        if (children != null) {
            int i = 0;
            for (final View child : children) {
                if (child instanceof DayProgressView) {
                    final DayProgressView dayProgressView = (DayProgressView) child;
                    dayProgressView.getDayOfWeek().setText(days[i]);
                    dayProgressView.addAnimationListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (position == mSlidePager.getCurrentItem() && dayProgressView.getCircularBar().getProgress() >= 99.95f) {
                                switch (dayProgressView.getId()) {
                                    case R.id.day_progress_3:
                                        dayProgressView.show(true, DayProgressView.STREAK.RIGHT_STREAK);
                                        break;
                                    case R.id.day_progress_4:
                                        dayProgressView.show(true, DayProgressView.STREAK.LEFT_STREAK);
                                        dayProgressView.show(true, DayProgressView.STREAK.RIGHT_STREAK);
                                        break;
                                    case R.id.day_progress_5:
                                        dayProgressView.show(true, DayProgressView.STREAK.LEFT_STREAK);
                                        break;
                                }
                            }
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {
                        }
                    });

                    animateProgress(dayProgressView, i);


                    i++;
                }
            }
        }
    }

    private void animateProgress(DayProgressView view, int i) {
        switch (view.getId()) {
            case R.id.day_progress_3:
            case R.id.day_progress_4:
            case R.id.day_progress_5:
                view.animateProgress(0, 100, BAR_ANIMATION_TIME);
                break;
            default:
                view.animateProgress(0, (25 * i), BAR_ANIMATION_TIME);
                view.getCircularBar().setCircleFillColor(getResources().getColor(android.R.color.transparent));
                break;
        }
    }

    private void animateSeries(List<View> children) {
        if (children != null) {
            for (final View child : children) {
                if (child instanceof DayProgressView) {
                    final DayProgressView dayProgressView = (DayProgressView) child;
                    dayProgressView.show(false, DayProgressView.STREAK.RIGHT_STREAK);
                    dayProgressView.show(false, DayProgressView.STREAK.LEFT_STREAK);
                }
            }
        }
    }

    private View[] initDummyViews() {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View[] views = new View[4];
        views[0] = inflater.inflate(R.layout.page, null);
        views[1] = inflater.inflate(R.layout.page, null);
        views[2] = inflater.inflate(R.layout.page, null);
        views[3] = inflater.inflate(R.layout.page, null);
        return views;
    }
}
