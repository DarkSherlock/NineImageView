package com.liang.tind.nineimageview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.liang.tind.nineimageview.util.ColorUtil;
import com.liang.tind.nineimageview.util.UIutils;
import com.liang.tind.nineimageview.widget.nineImageview.ImageAttr;
import com.liang.tind.nineimageview.widget.photoview.PhotoView;
import com.shizhefei.view.largeimage.LargeImageView;
import com.sunfusheng.glideimageview.util.DisplayUtil;

import java.util.List;

public class NineImagesDetailActivity extends AppCompatActivity implements ViewTreeObserver.OnPreDrawListener, View.OnClickListener {
    private static final String TAG = "NineImagesDetail";
    public static final String IMAGE_ATTR = "image_attr";
    public static final String CUR_POSITION = "cur_position";
    public static final int ANIM_DURATION = 300; // ms

    private RelativeLayout rootView;
    private ViewPager viewPager;
    private TextView mTvTip;
    private NineImagesDetailAdapter mAdapter;
    private List<ImageAttr> imageAttrs;
    private boolean isAnimating;

    private int curPosition;
    private int screenWidth;
    private int screenHeight;
    private float scaleX;
    private float scaleY;
    private float translationX;
    private float translationY;
    private ImageView mIvMore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_nine_images);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        mTvTip = (TextView) findViewById(R.id.tv_tip);
        rootView = (RelativeLayout) findViewById(R.id.rootView);
        mIvMore = findViewById(R.id.iv_more);
        mIvMore.setOnClickListener(this);
        screenWidth = DisplayUtil.getScreenWidth(this);
        screenHeight = DisplayUtil.getScreenHeight(this);
        Intent intent = getIntent();
        imageAttrs = (List<ImageAttr>) intent.getSerializableExtra(IMAGE_ATTR);
        curPosition = intent.getIntExtra(CUR_POSITION, 0);
        mTvTip.setText(String.format(getString(R.string.image_index), (curPosition + 1), imageAttrs.size()));

        mAdapter = new NineImagesDetailAdapter(this, imageAttrs);
        viewPager.setAdapter(mAdapter);
        viewPager.setCurrentItem(curPosition);

        viewPager.getViewTreeObserver().addOnPreDrawListener(this);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                Log.e(TAG, "onPageSelected: position==" + position);
                curPosition = position;
                mTvTip.setText(String.format(getString(R.string.image_index), (curPosition + 1), imageAttrs.size()));
                mAdapter.loadUrl(position);
            }
        });
    }

    @Override
    public void onBackPressed() {
        finishWithAnim();
    }

    private void initImageAttr(ImageAttr attr) {
        int originalWidth = attr.width;
        int originalHeight = attr.height;
        int originalCenterX = attr.left + (originalWidth >>1);
        int originalCenterY = attr.top + (originalHeight >>1);

        float widthRatio = screenWidth * 1.0f / attr.realWidth;
        int finalHeight = (int) (attr.realHeight * widthRatio);
        int finalWidth = screenWidth; //imageAttrs.size() == 1 ? screenWidth : finalHeight;

        scaleX = originalWidth * 1.0f / finalWidth;
        scaleY = originalHeight * 1.0f / finalHeight;
        translationX = originalCenterX - (screenWidth >>1);
        translationY = originalCenterY - (screenHeight >>1)- (UIutils.getStatusBarHeight(this)>>1);
    }

    @Override
    public boolean onPreDraw() {
        if (isAnimating) return true;
        rootView.getViewTreeObserver().removeOnPreDrawListener(this);
        View view = mAdapter.getLargeView(curPosition);
        ImageAttr attr = imageAttrs.get(curPosition);

        initImageAttr(attr);

        translateXAnim(view, translationX, 0);
        translateYAnim(view, translationY, 0);
        scaleXAnim(view, scaleX, 1);
        scaleYAnim(view, scaleY, 1);
        setBackgroundColor(0f, 1f, new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimating = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        mAdapter.loadUrl(curPosition);
        return true;
    }

    public void finishWithAnim() {
        if (isAnimating) return;
        View view = mAdapter.getLargeView(curPosition);
        if (view instanceof PhotoView) {
            ((PhotoView) view).setScale(1f);
        } else if (view instanceof LargeImageView) {
            ((LargeImageView) view).smoothScale(1, view.getMeasuredWidth() >> 1, view.getMeasuredHeight() >> 1);
        }
        ImageAttr attr = imageAttrs.get(curPosition);
        initImageAttr(attr);

        translateXAnim(view, 0, translationX);
        translateYAnim(view, 0, translationY);
        scaleXAnim(view, 1, scaleX);
        scaleYAnim(view, 1, scaleY);

        mTvTip.animate().alpha(0).setDuration(ANIM_DURATION);
        mIvMore.animate().alpha(0).setDuration(ANIM_DURATION);
        setBackgroundColor(1f, 0f, new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimating = false;
                finish();
                overridePendingTransition(0, 0);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
    }

    private void translateXAnim(View largeImageView, float from, float to) {
        ValueAnimator anim = ValueAnimator.ofFloat(from, to);
        anim.addUpdateListener(it -> largeImageView.setX((Float) it.getAnimatedValue()));
        anim.setDuration(ANIM_DURATION);
        anim.start();
    }

    private void translateYAnim(View largeImageView, float from, float to) {
        ValueAnimator anim = ValueAnimator.ofFloat(from, to);
        anim.addUpdateListener(it -> largeImageView.setY((Float) it.getAnimatedValue()));
        anim.setDuration(ANIM_DURATION);
        anim.start();
    }

    private void scaleXAnim(View largeImageView, float from, float to) {
        ValueAnimator anim = ValueAnimator.ofFloat(from, to);
        anim.addUpdateListener(it -> largeImageView.setScaleX((Float) it.getAnimatedValue()));
        anim.setDuration(ANIM_DURATION);
        anim.start();
    }

    private void scaleYAnim(View largeImageView, float from, float to) {
        ValueAnimator anim = ValueAnimator.ofFloat(from, to);
        anim.addUpdateListener(it -> largeImageView.setScaleY((Float) it.getAnimatedValue()));
        anim.setDuration(ANIM_DURATION);
        anim.start();
    }

    private void setBackgroundColor(float from, float to, Animator.AnimatorListener listener) {
        ValueAnimator anim = ValueAnimator.ofFloat(from, to);
        anim.addUpdateListener(it -> {
            int color = ColorUtil.evaluate((Float) it.getAnimatedValue(), Color.TRANSPARENT, Color.BLACK);
            rootView.setBackgroundColor(color);
        });
        anim.setDuration(ANIM_DURATION);
        if (listener != null) {
            anim.addListener(listener);
        }
        anim.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_more:
                mAdapter.showBottomDialog(viewPager.getCurrentItem());
                break;
        }
    }
}
