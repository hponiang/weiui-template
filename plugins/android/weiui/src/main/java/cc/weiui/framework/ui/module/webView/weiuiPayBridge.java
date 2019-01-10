package cc.weiui.framework.ui.module.webView;


import cc.weiui.framework.extend.delegate.ModuleDelegate;
import cc.weiui.framework.extend.view.ExtendWebView;
import cc.weiui.framework.extend.view.webviewBridge.JsCallback;


public class weiuiPayBridge {

    public static Class init() {
        return weiuiPayBridge.class;
    }

    /***************************************************************************************************/
    /***************************************************************************************************/
    /***************************************************************************************************/

    /**
     * 官方微信支付
     *
     * @param payData
     * @param callback
     */
    public static void weixin(ExtendWebView webView, String payData, JsCallback callback) {
        ModuleDelegate.getInstance().getData("weiui_pay", "weixin", null, webView, payData, callback);
    }

    /**
     * 官方支付宝支付
     *
     * @param payData
     * @param callback
     */
    public static void alipay(ExtendWebView webView, String payData, JsCallback callback) {
        ModuleDelegate.getInstance().getData("weiui_pay", "alipay", webView, payData, callback);
    }

    /**
     * 银联微信支付（无回调功能）
     *
     * @param payData
     */
    public static void union_weixin(ExtendWebView webView, String payData) {
        ModuleDelegate.getInstance().getData("weiui_pay", "union_weixin", webView, payData);
    }

    /**
     * 银联支付宝支付（无回调功能）
     *
     * @param payData
     */
    public static void union_alipay(ExtendWebView webView, String payData) {
        ModuleDelegate.getInstance().getData("weiui_pay", "union_alipay", webView, payData);
    }

}
