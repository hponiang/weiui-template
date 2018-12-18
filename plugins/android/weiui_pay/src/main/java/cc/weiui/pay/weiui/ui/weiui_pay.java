package cc.weiui.pay.weiui.ui;

import com.taobao.weex.WXSDKEngine;
import com.taobao.weex.common.WXException;

import cc.weiui.pay.weiui.ui.module.weiuiPayModule;

public class weiui_pay {

    public static void init() {
        try {
            WXSDKEngine.registerModule("weiui_pay", weiuiPayModule.class);
        } catch (WXException e) {
            e.printStackTrace();
        }
    }

}
