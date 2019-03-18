package cc.weiui.framework.ui.module;

import android.app.Activity;

import com.alibaba.fastjson.JSONObject;
import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.common.WXModule;

import cc.weiui.framework.extend.module.weiuiJson;
import cc.weiui.framework.extend.module.weiuiPage;
import cc.weiui.framework.ui.weiui;

public class WeexNavigatorModule extends WXModule {

    private weiui __obj;

    private weiui myApp() {
        if (__obj == null) {
            __obj = new weiui();
        }
        return __obj;
    }

    /***************************************************************************************************/
    /***************************************************************************************************/
    /***************************************************************************************************/

    @JSMethod
    public void push(String object, JSCallback callback) {
        JSONObject json = weiuiJson.parseObject(object);
        if (json.size() == 0) {
            json.put("url", object);
        }
        json.put("pageTitle", weiuiJson.getString(json, "pageTitle", " "));
        myApp().openPage(mWXSDKInstance.getContext(), json.toJSONString(), callback);
    }

    @JSMethod
    public void pop(String object, JSCallback callback) {
        JSONObject json = weiuiJson.parseObject(object);
        if (weiuiJson.getString(json, "pageName", null) == null) {
            json.put("pageName", weiuiPage.getPageName((Activity) mWXSDKInstance.getContext()));
        }
        if (callback != null) {
            json.put("listenerName", "__navigatorPop");
            myApp().setPageStatusListener(mWXSDKInstance.getContext(), json.toJSONString(), callback);
        }
        myApp().closePage(mWXSDKInstance.getContext(), json.toJSONString());
    }
}
