/*
 * Copyright (C) 2017 Beijing Didi Infinity Technology and Development Co.,Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dryseed.dsvirtualapk.core.internal;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;

import com.dryseed.dsvirtualapk.core.PluginManager;
import com.dryseed.dsvirtualapk.core.utils.ReflectUtil;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

/**
 * Created by renyugang on 16/8/9.
 */
class ResourcesManager {
    /**
     * 创建插件的Resources对象
     * 1. 合并模式下，才会走到这个方法里，插件资源合并到宿主里面去，共用一个Resources对象。
     * 2. 会更新所有加载过的插件，更新新的Resources对象。
     * 3. 这种解决方案存在几个问题，具体看方法内注释。更好地解决方案可以参考atlas。
     *
     * @param hostContext 宿主的Context
     * @param apk         插件的路径
     * @return
     */
    public static synchronized Resources createResources(Context hostContext, String apk) {
        // 宿主的Resources对象
        Resources hostResources = hostContext.getResources();
        // 插件的Resources对象
        Resources newResources = null;
        AssetManager assetManager;
        try {
            /**
             * 针对系统版本的区分涉及到资源加载时候的兼容性问题
             * 由于资源做过分区，则在Android L后直接将插件包的apk地址 addAssetPath 之后就可以，
             * 但是在Android L之前，addAssetPath 只是把补丁包加入到资源路径列表里，但是资源的解析其实是在很早的时候就已经执行完了
             * 由于有系统资源的存在， mResources 的初始化在很早就初始化了，所以我们就算通过 addAssetPath 方法将 apk 添加到 mAssetPaths 里，
             * 在查找资源的时候也不会找到这部分的资源，因为在旧的 mResources 里没有这部分的 id。
             * 所以在 Android L 之前是需要想办法构造一个新的 AssetManager 里的 mResources 才行
             */
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                /**
                 * VirtualAPK 用的是类似 InstantRun 的那种方案，构造一个新的 AssetManager ，将宿主和加载过的插件的所有 apk 全都添加一遍，
                 * 然后再调用 hookResources 方法将新的 Resources 替换回原来的，这样会引起两个问题，
                 * 一个是每次加载新的插件都会重新构造一个 AssetManger 和 Resources ，然后重新添加所有资源，这样涉及到很多机型的兼容(因为部分厂商自己修改了 Resources 的类名)，
                 * 一个是需要有一个替换原来 Resources 的过程，这样就需要涉及到很多地方，从 hookResources 的实现里看，替换了四处地方，在尽量少的 hook 原则下这样的情况还是尽量避免的。
                 */
                assetManager = AssetManager.class.newInstance();
                // code : assetManager.addAssetPath(hostContext.getApplicationInfo().sourceDir)
                ReflectUtil.invoke(AssetManager.class, assetManager, "addAssetPath", hostContext.getApplicationInfo().sourceDir);
            } else {
                assetManager = hostResources.getAssets();
            }

            // 添加 加载过的插件 资源
            ReflectUtil.invoke(AssetManager.class, assetManager, "addAssetPath", apk);
            List<LoadedPlugin> pluginList = PluginManager.getInstance(hostContext).getAllLoadedPlugins();
            for (LoadedPlugin plugin : pluginList) {
                ReflectUtil.invoke(AssetManager.class, assetManager, "addAssetPath", plugin.getLocation());
            }

            // 涉及到很多机型的兼容(因为部分厂商自己修改了 Resources 的类名)
            if (isMiUi(hostResources)) {
                newResources = MiUiResourcesCompat.createResources(hostResources, assetManager);
            } else if (isVivo(hostResources)) {
                newResources = VivoResourcesCompat.createResources(hostContext, hostResources, assetManager);
            } else if (isNubia(hostResources)) {
                newResources = NubiaResourcesCompat.createResources(hostResources, assetManager);
            } else if (isNotRawResources(hostResources)) {
                newResources = AdaptationResourcesCompat.createResources(hostResources, assetManager);
            } else {
                // is raw android resources
                newResources = new Resources(assetManager, hostResources.getDisplayMetrics(), hostResources.getConfiguration());
            }

            // lastly, sync all LoadedPlugin to newResources
            for (LoadedPlugin plugin : pluginList) {
                plugin.updateResources(newResources);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return newResources;
    }

    /**
     * 替换原来的Resources，改为新生成的Resources
     *
     * @param base
     * @param resources
     */
    public static void hookResources(Context base, Resources resources) {
        try {
            // hook mResources
            ReflectUtil.setField(base.getClass(), base, "mResources", resources);
            // hook mPackageInfo
            Object loadedApk = ReflectUtil.getPackageInfo(base);
            ReflectUtil.setField(loadedApk.getClass(), loadedApk, "mResources", resources);
            // hook activityThread.mResourcesManager
            Object activityThread = ReflectUtil.getActivityThread(base);
            Object resManager = ReflectUtil.getField(activityThread.getClass(), activityThread, "mResourcesManager");
            if (Build.VERSION.SDK_INT < 24) {
                // hook mActiveResources
                Map<Object, WeakReference<Resources>> map = (Map<Object, WeakReference<Resources>>) ReflectUtil.getField(resManager.getClass(), resManager, "mActiveResources");
                Object key = map.keySet().iterator().next();
                map.put(key, new WeakReference<>(resources));
            } else {
                // still hook Android N Resources, even though it's unnecessary, then nobody will be strange.
                Map map = (Map) ReflectUtil.getFieldNoException(resManager.getClass(), resManager, "mResourceImpls");
                Object key = map.keySet().iterator().next();
                Object resourcesImpl = ReflectUtil.getFieldNoException(Resources.class, resources, "mResourcesImpl");
                map.put(key, new WeakReference<>(resourcesImpl));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isMiUi(Resources resources) {
        return resources.getClass().getName().equals("android.content.res.MiuiResources");
    }

    private static boolean isVivo(Resources resources) {
        return resources.getClass().getName().equals("android.content.res.VivoResources");
    }

    private static boolean isNubia(Resources resources) {
        return resources.getClass().getName().equals("android.content.res.NubiaResources");
    }

    private static boolean isNotRawResources(Resources resources) {
        return !resources.getClass().getName().equals("android.content.res.Resources");
    }

    private static final class MiUiResourcesCompat {
        private static Resources createResources(Resources hostResources, AssetManager assetManager) throws Exception {
            Class resourcesClazz = Class.forName("android.content.res.MiuiResources");
            Resources newResources = (Resources) ReflectUtil.invokeConstructor(resourcesClazz,
                    new Class[]{AssetManager.class, DisplayMetrics.class, Configuration.class},
                    new Object[]{assetManager, hostResources.getDisplayMetrics(), hostResources.getConfiguration()});
            return newResources;
        }
    }

    private static final class VivoResourcesCompat {
        private static Resources createResources(Context hostContext, Resources hostResources, AssetManager assetManager) throws Exception {
            Class resourcesClazz = Class.forName("android.content.res.VivoResources");
            Resources newResources = (Resources) ReflectUtil.invokeConstructor(resourcesClazz,
                    new Class[]{AssetManager.class, DisplayMetrics.class, Configuration.class},
                    new Object[]{assetManager, hostResources.getDisplayMetrics(), hostResources.getConfiguration()});
            ReflectUtil.invokeNoException(resourcesClazz, newResources, "init",
                    new Class[]{String.class}, hostContext.getPackageName());
            Object themeValues = ReflectUtil.getFieldNoException(resourcesClazz, hostResources, "mThemeValues");
            ReflectUtil.setFieldNoException(resourcesClazz, newResources, "mThemeValues", themeValues);
            return newResources;
        }
    }

    private static final class NubiaResourcesCompat {
        private static Resources createResources(Resources hostResources, AssetManager assetManager) throws Exception {
            Class resourcesClazz = Class.forName("android.content.res.NubiaResources");
            Resources newResources = (Resources) ReflectUtil.invokeConstructor(resourcesClazz,
                    new Class[]{AssetManager.class, DisplayMetrics.class, Configuration.class},
                    new Object[]{assetManager, hostResources.getDisplayMetrics(), hostResources.getConfiguration()});
            return newResources;
        }
    }

    private static final class AdaptationResourcesCompat {
        private static Resources createResources(Resources hostResources, AssetManager assetManager) throws Exception {
            Resources newResources;
            try {
                Class resourcesClazz = hostResources.getClass();
                newResources = (Resources) ReflectUtil.invokeConstructor(resourcesClazz,
                        new Class[]{AssetManager.class, DisplayMetrics.class, Configuration.class},
                        new Object[]{assetManager, hostResources.getDisplayMetrics(), hostResources.getConfiguration()});
            } catch (Exception e) {
                newResources = new Resources(assetManager, hostResources.getDisplayMetrics(), hostResources.getConfiguration());
            }

            return newResources;
        }
    }

}
