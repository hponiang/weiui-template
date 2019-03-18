package cc.weiui.framework.ui.module;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cc.weiui.framework.activity.PageActivity;
import cc.weiui.framework.extend.module.weiuiJson;
import cc.weiui.framework.extend.view.ExtendWebView;
import cc.weiui.framework.extend.view.webviewBridge.JsCallback;
import cc.weiui.framework.ui.weiui;

public class WebNavigationBarModule {

    public static void setTitle(ExtendWebView webView, String object, JsCallback callback) {
        if (webView.getContext() instanceof PageActivity) {
            JSONObject json = weiuiJson.parseObject(object);
            if (json.size() == 0) {
                json.put("title", object);
            }
            PageActivity mPageActivity = ((PageActivity) webView.getContext());
            mPageActivity.setNavigationTitle(json, result -> {
                if (callback != null) {
                    weiui.MCallback(callback).invokeAndKeepAlive(result);
                }
            });
        }
    }

    public static void setLeftItem(ExtendWebView webView, String object, JsCallback callback) {
        if (webView.getContext() instanceof PageActivity) {
            Object items = null;
            JSONObject json = weiuiJson.parseObject(object);
            if (json.size() == 0) {
                JSONArray array = weiuiJson.parseArray(object);
                if (array.size() == 0) {
                    json.put("title", object);
                }else{
                    items = array;
                }
            }else{
                items = json;
            }
            PageActivity mPageActivity = ((PageActivity) webView.getContext());
            mPageActivity.setNavigationItems(items, "left", result -> {
                if (callback != null) {
                    weiui.MCallback(callback).invokeAndKeepAlive(result);
                }
            });
        }
    }

    public static void setRightItem(ExtendWebView webView, String object, JsCallback callback) {
        if (webView.getContext() instanceof PageActivity) {
            Object items = null;
            JSONObject json = weiuiJson.parseObject(object);
            if (json.size() == 0) {
                JSONArray array = weiuiJson.parseArray(object);
                if (array.size() == 0) {
                    json.put("title", object);
                }else{
                    items = array;
                }
            }else{
                items = json;
            }
            PageActivity mPageActivity = ((PageActivity) webView.getContext());
            mPageActivity.setNavigationItems(items, "right", result -> {
                if (callback != null) {
                    weiui.MCallback(callback).invokeAndKeepAlive(result);
                }
            });
        }
    }

    public static void show(ExtendWebView webView) {
        if (webView.getContext() instanceof PageActivity) {
            PageActivity mPageActivity = ((PageActivity) webView.getContext());
            mPageActivity.showNavigation();
        }
    }

    public static void hide(ExtendWebView webView) {
        if (webView.getContext() instanceof PageActivity) {
            PageActivity mPageActivity = ((PageActivity) webView.getContext());
            mPageActivity.hideNavigation();
        }
    }
}
