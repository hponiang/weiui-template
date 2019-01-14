package cc.weiui.pay.weiui.ui.module;

import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.common.WXModule;

import cc.weiui.pay.weiui.ui.weiui_pay;

public class WeexModule extends WXModule {

    private static final String TAG = "weiuiPayModule";

    private weiui_pay __obj;

    private weiui_pay myApp() {
        if (__obj == null) {
            __obj = new weiui_pay();
        }
        return __obj;
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
    @JSMethod
    public void weixin(String payData, JSCallback callback) {
        myApp().weixin(mWXSDKInstance.getContext(), payData, callback);
    }

    /**
     * 官方支付宝支付
     *
     * @param payData
     * @param callback
     */
    @JSMethod
    public void alipay(String payData, JSCallback callback) {
        myApp().alipay(mWXSDKInstance.getContext(), payData, callback);
    }

    /**
     * 银联微信支付（无回调功能）
     *
     * @param payData
     */
    @JSMethod
    public void union_weixin(String payData) {
        myApp().union_weixin(mWXSDKInstance.getContext(), payData);
    }

    /**
     * 银联支付宝支付（无回调功能）
     *
     * @param payData
     */
    @JSMethod
    public void union_alipay(String payData) {
        myApp().union_alipay(mWXSDKInstance.getContext(), payData);
    }
}

