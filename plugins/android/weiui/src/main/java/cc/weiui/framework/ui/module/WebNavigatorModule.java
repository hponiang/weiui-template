package cc.weiui.framework.ui.module;

import android.app.Activity;

import com.alibaba.fastjson.JSONObject;

import cc.weiui.framework.extend.module.weiuiJson;
import cc.weiui.framework.extend.module.weiuiPage;
import cc.weiui.framework.extend.view.ExtendWebView;
import cc.weiui.framework.extend.view.webviewBridge.JsCallback;
import cc.weiui.framework.ui.weiui;

public class WebNavigatorModule {

    private static weiui __obj;

    private static weiui myApp() {
        if (__obj == null) {
            __obj = new weiui();
        }
        return __obj;
    }

    /***************************************************************************************************/
    /***************************************************************************************************/
    /***************************************************************************************************/

    public static void push(ExtendWebView webView, String object, JsCallback callback) {
        JSONObject json = weiuiJson.parseObject(object);
        if (json.size() == 0) {
            json.put("url", object);
        }
        json.put("pageTitle", weiuiJson.getString(json, "pageTitle", " "));
        myApp().openPage(webView.getContext(), json.toJSONString(), weiui.MCallback(callback));
    }

    public static void pop(ExtendWebView webView, String object, JsCallback callback) {
        JSONObject json = weiuiJson.parseObject(object);
        if (weiuiJson.getString(json, "pageName", null) == null) {
            json.put("pageName", weiuiPage.getPageName((Activity) webView.getContext()));
        }
        if (callback != null) {
            json.put("listenerName", "__navigatorPop");
            myApp().setPageStatusListener(webView.getContext(), json.toJSONString(), weiui.MCallback(callback));
        }
        myApp().closePage(webView.getContext(), json.toJSONString());
    }
}
