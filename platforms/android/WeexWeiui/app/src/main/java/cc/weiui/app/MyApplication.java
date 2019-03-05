package cc.weiui.app;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.alibaba.weex.plugin.loader.WeexPluginContainer;
import com.taobao.weex.WXEnvironment;
import com.taobao.weex.WXSDKEngine;

import cc.weiui.framework.extend.module.weiuiBase;
import cc.weiui.framework.extend.module.weiui;

public class MyApplication extends Application {

    protected void attachBaseContext(Context ctx) {
        super.attachBaseContext(ctx);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //
        WXEnvironment.setOpenDebugLog(true);
        WXEnvironment.setApkDebugable(true);
        WXSDKEngine.addCustomOptions("appName", weiuiBase.appName);
        WXSDKEngine.addCustomOptions("appGroup", weiuiBase.appGroup);
        //
        weiui.init(this);
        WeexPluginContainer.loadAll(this);
    }
}
