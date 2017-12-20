package com.liang.tind.nineimageview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.liang.tind.nineimageview.util.UIutils;
import com.liang.tind.nineimageview.widget.nineImageview.ImageAttr;
import com.liang.tind.nineimageview.widget.nineImageview.NineImageView;
import com.liang.tind.nineimageview.widget.nineImageview.NineImageViewEventAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    String longUrl = "https://ww3.sinaimg.cn/mw690/9c940e87gw1eufdweh67nj20rkdcbe81.jpg";
    String longUrl2 = "http://wx4.sinaimg.cn/mw690/e59bcb0dly1fgqlue0vtvj20c83htx3d.jpg";
    String gif = "https://wx4.sinaimg.cn/mw690/006D2Nuxgy1flxscdf84mg30cg06zqkz.gif";
    String gifUrl = "https://wx4.sinaimg.cn/mw690/006D2Nuxgy1flxscdf84mg30cg06zqkz.gif";
    String jpg = "http://img2.imgtn.bdimg.com/it/u=2850936076,2080165544&fm=206&gp=0.jpg";
    ArrayList<ImageAttr> imageAttrs = new ArrayList<>();
    private NineImageView mNineimageview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        Button buttonGif = findViewById(R.id.btn_1);
        Button buttonJpg = findViewById(R.id.btn_2);
        Button buttonLong = findViewById(R.id.btn_3);
        Button btn4 = findViewById(R.id.btn_4);
        buttonGif.setOnClickListener(this);
        buttonJpg.setOnClickListener(this);
        buttonLong.setOnClickListener(this);
        btn4.setOnClickListener(this);

        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText("测试");
        tvTitle.setText("最大尺寸: " + UIutils.getMaximumTextureSize());

        mNineimageview = (NineImageView) findViewById(R.id.nine_image_view);

    }

    @Override
    public void onClick(View v) {
        Log.e(TAG, "onClick: "+v.getId());
        switch (v.getId()) {
            case R.id.btn_1:
                setAdapter(gifUrl,1);
                break;
            case R.id.btn_2:
                setAdapter(jpg,9);
                break;
            case R.id.btn_3:
                setAdapter(longUrl,2);
                break;
            case R.id.btn_4:
                setAdapter(jpg,12);
                break;

        }
    }

    public void setAdapter(String url,int size) {
            imageAttrs.clear();
        for (int i = 0; i < size; i++) {
            ImageAttr imageAttr = new ImageAttr();
            imageAttr.url = url;
            imageAttr.compressUrl = url;
            imageAttr.thumbnailUrl = url;

            imageAttrs.add(imageAttr);
        }

        mNineimageview.setAdapter(new NineImageViewEventAdapter(this, imageAttrs));
    }


}
