package com.dryseed.resourceloaderhostapk;

import dalvik.system.DexClassLoader;

/**
 * @version $Id: BundleDexClassLoader.java, v 0.1 2015年12月11日 下午7:12:49 mochuan.zhb Exp $
 * @Author Zheng Haibo
 * @Company Alibaba Group
 * @PersonalWebsite http://www.mobctrl.net
 * @Description bundle的类加载器
 */
public class BundleDexClassLoader extends DexClassLoader {

    public BundleDexClassLoader(String dexPath, String optimizedDirectory,
                                String libraryPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, libraryPath, parent);
    }

}
