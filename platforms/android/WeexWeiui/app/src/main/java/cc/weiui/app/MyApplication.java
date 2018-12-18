package cc.weiui.app;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.weex.plugin.loader.WeexPluginContainer;
import com.lljjcoder.weiui.ui.weiui_citypicker;
import com.luck.picture.lib.weiui.ui.weiui_picture;
import com.taobao.weex.WXEnvironment;
import com.taobao.weex.WXSDKEngine;

import cc.weiui.pay.weiui.ui.weiui_pay;
import io.rong.imlib.weiui.ui.weiui_rongim;
import cc.weiui.framework.extend.module.weiui;
import cc.weiui.framework.extend.module.weiuiJson;
import cc.weiui.umeng.ui.weiui_umeng;

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
        WXSDKEngine.addCustomOptions("appName", Base.appName);
        WXSDKEngine.addCustomOptions("appGroup", Base.appGroup);
        //
        weiui.init(this);
        weiui_citypicker.init();
        weiui_picture.init();
        weiui_pay.init();
        WeexPluginContainer.loadAll(this);
        //
        initRongim();
        initUmeng();
    }

    private void initRongim() {
        JSONObject rongim = weiuiJson.parseObject(Base.config.getObject("rongim").get("android"));
        if (weiuiJson.getBoolean(rongim, "enabled")) {
            weiui_rongim.init(weiuiJson.getString(rongim, "appKey"), weiuiJson.getString(rongim, "appSecret"));
        }
    }

    private void initUmeng() {
        JSONObject umeng = weiuiJson.parseObject(Base.config.getObject("umeng").get("android"));
        if (weiuiJson.getBoolean(umeng, "enabled")) {
            weiui_umeng.init(weiuiJson.getString(umeng, "appKey"), weiuiJson.getString(umeng, "appSecret"), weiuiJson.getString(umeng, "channel"));
        }
    }
}
