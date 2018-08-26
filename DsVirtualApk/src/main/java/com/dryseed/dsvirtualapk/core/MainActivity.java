package com.dryseed.dsvirtualapk.core;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dryseed.dsvirtualapk.core.utils.LogUtil;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    private PluginManager pluginManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        Toast.makeText(this, "" + this.getPackageName(), Toast.LENGTH_SHORT).show();
        loadPlugin(this);

    }

    private void initViews() {
        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testInstrumentation(v);
            }
        });
        findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testClassLoader(v);
            }
        });
        findViewById(R.id.btn3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testActivity(v);
            }
        });
    }

    /**
     * 从sdcard或者assets中获取apk文件，调用pluginManager.loadPlugin(file);
     *
     * @param context
     */
    private void loadPlugin(Context context) {
        pluginManager = PluginManager.getInstance(context);

        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Toast.makeText(this, "sdcard was NOT MOUNTED!", Toast.LENGTH_SHORT).show();
        }

        File apk = new File(Environment.getExternalStorageDirectory(), "Test.apk");
        if (apk.exists()) {
            try {
                pluginManager.loadPlugin(apk);
                LogUtil.d("Loaded plugin from apk: " + apk);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                File file = new File(context.getFilesDir(), "Test.apk");
                java.io.InputStream inputStream = context.getAssets().open("Test.apk", 2);
                java.io.FileOutputStream outputStream = new java.io.FileOutputStream(file);
                byte[] buf = new byte[1024];
                int len;

                while ((len = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, len);
                }

                outputStream.close();
                inputStream.close();

                pluginManager.loadPlugin(file);
                LogUtil.d("Loaded plugin from assets: " + file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Test Hook Instrumentation
     * 1. VAInstrumentation动态代理了Instrumentation
     *
     * @param view
     */
    public void testInstrumentation(View view) {
        Intent intent = new Intent(MainActivity.this, TestActivity.class);
        startActivity(intent);
    }

    /**
     * Test Hook ClassLoader
     * 1. 动态加载apk中的Utils类
     * 2. LoadedPlugin中创建了新的ClassLoader
     *
     * @param view
     */
    public void testClassLoader(View view) {
        try {
            Class<?> clazz = pluginManager.getLoadedPlugin("com.dryseed.dexclassloaderbundleapk").getClassLoader().loadClass("com.dryseed.dexclassloaderbundleapk.Utils");
            Constructor<?> constructor = clazz.getConstructor();
            Object bundleUtils = constructor.newInstance();

            Method printSumMethod = clazz.getMethod("printSum", Context.class,
                    int.class, int.class, String.class);
            printSumMethod.setAccessible(true);
            Integer sum = (Integer) printSumMethod.invoke(bundleUtils,
                    getApplicationContext(), 10, 20, "计算结果");
            Log.d("MMM", "debug:sum = " + sum);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test Jump Plugin Activity
     *
     * @param view
     */
    public void testActivity(View view) {
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName("com.dryseed.dexclassloaderbundleapk", "com.dryseed.dexclassloaderbundleapk.MainActivity");
        intent.setComponent(componentName);
        startActivity(intent);
    }
}
