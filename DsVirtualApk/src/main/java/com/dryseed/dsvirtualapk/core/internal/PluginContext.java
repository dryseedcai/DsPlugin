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
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Resources;

/**
 * Created by renyugang on 16/8/12.
 */
class PluginContext extends ContextWrapper {

    private final LoadedPlugin mPlugin;

    public PluginContext(LoadedPlugin plugin) {
        super(plugin.getPluginManager().getHostContext());
        this.mPlugin = plugin;
    }

    private Context getHostContext() {
        return getBaseContext();
    }

    @Override
    public ClassLoader getClassLoader() {
        return this.mPlugin.getClassLoader();
    }


    @Override
    public Resources getResources() {
        return this.mPlugin.getResources();
    }

    @Override
    public void startActivity(Intent intent) {
        ComponentsHandler componentsHandler = mPlugin.getPluginManager().getComponentsHandler();
        componentsHandler.transformIntentToExplicitAsNeeded(intent);
        super.startActivity(intent);
    }

}
