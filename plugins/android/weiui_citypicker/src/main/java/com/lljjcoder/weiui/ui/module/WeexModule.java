package com.lljjcoder.weiui.ui.module;

import com.lljjcoder.weiui.ui.weiui_citypicker;
import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.common.WXModule;


public class WeexModule extends WXModule {

    private static final String TAG = "weiuiCitypickerModule";

    private weiui_citypicker myApp;

    WeexModule() {
        myApp = new weiui_citypicker();
    }

    /***************************************************************************************************/
    /***************************************************************************************************/
    /***************************************************************************************************/

    /**
     * 选择地址
     * @param object
     * @param callback
     */
    @JSMethod
    public void select(String object, JSCallback callback) {
        myApp.select(mWXSDKInstance.getContext(), object, callback);
    }
}
