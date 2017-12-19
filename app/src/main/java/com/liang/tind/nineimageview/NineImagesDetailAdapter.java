package com.liang.tind.nineimageview;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.liang.tind.nineimageview.widget.nineImageview.ImageAttr;
import com.liang.tind.nineimageview.widget.photoview.OnOutsidePhotoTapListener;
import com.liang.tind.nineimageview.widget.photoview.OnPhotoTapListener;
import com.liang.tind.nineimageview.widget.photoview.PhotoView;
import com.shizhefei.view.largeimage.LargeImageView;
import com.shizhefei.view.largeimage.factory.FileBitmapDecoderFactory;
import com.sunfusheng.glideimageview.GlideImageLoader;
import com.sunfusheng.glideimageview.progress.CircleProgressView;
import com.sunfusheng.glideimageview.util.DisplayUtil;

import java.io.File;
import java.util.List;

public class NineImagesDetailAdapter extends PagerAdapter implements OnPhotoTapListener, OnOutsidePhotoTapListener, View.OnLongClickListener, View.OnClickListener {
    private static final String TAG = "NineImagesDetailAdapter";
    private Context mContext;
    private LayoutInflater mInflater;
    private List<ImageAttr> images;
    private SparseArray<View> mViews = new SparseArray<>();
    private SparseArray<CircleProgressView> progressViews = new SparseArray<>();
    private Dialog mBottomDialog;

    public NineImagesDetailAdapter(Context context, @NonNull List<ImageAttr> images) {
        super();
        this.mContext = context;
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.images = images;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
    }

    public View getLargeView(int index) {
        return mViews.get(index);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = mInflater.inflate(R.layout.item_photoview, container, false);
        ImageAttr attr = images.get(position);
        CircleProgressView progressView = (CircleProgressView) view.findViewById(R.id.progressView);
        PhotoView photoView = (PhotoView) view.findViewById(R.id.photoView);
        LargeImageView largeImageView = view.findViewById(R.id.largeImageView);
        if (attr.isLongImage){
            largeImageView.setTag(R.id.tag_id, position);
            photoView.setVisibility(View.GONE);
            largeImageView.setVisibility(View.VISIBLE);
            largeImageView.setOnClickListener(this);
            largeImageView.setTag( R.id.tag_id, position);
            mViews.put(position, largeImageView);
        }else {
            largeImageView.setVisibility(View.GONE);
            photoView.setVisibility(View.VISIBLE);
            photoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            photoView.setOnPhotoTapListener(this);
            photoView.setOnOutsidePhotoTapListener(this);
            photoView.setOnLongClickListener(this);
            photoView.setTag( R.id.tag_id,position);
            mViews.put(position, photoView);
        }

        progressViews.put(position, progressView);
        container.addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return view;
    }

    public void loadUrl(int position) {
        ImageAttr attr = images.get(position);
        String url = attr.isUrlEnable ? attr.url : attr.compressUrl;
        View view = mViews.get(position);
        CircleProgressView progressView = progressViews.get(position);
        progressView.setProgress(0);
        progressView.setVisibility(View.VISIBLE);
        if (attr.isLongImage) {
            Glide.with(mContext).downloadOnly().load(url).into(new SimpleTarget<File>() {
                @Override
                public void onResourceReady(File resource, Transition<? super File> transition) {
                    ((LargeImageView) view).setImage(new FileBitmapDecoderFactory(resource));
                }
            });
        } else {
            PhotoView photoView = (PhotoView) view;
            GlideImageLoader imageLoader = GlideImageLoader.create( photoView);
            imageLoader.setOnGlideImageViewListener(url, (percent, isDone, exception) -> {
                Log.e(TAG, "loadUrl: percent==" + percent + "isDone==" + isDone);
                progressView.setProgress(percent);
                progressView.setVisibility(isDone ? View.GONE : View.VISIBLE);
            });
            RequestOptions requestOptions = imageLoader.requestOptions(R.color.placeholder)
                    .centerCrop()
                    .skipMemoryCache(false)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
            RequestBuilder<Drawable> requestBuilder = imageLoader.requestBuilder(url, requestOptions)
                    .transition(DrawableTransitionOptions.withCrossFade());
            requestBuilder.into(new SimpleTarget<Drawable>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                @Override
                public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                    if (resource.getIntrinsicHeight() > DisplayUtil.getScreenHeight(mContext)) {
                        photoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }
                    requestBuilder.into(photoView);
                }
            });
        }


    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public void onPhotoTap(ImageView view, float x, float y) {
        ((NineImagesDetailActivity) mContext).finishWithAnim();
    }

    @Override
    public void onOutsidePhotoTap(ImageView imageView) {
        ((NineImagesDetailActivity) mContext).finishWithAnim();
    }

    @Override
    public boolean onLongClick(View v) {
        int position = (int) v.getTag( R.id.tag_id);
        showBottomDialog(position);
        return true;
    }

    public void showBottomDialog(int position) {
        mBottomDialog = new Dialog(mContext, R.style.ActionSheetDialogStyle);

        View contentView = LayoutInflater.from(mContext).inflate(R.layout.dialog_content_view_nine_image_detail, null);
        TextView tvLoadUrl = (TextView) contentView.findViewById(R.id.tv_load_url);
        TextView tvCancel = (TextView) contentView.findViewById(R.id.tv_cancel);

        tvLoadUrl.setOnClickListener(this);
        tvCancel.setOnClickListener(this);

        tvLoadUrl.setTag(R.id.tag_id,position);
        tvCancel.setTag(R.id.tag_id,position);

        mBottomDialog.setContentView(contentView);

        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
        layoutParams.width = mContext.getResources().getDisplayMetrics().widthPixels;
        contentView.setLayoutParams(layoutParams);

        mBottomDialog.getWindow().setGravity(Gravity.BOTTOM);
        mBottomDialog.show();
    }

    @Override
    public void onClick(View v) {
        int position = (int) v.getTag( R.id.tag_id);
        switch (v.getId()) {
            case R.id.tv_load_url:
                images.get(position).setUrlEnable(true);
                loadUrl(position);
                dismissBottomDialog();
                break;
            case R.id.tv_cancel:
                dismissBottomDialog();
                break;
            case R.id.largeImageView:
                if (mContext instanceof NineImagesDetailActivity){
                    ((NineImagesDetailActivity) mContext).finishWithAnim();
                }
                break;
        }
    }

    private void dismissBottomDialog() {
        if (mBottomDialog != null) {
            mBottomDialog.dismiss();
        }
    }
}