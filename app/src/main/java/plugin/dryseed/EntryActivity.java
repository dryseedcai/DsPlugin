package plugin.dryseed;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import plugin.dryseed.plugin.R;

public class EntryActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entry_layout);
    }

    public void onBtn1Click(View view) {
        startActivity(new Intent(this, plugin.dryseed.chapter1.dynamic_proxy_hook.hook.MainActivity.class));
    }

    public void onBtn2Click(View view) {
        startActivity(new Intent(this, plugin.dryseed.chapter2.binder_hook.MainActivity.class));
    }

    public void onBtn3Click(View view) {
        startActivity(new Intent(this, plugin.dryseed.chapter3.ams_pms_hook.MainActivity.class));
    }

    public void onBtn4Click(View view) {
        startActivity(new Intent(this, plugin.dryseed.chapter4.intercept_activity.MainActivity.class));
    }

    public void onBtn5Click(View view) {
        startActivity(new Intent(this, plugin.dryseed.chapter5.classloader_hook.MainActivity.class));
    }

    public void onBtn6Click(View view) {
        startActivity(new Intent(this, plugin.dryseed.chapter6.broadcast_receiver_management.MainActivity.class));
    }
}
