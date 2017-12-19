package com.liang.tind.nineimageview.widget.nineImageview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.StringDef;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ImageViewWrapper extends AppCompatImageView {
    private static final String TAG = "ImageViewWrapper";
    private int moreNum = 0;              //显示更多的数量
    private int maskColor = 0x88000000;   //默认的遮盖颜色
    private float textSize = 30;          //显示文字的大小单位sp
    private int textColor = 0xFFFFFFFF;   //显示文字的颜色

    private TextPaint textNumPaint;              //显示多出的图片数量文字的画笔
    private TextPaint textTypePaint;              //显示图片类型的画笔
    private String msg = "";                  //要绘制的文字

    private float typeRectPadding;
    private String type;//图片类型默认为jpg
    private Paint mTypeRectPaint;

    @StringDef({ImageType.IMAGE_TYPE_JPG, ImageType.IMAGE_TYPE_GIF, ImageType.IMAGE_TYPE_LONG})
    @Retention(RetentionPolicy.SOURCE)
    public static @interface ImageType {
        public static final String IMAGE_TYPE_JPG = "JPG";
        public static final String IMAGE_TYPE_GIF = "GIF";
        public static final String IMAGE_TYPE_LONG = "长图";//长图
    }


    public ImageViewWrapper(Context context) {
        this(context, null);
    }

    public ImageViewWrapper(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageViewWrapper(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        typeRectPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getContext().getResources().getDisplayMetrics());

        //转化单位
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSize, getContext().getResources().getDisplayMetrics());

        textNumPaint = new TextPaint();
        textNumPaint.setTextAlign(Paint.Align.CENTER);  //文字居中对齐
        textNumPaint.setAntiAlias(true);                //抗锯齿
        textNumPaint.setTextSize(textSize);             //设置文字大小
        textNumPaint.setColor(textColor);               //设置文字颜色

        setType(ImageType.IMAGE_TYPE_JPG);

        textTypePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        float textTypeSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getContext().getResources().getDisplayMetrics());
        textTypePaint.setTextSize(textTypeSize);
        textTypePaint.setColor(Color.WHITE);

        mTypeRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTypeRectPaint.setColor(Color.BLUE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(0, 0);
        if (moreNum > 0) {
            canvas.drawColor(maskColor);
            float baseY = getHeight() / 2 - (textNumPaint.ascent() + textNumPaint.descent()) / 2;
            canvas.drawText(msg, getWidth() / 2, baseY, textNumPaint);
        }
        canvas.translate(0, 0);
        if (!type.equals(ImageType.IMAGE_TYPE_JPG)) {
            Rect textRect = getTextRect(type, textTypePaint);
            float left = getWidth() - textRect.width() - (2 * typeRectPadding);
            float top = getHeight() - textRect.height() - (2 * typeRectPadding);

            canvas.drawRect(left, top, getWidth(), getHeight(), mTypeRectPaint);
            //显示中文的时候 总有一点点偏差 无法居中，这里判断=长图的时候 就额外手动调整一些 偏移量，让其显示居中；
            float offset = 0;
            if (type.equals(ImageType.IMAGE_TYPE_LONG)) {
                offset = (typeRectPadding / 5);//typeRectPadding 是5dp， 这里设置是1dp,效果刚好差不多居中
            }
            canvas.drawText(type, left + typeRectPadding - offset, getHeight() - typeRectPadding - offset, textTypePaint);
        }else {

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Drawable drawable = getDrawable();
                if (drawable != null) {
                    /**
                     * 默认情况下，所有的从同一资源（R.drawable.XXX）加载来的drawable实例都共享一个共用的状态，
                     * 如果你更改一个实例的状态，其他所有的实例都会收到相同的通知。
                     * 使用使 mutate 可以让这个drawable变得状态不定。这个操作不能还原（变为不定后就不能变为原来的状态）。
                     * 一个状态不定的drawable可以保证它不与其他任何一个drawabe共享它的状态。
                     * 此处应该是要使用的 mutate()，但是在部分手机上会出现点击后变白的现象，所以没有使用
                     * 目前这种解决方案没有问题
                     */
                    drawable.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                    ViewCompat.postInvalidateOnAnimation(this);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                Drawable drawableUp = getDrawable();
                if (drawableUp != null) {
                    drawableUp.clearColorFilter();
                    ViewCompat.postInvalidateOnAnimation(this);
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        setImageDrawable(null);
    }

    public int getMoreNum() {
        return moreNum;
    }

    public void setMoreNum(int moreNum) {
        this.moreNum = moreNum;
        msg = "+" + moreNum;
        invalidate();
    }

    public String getType() {
        return type;
    }

    public void setType(@ImageType String type) {
        this.type = type;
        invalidate();
    }

    public int getMaskColor() {
        return maskColor;
    }

    public void setMaskColor(int maskColor) {
        this.maskColor = maskColor;
        invalidate();
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
        textNumPaint.setTextSize(textSize);
        invalidate();
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        textNumPaint.setColor(textColor);
        invalidate();
    }

    public Rect getTextRect(String deviceName, Paint paint) {

        Rect mBound = new Rect();
        paint.getTextBounds(deviceName, 0, deviceName.length(), mBound);

        return mBound;
    }
}