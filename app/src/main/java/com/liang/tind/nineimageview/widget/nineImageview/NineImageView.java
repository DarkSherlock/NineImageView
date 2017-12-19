package com.liang.tind.nineimageview.widget.nineImageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.liang.tind.nineimageview.R;
import com.liang.tind.nineimageview.util.UIutils;
import com.sunfusheng.glideimageview.GlideImageLoader;

import java.util.ArrayList;
import java.util.List;

public class NineImageView extends ViewGroup {
    private static final String TAG = "NineImageView";
    public static final int MODE_GRID = 0;          //网格模式，4张图2X2布局
    public static final int MODE_FILL = 1;          //填充模式，类似于微信

    private int singleImageWidth;
    private int singleImageHeight;
    private int singleImageMaxWidth;
    private int singleImageMaxHeight;
    private int singleImageMinWidth;

    private int maxImageSize = 9;                   // 最大显示的图片数
    private int gridSpacing = 5;                    // 宫格间距，单位dp
    private int mode = MODE_GRID;                   // 默认使用GRID模式

    private int columnCount;    // 列数
    private int rowCount;       // 行数
    private int gridWidth;      // 宫格宽度
    private int gridHeight;     // 宫格高度

    private List<ImageViewWrapper> imageViews;
    private List<ImageAttr> imageAttrs = new ArrayList<>();
    private NineImageViewAdapter mAdapter;
    public static int mTextureMaxSize;
    private int[] mScreenSize;
    private boolean mIsSetedSingleSize;

    public NineImageView(Context context) {
        this(context, null);
    }

    public NineImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NineImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        //不同的手机设备能显示的最大尺寸不一样
        mTextureMaxSize = UIutils.getMaximumTextureSize();
        imageViews = new ArrayList<>();
        //手机屏幕高度
        mScreenSize = UIutils.getScreenSize(getContext());

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.NineImageView);

        maxImageSize = typedArray.getInt(R.styleable.NineImageView_ngv_maxSize, maxImageSize);
        mode = typedArray.getInt(R.styleable.NineImageView_ngv_mode, mode);

        gridSpacing = (int) typedArray.getDimension(R.styleable.NineImageView_ngv_gridSpacing, gridSpacing);
        singleImageWidth = (int) typedArray.getDimension(R.styleable.NineImageView_ngv_singleImageWidth, 0);
        singleImageHeight = (int) typedArray.getDimension(R.styleable.NineImageView_ngv_singleImageHeight, 0);

        if (singleImageWidth != 0 || singleImageHeight != 0) {
            mIsSetedSingleSize = true;
        }

        typedArray.recycle();
    }

    private void initSingleImageSize(int width) {
        if (singleImageMaxWidth == 0) {
            singleImageMaxWidth = width * 2 / 3;
            singleImageMinWidth = width / 3;
            singleImageMaxHeight = width / 2;
            setMaxOrMinSize();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.e(TAG, "onMeasure: ");
        int width;
        int height;
        width = MeasureSpec.getSize(widthMeasureSpec);
        int totalWidth = width - getPaddingLeft() - getPaddingRight();
        initSingleImageSize(totalWidth);
        if (imageAttrs.size() == 0) {
            width = singleImageMinWidth;
            height = singleImageMinWidth;
        } else {
            if (imageAttrs.size() == 1) {
                gridWidth = singleImageWidth;
                gridHeight = singleImageHeight;
            } else if (imageAttrs.size() == 2) {
                gridWidth = gridHeight = (totalWidth - gridSpacing) / 2;
            } else {
                gridWidth = gridHeight = (totalWidth - gridSpacing * 2) / 3;
            }
            Log.e(TAG, "onMeasure: gridWidth ==" + gridWidth + "gridHeight==" + gridHeight);
            width = gridWidth * columnCount + gridSpacing * (columnCount - 1) + getPaddingLeft() + getPaddingRight();
            height = gridHeight * rowCount + gridSpacing * (rowCount - 1) + getPaddingTop() + getPaddingBottom();
        }

        Log.e(TAG, "onMeasure: width == " + width + ", height==" + height);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.e(TAG, "onLayout: ");
        layoutChildrenView();
    }

    private void layoutChildrenView() {
        if (imageAttrs == null) return;
        int childrenCount = imageAttrs.size();
        for (int i = 0; i < childrenCount; i++) {
            ImageView imageView = (ImageView) getChildAt(i);
            ImageAttr imageAttr = imageAttrs.get(i);
            if (imageView == null) continue;

            int rowNum = i / columnCount;
            int columnNum = i % columnCount;
            int left = (gridWidth + gridSpacing) * columnNum + getPaddingLeft();
            int top = (gridHeight + gridSpacing) * rowNum + getPaddingTop();
            int right = left + gridWidth;
            int bottom = top + gridHeight;
            imageView.layout(left, top, right, bottom);

            loadImage(imageView, imageAttr, childrenCount);
        }
    }

    private void loadImage(ImageView imageView, ImageAttr attr, int count) {
//        避免view 多次触发onLayout=》layoutChildrenView=》loadImage()造成重复下载问题;
        if (imageView.getDrawable() == null) {
            String url = TextUtils.isEmpty(attr.thumbnailUrl) ? attr.url : attr.thumbnailUrl;
            if (url.endsWith(".gif") && imageView instanceof ImageViewWrapper) {
                ((ImageViewWrapper) imageView).setType(ImageViewWrapper.ImageType.IMAGE_TYPE_GIF);
            }

            GlideImageLoader imageLoader = GlideImageLoader.create(imageView);

            RequestOptions requestOptions = imageLoader.requestOptions(R.color.placeholder)
                    .centerCrop()
                    .skipMemoryCache(false)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);

            RequestBuilder<Drawable> requestBuilder = imageLoader.requestBuilder(url, requestOptions);
            requestBuilder.transition(DrawableTransitionOptions.withCrossFade())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Toast.makeText(getContext(), "下载网络图片失败...", Toast.LENGTH_SHORT).show();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            Log.e(TAG, "RequestListener  onResourceReady: ");
                            return false;
                        }
                    })
                    .into(new SimpleTarget<Drawable>() {
                        @Override
                        public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                            Log.e(TAG, "SimpleTarget onResourceReady: ");
                            String tagUrl = (String) imageView.getTag(R.id.tag_id);
                            if (!TextUtils.isEmpty(tagUrl) && tagUrl.equals(url)) {
                                int intrinsicHeight = resource.getIntrinsicHeight();
                                int intrinsicWidth = resource.getIntrinsicWidth();

                                if (intrinsicHeight > mTextureMaxSize) {
                                    intrinsicHeight = mTextureMaxSize;
                                }
                                if (intrinsicWidth > mTextureMaxSize) {
                                    intrinsicWidth = mTextureMaxSize;
                                }
                                if (count == 1) {
                                    setSingleImageWidthHeight(intrinsicWidth, intrinsicHeight);
                                }
                                if (intrinsicWidth != resource.getIntrinsicHeight() || intrinsicHeight != resource.getIntrinsicWidth()) {
                                    requestBuilder.clone().apply(requestOptions.override(intrinsicWidth, intrinsicHeight)).into(imageView);
                                } else {
                                    requestBuilder.into(imageView);
                                }
                                attr.realWidth = intrinsicWidth;
                                attr.realHeight = intrinsicHeight;

                                if (intrinsicHeight > mScreenSize[1] && imageView instanceof ImageViewWrapper) {
                                    ((ImageViewWrapper) imageView).setType(ImageViewWrapper.ImageType.IMAGE_TYPE_LONG);
                                    attr.isLongImage = true;
                                }
                            }
                        }
                    });
        }
    }

    private void setSingleImageWidthHeight(int widthSize, int heightSize) {
        if (mIsSetedSingleSize) {
            if (singleImageHeight == 0) {
                singleImageHeight = singleImageWidth;
            } else if (singleImageWidth == 0) {
                singleImageWidth = singleImageHeight;
            }
        } else {
            float ratio = widthSize * 1.0f / heightSize;

            if (widthSize > singleImageMaxWidth || ratio > 2.1f) {
                singleImageWidth = singleImageMaxWidth;
                singleImageHeight = (int) (singleImageMaxWidth * heightSize * 1.0f / widthSize);
            } else if (widthSize < singleImageMinWidth || ratio < 0.7f) {
                if (ratio < 0.3f) {
                    singleImageWidth = singleImageMinWidth / 2;
                    singleImageHeight = (int) (singleImageMinWidth / 2 * heightSize * 1.0f / widthSize);

                } else {
                    singleImageWidth = singleImageMinWidth;
                    singleImageHeight = (int) (singleImageMinWidth * heightSize * 1.0f / widthSize);
                }

            } else {
                singleImageWidth = widthSize;
                singleImageHeight = heightSize;
            }

        }

        setMaxOrMinSize();
    }

    private void setMaxOrMinSize() {
        if (singleImageHeight > singleImageMaxHeight) {
            singleImageHeight = singleImageMaxHeight;
        }

        if (singleImageWidth > singleImageMaxWidth) {
            singleImageWidth = singleImageMaxWidth;
        }

        if (singleImageHeight < singleImageMinWidth) {
            singleImageHeight = singleImageMinWidth;
        }

        if (singleImageWidth < singleImageMinWidth) {
            singleImageWidth = singleImageMinWidth;
        }
    }


    public void setAdapter(@NonNull NineImageViewAdapter adapter) {
        this.mAdapter = adapter;
        List<ImageAttr> attrList = adapter.getImageAttrs();
        if (attrList == null || attrList.isEmpty()) {
            setVisibility(GONE);
            return;
        }
        setVisibility(VISIBLE);

        int imageCount = attrList.size();
        if (maxImageSize > 0 && imageCount > maxImageSize) {
            attrList = attrList.subList(0, maxImageSize);
            imageCount = attrList.size(); // 再次获取图片数量
        }

        // 默认是3列显示，行数根据图片的数量决定
        rowCount = imageCount / 3 + (imageCount % 3 == 0 ? 0 : 1);
        columnCount = 3;
        // Grid模式下，显示4张使用2X2模式
        if (mode == MODE_GRID) {
            if (imageCount == 4) {
                rowCount = 2;
                columnCount = 2;
            }
        }

        // 保证View的复用，避免重复创建
        if (imageAttrs == null) {
            for (int i = 0; i < imageCount; i++) {
                ImageViewWrapper imageView = getImageView(i);
                addView(imageView, generateDefaultLayoutParams());
            }
        } else {
            int oldViewCount = imageAttrs.size();
            if (oldViewCount > imageCount) {
                removeViews(imageCount, oldViewCount - imageCount);
            } else if (oldViewCount < imageCount) {
                for (int i = oldViewCount; i < imageCount; i++) {
                    ImageViewWrapper imageView = getImageView(i);
                    addView(imageView, generateDefaultLayoutParams());
                }
            }
        }
        for (int i = 0; i < attrList.size(); i++) {
            ImageViewWrapper imageViewWrapper = imageViews.get(i);
            imageViewWrapper.setType(ImageViewWrapper.ImageType.IMAGE_TYPE_JPG);
            imageViewWrapper.setImageDrawable(null);//重新下载 重置bitmap
            ImageAttr attr = attrList.get(i);
            attr.isLongImage = false;
            String url = TextUtils.isEmpty(attr.thumbnailUrl) ? attr.url : attr.thumbnailUrl;
            imageViewWrapper.setTag(R.id.tag_id, url);//设置tag 避免加载错乱的bitmap
        }
        // 修改最后一个条目，决定是否显示更多
        if (adapter.getImageAttrs().size() > maxImageSize) {
            View child = getChildAt(maxImageSize - 1);
            if (child instanceof ImageViewWrapper) {
                ImageViewWrapper imageView = (ImageViewWrapper) child;
                imageView.setMoreNum(adapter.getImageAttrs().size() - maxImageSize);
            }
        }
        imageAttrs.clear();
        imageAttrs.addAll(attrList);
        layoutChildrenView();
    }

    // 获得ImageView，并保证ImageView的重用
    private ImageViewWrapper getImageView(int position) {
        ImageViewWrapper imageView = null;
        if (position < imageViews.size()) {
            imageView = imageViews.get(position);
        }
        if (imageView == null) {
            imageView = mAdapter.generateImageView(getContext());
            imageView.setOnClickListener(v -> mAdapter.onImageItemClick(getContext(), NineImageView.this, position, mAdapter.getImageAttrs()));
            imageViews.add(imageView);
        }

        return imageView;
    }

    public NineImageViewAdapter getAdapter() {
        return mAdapter;
    }

    public void setGridSpacing(int spacing) {
        gridSpacing = spacing;
    }

    public void setMaxSize(int maxSize) {
        maxImageSize = maxSize;
    }

    public int getMaxSize() {
        return maxImageSize;
    }

}
