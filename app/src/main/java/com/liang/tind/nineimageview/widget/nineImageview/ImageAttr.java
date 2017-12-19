package com.liang.tind.nineimageview.widget.nineImageview;

import java.io.Serializable;

public class ImageAttr implements Serializable {

    public String url;//原图URL
    public String thumbnailUrl;//缩略图
    public String compressUrl;//压缩图
    public boolean isUrlEnable = false;//是否显示原图
    public boolean isLongImage ;
    // 显示的宽高
    public int width;
    public int height;

    // 真实的高度
    public int realWidth;
    public int realHeight;

    // 左上角坐标
    public int left;
    public int top;

    @Override
    public String toString() {
        return "ImageAttr{" +
                "width=" + width +
                ", height=" + height +
                ", realWidth=" + realWidth +
                ", realHeight=" + realHeight +
                ", left=" + left +
                ", top=" + top +", url="+url +
                '}';
    }

    public boolean isUrlEnable() {
        return isUrlEnable;
    }

    public void setUrlEnable(boolean urlEnable) {
        isUrlEnable = urlEnable;
    }
}
