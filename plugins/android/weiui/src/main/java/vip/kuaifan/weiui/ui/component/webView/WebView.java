package vip.kuaifan.weiui.ui.component.webView;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONObject;
import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.dom.WXDomObject;
import com.taobao.weex.ui.component.WXVContainer;

import java.util.HashMap;
import java.util.Map;

import vip.kuaifan.weiui.R;
import vip.kuaifan.weiui.extend.module.weiuiConstants;
import vip.kuaifan.weiui.extend.module.weiuiJson;
import vip.kuaifan.weiui.extend.module.weiuiParse;
import vip.kuaifan.weiui.extend.view.ProgressWebView;

/**
 * Created by WDM on 2018/4/13.
 */
public class WebView extends WXVContainer<ViewGroup> {

    private static final String TAG = "WebView";

    private View mView;

    private ProgressWebView v_webview;

    public WebView(WXSDKInstance instance, WXDomObject dom, WXVContainer parent) {
        super(instance, dom, parent);
    }

    @Override
    protected ViewGroup initComponentHostView(@NonNull Context context) {
        mView = ((Activity) context).getLayoutInflater().inflate(R.layout.layout_weiui_webview, null);
        initPagerView();
        //
        if (getDomObject().getEvents().contains(weiuiConstants.Event.READY)) {
            fireEvent(weiuiConstants.Event.READY, null);
        }
        //
        return (ViewGroup) mView;
    }

    @Override
    public void addSubView(View view, int index) {

    }

    private void initPagerView() {
        v_webview = mView.findViewById(R.id.v_webview);
        //
        if (getDomObject().getEvents().contains(weiuiConstants.Event.STATE_CHANGED)) {
            v_webview.setOnStatusClient(new ProgressWebView.StatusCall() {
                @Override
                public void onStatusChanged(android.webkit.WebView view, String status) {
                    Map<String, Object> retData = new HashMap<>();
                    retData.put("status", status);
                    fireEvent(weiuiConstants.Event.STATE_CHANGED, retData);
                }

                @Override
                public void onErrorChanged(android.webkit.WebView view, int errorCode, String description, String failingUrl) {
                    Map<String, Object> retData = new HashMap<>();
                    retData.put("status", "error");
                    retData.put("errCode", errorCode);
                    retData.put("errMsg", description);
                    retData.put("errUrl", failingUrl);
                    fireEvent(weiuiConstants.Event.STATE_CHANGED, retData);
                }

                @Override
                public void onTitleChanged(android.webkit.WebView view, String title) {
                    Map<String, Object> retData = new HashMap<>();
                    retData.put("status", "title");
                    retData.put("title", title);
                    fireEvent(weiuiConstants.Event.STATE_CHANGED, retData);
                }
            });
        }
    }

    @Override
    protected boolean setProperty(String key, Object param) {
        return initProperty(key, param) || super.setProperty(key, param);
    }

    private boolean initProperty(String key, Object val) {
        switch (key) {
            case "weiui":
                JSONObject json = weiuiJson.parseObject(weiuiParse.parseStr(val, ""));
                if (json.size() > 0) {
                    for (Map.Entry<String, Object> entry : json.entrySet()) {
                        initProperty(entry.getKey(), entry.getValue());
                    }
                }
                return true;

            case "url":
                setUrl(weiuiParse.parseStr(val, ""));
                return true;

            case "content":
                setContent(weiuiParse.parseStr(val, ""));
                return true;

            default:
                return false;
        }
    }

    private boolean __canGoBack() {
        return v_webview != null && v_webview.canGoBack();
    }

    private boolean __canGoForward() {
        return v_webview != null && v_webview.canGoForward();
    }

    /***************************************************************************************************/
    /***************************************************************************************************/
    /***************************************************************************************************/

    /**
     * 设置Url
     * @param url
     */
    @JSMethod
    public void setUrl(String url){
        if (v_webview != null) {
            v_webview.loadUrl(url);
        }
    }

    /**
     * 设置内容
     * @param content
     */
    @JSMethod
    public void setContent(String content){
        if (v_webview != null) {
            v_webview.loadDataWithBaseURL("about:blank", "<html>" +
                    "<header>" +
                    "<meta charset='utf-8'>" +
                    "<meta name='viewport' content='width=device-width, initial-scale=1, maximum-scale=1, minimum-scale=1, user-scalable=no'>" +
                    "<style type='text/css'>" + ProgressWebView.commonStyle() + "</style>" +
                    "</header>" +
                    "<body>" + content + "</body>" +
                    "</html>", "text/html", "utf-8", null);
        }
    }

    /**
     * 是否可以后退
     */
    @JSMethod
    public void canGoBack(JSCallback callback) {
        if (callback != null) {
            callback.invoke(__canGoBack());
        }
    }

    /**
     * 后退
     */
    @JSMethod
    public void goBack(JSCallback callback){
        boolean canBack = false;
        if (__canGoBack()) {
            v_webview.goBack();
            canBack = true;
        }
        if (callback != null) {
            callback.invoke(canBack);
        }
    }

    /**
     * 是否可以前进
     */
    @JSMethod
    public void canGoForward(JSCallback callback) {
        if (callback != null) {
            callback.invoke(__canGoForward());
        }
    }

    /**
     * 前进
     */
    @JSMethod
    public void goForward(JSCallback callback){
        boolean canForward = false;
        if (__canGoForward()) {
            v_webview.goForward();
            canForward = true;
        }
        if (callback != null) {
            callback.invoke(canForward);
        }
    }
}
