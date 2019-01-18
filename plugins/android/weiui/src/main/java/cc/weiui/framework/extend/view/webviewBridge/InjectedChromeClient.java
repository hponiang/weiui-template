package cc.weiui.framework.extend.view.webviewBridge;

import android.webkit.JsPromptResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import java.util.HashMap;
import java.util.Map;

import cc.weiui.framework.extend.module.weiuiJson;
import cc.weiui.framework.extend.view.ExtendWebView;


public class InjectedChromeClient extends WebChromeClient {
    private Map<String, JsCallJava> mJsCallJava = new HashMap<>();
    private boolean mIsInjectedJS;
    private boolean mAgainInjectedJS;
    private boolean enableApi = true;

    public InjectedChromeClient(String injectedName, Class injectedCls) {
        mJsCallJava.put(injectedName, new JsCallJava(injectedName, injectedCls));
    }

    public InjectedChromeClient(Map<String, Class> data) {
        if (data != null) {
            for (String injectedName : data.keySet()) {
                Class injectedCls = data.get(injectedName);
                if (injectedCls != null) {
                    mJsCallJava.put(injectedName, new JsCallJava(injectedName, injectedCls));
                }
            }
        }
    }

    public void setEnableApi(boolean enableApi) {
        this.enableApi = enableApi;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        //为什么要在这里注入JS
        //1、OnPageStarted中注入有可能全局注入不成功，导致页面脚本上所有接口任何时候都不可用
        //2、OnPageFinished中注入，虽然最后都会全局注入成功，但是完成时间有可能太晚，当页面在初始化调用接口函数时会等待时间过长
        //3、在进度变化时注入，刚好可以在上面两个问题中得到一个折中处理
        //4、进度大于15时进行注入
        if (newProgress <= 15) {
            mIsInjectedJS = false;
        } else if (!mIsInjectedJS) {
            for (String key : mJsCallJava.keySet()) {
                JsCallJava value = mJsCallJava.get(key);
                if (value != null) {
                    view.loadUrl(value.getPreloadInterfaceJS());
                }
            }
            mIsInjectedJS = true;
        }
        //5、进度大于25%时再次进行注入，因为从测试看来只有进度大于这个数字页面才真正得到框架刷新加载，保证100%注入成功
        if (newProgress <= 25) {
            mAgainInjectedJS = false;
        } else if (!mAgainInjectedJS) {
            for (String key : mJsCallJava.keySet()) {
                JsCallJava value = mJsCallJava.get(key);
                if (value != null) {
                    view.loadUrl(value.getPreloadInterfaceJS());
                }
            }
            view.loadUrl("javascript:(function(b){b.__readyInternum=0;b.__readyInterval=setInterval(function(){if(b.__readyInternum>100){clearInterval(b.__readyInterval)}if(typeof b.weiuiReady===\"function\"){b.weiuiReady();clearInterval(b.__readyInterval)}b.__readyInternum++},100)})(window);");
            mAgainInjectedJS = true;
        }
        //JS注入结束
        super.onProgressChanged(view, newProgress);
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        String identify = weiuiJson.getString(message, "__identify");
        if (enableApi && !"".equals(identify) && view instanceof ExtendWebView) {
            JsCallJava JSCJ = mJsCallJava.get(identify);
            if (JSCJ != null) {
                result.confirm(JSCJ.call((ExtendWebView) view, message));
                return true;
            }
        }
        return super.onJsPrompt(view, url, message, defaultValue, result);
    }
}