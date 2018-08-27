package com.dryseed.dsvirtualapk.core;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dryseed.dsvirtualapk.core.utils.Constants;
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
    }

    private void initViews() {
        findViewById(R.id.loadBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPlugin(MainActivity.this);
            }
        });
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
        findViewById(R.id.btn4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testResources(v);
            }
        });
    }

    /**
     * 从sdcard或者assets中获取apk文件，调用pluginManager.loadPlugin(file);
     *
     * @param context
     */
    @SuppressLint("UseToastDirectly")
    private void loadPlugin(Context context) {
        pluginManager = PluginManager.getInstance(context);

        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Toast.makeText(this, "sdcard was NOT MOUNTED!", Toast.LENGTH_SHORT).show();
        }

        File apk = new File(Environment.getExternalStorageDirectory(), "Test.apk");
        if (apk.exists()) {
            try {
                pluginManager.loadPlugin(apk);
                Toast.makeText(context, "Loaded plugin from apk: " + apk, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(context, "Loaded plugin from assets: " + file, Toast.LENGTH_SHORT).show();
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
            Class<?> clazz = pluginManager.getLoadedPlugin(Constants.TEST_PACKAGE_NAME).getClassLoader().loadClass(Constants.TEST_PACKAGE_NAME + ".Utils");
            Constructor<?> constructor = clazz.getConstructor();
            Object bundleUtils = constructor.newInstance();

            Method printSumMethod = clazz.getMethod("printSum", Context.class,
                    int.class, int.class, String.class);
            printSumMethod.setAccessible(true);
            Integer sum = (Integer) printSumMethod.invoke(bundleUtils,
                    getApplicationContext(), 10, 20, "计算结果");
            LogUtil.d("MMM", "debug:sum = " + sum);
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
        ComponentName componentName = new ComponentName(Constants.TEST_PACKAGE_NAME, Constants.TEST_PACKAGE_NAME + ".MainActivity");
        intent.setComponent(componentName);
        startActivity(intent);
    }

    /**
     * Test Hook Resources
     * TODO: 还需解决资源文件id冲突的问题
     *
     * @param view
     */
    @SuppressLint({"UseToastDirectly", "ResourceType"})
    public void testResources(View view) {
        /**
         * 如果使用getIdentifier方法，第一个参数是资源名称，第二个参数是资源类型，第三个参数是离线apk的包名，切记第三个参数。
         */
        Resources resources = pluginManager.getLoadedPlugin(Constants.TEST_PACKAGE_NAME).getResources();
        //String str = resources.getString(resources.getIdentifier("bundle_name", "string", Constants.TEST_PACKAGE_NAME));

        String s = resources.getString(0x7f060024);
        Toast.makeText(this, getResources().getString(0x7f060024) + "||" + s, Toast.LENGTH_SHORT).show();
    }
}
