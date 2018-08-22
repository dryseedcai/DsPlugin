package com.dryseed.layoutinflaterclassloader;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

/**
 * @version $Id: BundleActivity.java, v 0.1 2015年12月16日 下午3:49:00 mochuan.zhb Exp $
 * @Author Zheng Haibo
 * @Company Alibaba Group
 * @PersonalWebsite http://www.mobctrl.net
 * @Description
 */
public class BundleActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("debug:BundleActivity onCreate...");
        // 设置Bundle Layout中的ID：bundle_layout=0x7f03001b;
        int bundleLayoutId = 0x7f03001b;
        View bundleView = LayoutInflater.from(this).inflate(bundleLayoutId, null);
        // 有bug：java.lang.RuntimeException: Unable to start activity ComponentInfo{com.dryseed.layoutinflaterclassloader/com.dryseed.layoutinflaterclassloader.BundleActivity}:
        // android.content.res.Resources$NotFoundException: File res/layout/abc_action_bar_title_item.xml from drawable resource ID #0x7f030000
        setContentView(bundleView);
    }

    @Override
    protected void attachBaseContext(Context context) {
        BundleResourceLoader.replaceContextResources(context, null);
        super.attachBaseContext(context);
    }
}
