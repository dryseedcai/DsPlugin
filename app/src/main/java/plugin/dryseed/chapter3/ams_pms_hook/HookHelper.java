package plugin.dryseed.chapter3.ams_pms_hook;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * @author weishu
 * @date 16/3/7
 */
public final class HookHelper {

    /**
     * Hook AMS
     */
    public static void hookActivityManager() {
        /*
            public ActivityResult execStartActivity(
                Context who, IBinder contextThread, IBinder token, Activity target,
                Intent intent, int requestCode, Bundle options) {
                // ... 省略无关代码
                try {
                    intent.migrateExtraStreamToClipData();
                    intent.prepareToLeaveProcess();
                    // ----------------look here!!!!!!!!!!!!!!!!!!!
                    int result = ActivityManagerNative.getDefault()
                        .startActivity(whoThread, who.getBasePackageName(), intent,
                                intent.resolveTypeIfNeeded(who.getContentResolver()),
                                token, target != null ? target.mEmbeddedID : null,
                                requestCode, 0, null, null, options);
                    checkStartActivityResult(result, intent);
                } catch (RemoteException e) {
                }
                return null;
            }
         */
        try {
            Class<?> activityManagerNativeClass = Class.forName("android.app.ActivityManagerNative");

            // 获取 gDefault 这个字段, 想办法替换它
            Field gDefaultField = activityManagerNativeClass.getDeclaredField("gDefault");
            gDefaultField.setAccessible(true);

            // Singleton<IActivityManager> gDefault = ActivityManagerNative.gDefault
            Object gDefault = gDefaultField.get(null);

            // 4.x以上的gDefault是一个 android.util.Singleton对象; 我们取出这个单例里面的字段
            Class<?> singleton = Class.forName("android.util.Singleton");
            Field mInstanceField = singleton.getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);

            // ActivityManagerNative 的gDefault对象里面原始的 IActivityManager对象
            // IActivityManager rawIActivityManager = gDefault.mInstance
            Object rawIActivityManager = mInstanceField.get(gDefault);

            // 创建一个这个对象的代理对象, 然后替换这个字段, 让我们的代理对象帮忙干活
            Class<?> iActivityManagerInterface = Class.forName("android.app.IActivityManager");
            Object proxy = Proxy.newProxyInstance(
                    Thread.currentThread().getContextClassLoader(),
                    new Class<?>[]{iActivityManagerInterface},
                    new HookHandler(rawIActivityManager)
            );
            mInstanceField.set(gDefault, proxy);

        } catch (Exception e) {
            throw new RuntimeException("Hook Failed", e);
        }

    }

    /**
     * Hook PMS
     *
     * @param context
     */
    public static void hookPackageManager(Context context) {
        /*
            public PackageManager getPackageManager() {
                if (mPackageManager != null) {
                    return mPackageManager;
                }

                IPackageManager pm = ActivityThread.getPackageManager();
                if (pm != null) {
                    // Doesn't matter if we make more than one instance.
                    return (mPackageManager = new ApplicationPackageManager(this, pm));
                }
                return null;
            }
         */
        try {
            // 获取全局的ActivityThread对象
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            Object currentActivityThread = currentActivityThreadMethod.invoke(null);

            // 获取ActivityThread里面原始的 sPackageManager
            Field sPackageManagerField = activityThreadClass.getDeclaredField("sPackageManager");
            sPackageManagerField.setAccessible(true);
            Object sPackageManager = sPackageManagerField.get(currentActivityThread);

            // 准备好代理对象, 用来替换原始的对象
            Class<?> iPackageManagerInterface = Class.forName("android.content.pm.IPackageManager");
            Object proxy = Proxy.newProxyInstance(
                    iPackageManagerInterface.getClassLoader(),
                    new Class<?>[]{iPackageManagerInterface},
                    new HookHandler(sPackageManager)
            );

            // 1. 替换掉 ActivityThread 里面的 sPackageManager 字段
            sPackageManagerField.set(currentActivityThread, proxy);

            // 2. 替换 ApplicationPackageManager 里面的 mPm对象
            PackageManager pm = context.getPackageManager();
            Field mPmField = pm.getClass().getDeclaredField("mPM");
            mPmField.setAccessible(true);
            mPmField.set(pm, proxy);
        } catch (Exception e) {
            throw new RuntimeException("hook failed", e);
        }
    }
}
