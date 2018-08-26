package plugin.dryseed.chapter6.broadcast_receiver_management;

import java.io.File;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * @author weishu
 * @date 16/4/7
 */
public class MainActivity extends Activity {

    private static final String ACTION = "com.dryseed.bundleapk.PLUGIN_ACTION";
    private static final String APK_NAME = "BundleApk-debug.apk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button t = new Button(this);
        setContentView(t);
        t.setText("send broadcast to plugin: demo");
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "插件插件!收到请回答!!", Toast.LENGTH_SHORT).show();
                sendBroadcast(new Intent("com.dryseed.bundleapk.MyReceiver"));
            }
        });

        Utils.extractAssets(this, APK_NAME);
        File testPlugin = getFileStreamPath(APK_NAME);
        try {
            ReceiverHelper.preLoadReceiver(this, testPlugin);
            Log.i(getClass().getSimpleName(), "hook success");
        } catch (Exception e) {
            throw new RuntimeException("receiver load failed", e);
        }

        // 注册插件收到我们发送的广播之后, 回传的广播
        registerReceiver(mReceiver, new IntentFilter(ACTION));
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("MMM", "onReceive2");
            Toast.makeText(context, "插件插件,我是主程序,握手完成!", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
