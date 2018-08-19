package com.dryseed.assetsmultidexloader;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.dryseed.assetsmultidexloader.utils.LogUtil;

import java.io.File;

/**
 * @author caiminming
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File dexDir = getDir(AssetsManager.APK_DIR, Context.MODE_PRIVATE);
        // path : /data/data/com.dryseed.assetsmultidexloader/app_third_apk
        LogUtil.d("path : " + dexDir.getAbsolutePath());

    }
}
