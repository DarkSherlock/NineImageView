package com.liang.tind.nineimageview.widget.nineImageview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.liang.tind.nineimageview.NineImagesDetailActivity;

import java.io.Serializable;
import java.util.List;


public class NineImageViewEventAdapter extends NineImageViewAdapter {
    private static final String TAG = "NineImageViewEventAdapt";
    public NineImageViewEventAdapter(Context context, List<ImageAttr> imageAttr) {
        super(context, imageAttr);
    }

    @Override
    protected void onImageItemClick(Context context, NineImageView nineImageView, int index, List<ImageAttr> images) {
        for (int i = 0; i < images.size(); i++) {
            ImageAttr attr = images.get(i);
            View imageView = nineImageView.getChildAt(i);
            if (i >= nineImageView.getMaxSize()) {
                imageView = nineImageView.getChildAt(nineImageView.getMaxSize() - 1);
            }
            attr.width = imageView.getWidth();
            attr.height = imageView.getHeight();
            int[] points = new int[2];
            imageView.getLocationInWindow(points);
            attr.left = points[0];
            attr.top = points[1];
            Log.e(TAG, "onImageItemClick: "+attr.left+"y=="+attr.top);
        }

        Intent intent = new Intent(context, NineImagesDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(NineImagesDetailActivity.IMAGE_ATTR, (Serializable) images);
        bundle.putInt(NineImagesDetailActivity.CUR_POSITION, index);
        intent.putExtras(bundle);
        context.startActivity(intent);
        ((Activity) context).overridePendingTransition(0, 0);
    }

}
