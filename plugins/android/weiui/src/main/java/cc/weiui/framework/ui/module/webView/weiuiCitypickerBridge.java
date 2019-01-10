package cc.weiui.framework.ui.module.webView;

import cc.weiui.framework.extend.delegate.ModuleDelegate;
import cc.weiui.framework.extend.view.ExtendWebView;
import cc.weiui.framework.extend.view.webviewBridge.JsCallback;


public class weiuiCitypickerBridge {

    public static Class init() {
        return weiuiCitypickerBridge.class;
    }

    /***************************************************************************************************/
    /***************************************************************************************************/
    /***************************************************************************************************/

    /**
     * 选择地址
     * @param object
     * @param callback
     */
    public static void select(ExtendWebView webView, String object, JsCallback callback) {
        ModuleDelegate.getInstance().getData("weiui_citypicker", "select", webView, object, callback);
    }
}
