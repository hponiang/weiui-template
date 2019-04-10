package cc.weiui.framework.ui.module;

import com.alibaba.fastjson.JSONObject;
import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.common.WXModule;

import cc.weiui.framework.ui.weiui;

public class WeexEventModule extends WXModule {

    private weiui __obj;

    private weiui myApp() {
        if (__obj == null) {
            __obj = new weiui();
        }
        return __obj;
    }

    @JSMethod
    public void openURL(String url) {
        JSONObject params = new JSONObject();
        params.put("url", url);
        myApp().openPage(mWXSDKInstance.getContext(), params.toJSONString(), null);
    }
}
