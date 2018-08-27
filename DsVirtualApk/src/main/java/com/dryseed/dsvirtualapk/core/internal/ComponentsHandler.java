package com.dryseed.dsvirtualapk.core.internal;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.widget.Toast;

import com.dryseed.dsvirtualapk.core.PluginManager;
import com.dryseed.dsvirtualapk.core.utils.Constants;
import com.dryseed.dsvirtualapk.core.utils.LogUtil;

/**
 * @author caiminming
 */
public class ComponentsHandler {
    private Context mContext;
    private PluginManager mPluginManager;

    public ComponentsHandler(PluginManager pluginManager) {
        mPluginManager = pluginManager;
        mContext = pluginManager.getHostContext();
    }

    /**
     * transform intent from implicit to explicit
     */
    @SuppressLint("UseToastDirectly")
    public Intent transformIntentToExplicitAsNeeded(Intent intent) {
        ComponentName component = intent.getComponent();
        if (component == null
                || component.getPackageName().equals(mContext.getPackageName())) {
//            ResolveInfo info = mPluginManager.resolveActivity(intent);
//            if (info != null && info.activityInfo != null) {
//                component = new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
//                intent.setComponent(component);
//            }
        }

        Toast.makeText(mContext, "TODO: mPluginManager.resolveActivity", Toast.LENGTH_SHORT).show();
        return intent;
    }

    public void markIntentIfNeeded(Intent intent) {
        if (intent.getComponent() == null) {
            return;
        }

        String targetPackageName = intent.getComponent().getPackageName();
        String targetClassName = intent.getComponent().getClassName();
        // search map and return specific launchmode stub activity
        // 判断如果启动的是插件中类，则将启动的包名和Activity类名存到了intent中，并设置占位Activity
        if (!targetPackageName.equals(mContext.getPackageName())
                && mPluginManager.getLoadedPlugin(targetPackageName) != null) {
            LogUtil.d("dispatchStubActivity");
            intent.putExtra(Constants.KEY_IS_PLUGIN, true);
            intent.putExtra(Constants.KEY_TARGET_PACKAGE, targetPackageName);
            intent.putExtra(Constants.KEY_TARGET_ACTIVITY, targetClassName);
            dispatchStubActivity(intent);
        }
    }

    private void dispatchStubActivity(Intent intent) {
        ComponentName component = intent.getComponent();
        String targetClassName = intent.getComponent().getClassName();
        LoadedPlugin loadedPlugin = mPluginManager.getLoadedPlugin(intent);
        ActivityInfo info = loadedPlugin.getActivityInfo(component);
        if (info == null) {
            throw new RuntimeException("can not find " + component);
        }

//        int launchMode = info.launchMode;
//        Resources.Theme themeObj = loadedPlugin.getResources().newTheme();
//        themeObj.applyStyle(info.theme, true);
//        String stubActivity = mStubActivityInfo.getStubActivity(targetClassName, launchMode, themeObj);
//        LogUtil.d(String.format("dispatchStubActivity,[%s -> %s]", targetClassName, stubActivity));
//        intent.setClassName(mContext, stubActivity);

        intent.setClassName(mContext, String.format("%s.A$%d", "com.dryseed.dsvirtualapk.core", 1));
    }
}
