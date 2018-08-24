package com.dryseed.dsvirtualapk;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dryseed.dsvirtualapk.internal.LoadedPlugin;
import com.dryseed.dsvirtualapk.utils.DexUtil;
import com.dryseed.dsvirtualapk.utils.LogUtil;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadPlugin(this);

    }

    private void loadPlugin(Context context) {
        PluginManager pluginManager = PluginManager.getInstance(context);

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
     *
     * @param view
     */
    public void testInstrumentation(View view) {
        Intent intent = new Intent(MainActivity.this, TestActivity.class);
        startActivity(intent);
    }

    /**
     * Test Hook ClassLoader
     *
     * @param view
     */
    public void testClassLoader(View view) {
        try {
            Class<?> clazz = LoadedPlugin.loadClass(getApplicationContext(),
                    "com.dryseed.dexclassloaderbundleapk.Utils");
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
}
