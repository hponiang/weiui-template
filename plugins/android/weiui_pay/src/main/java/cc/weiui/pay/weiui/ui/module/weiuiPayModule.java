package cc.weiui.pay.weiui.ui.module;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import com.alipay.sdk.app.PayTask;
import com.chinaums.pppay.unify.UnifyPayListener;
import com.chinaums.pppay.unify.UnifyPayPlugin;
import com.chinaums.pppay.unify.UnifyPayRequest;
import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.common.WXModule;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.util.HashMap;
import java.util.Map;

import cc.weiui.framework.extend.module.weiuiCommon;
import cc.weiui.framework.extend.module.weiuiJson;
import cc.weiui.pay.library.alipay.PayResult;
import cc.weiui.pay.library.weixin.PayStatic;

public class weiuiPayModule extends WXModule {

    private static final String TAG = "weiuiPayModule";

    private static final int SDK_PAY_FLAG = 1;

    private Map<String, JSCallback> msgJSCallback = new HashMap<>();

    @SuppressLint("HandlerLeak")
    @SuppressWarnings("unchecked")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                /**
                 * 支付宝结果回调
                 */
                case SDK_PAY_FLAG:
                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                    JSCallback mJSCallback = msgJSCallback.get(payResult.getMsgName());
                    if (mJSCallback != null) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("status", payResult.getResultStatus());
                        data.put("result", payResult.getResult());
                        data.put("memo", payResult.getMemo());
                        mJSCallback.invoke(data);
                        msgJSCallback.remove(payResult.getMsgName());
                        break;
                    }

                default:
                    break;
            }
        }
    };

    /**
     * 微信支付结果回调
     * @param resp
     */
    public static void onWeixinResp(BaseResp resp) {
        if (PayStatic.payCallback != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("status", resp.errCode);
            if (resp.errCode == 0) {
                data.put("msg", "支付成功");
            } else if (resp.errCode == -1) {
                data.put("msg", "可能的原因：签名错误、未注册APPID、项目设置APPID不正确、注册的APPID与设置的不匹配、其他异常等");
            } else if (resp.errCode == -2) {
                data.put("msg", "用户取消");
            } else {
                return;
            }
            PayStatic.payCallback.invoke(data);
            PayStatic.payCallback = null;
        }
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
        PayStatic.payParamets = weiuiJson.parseObject(payData);
        PayStatic.payCallback = callback;
        IWXAPI api = WXAPIFactory.createWXAPI(mWXSDKInstance.getContext(), null);
        api.registerApp(weiuiJson.getString(PayStatic.payParamets, "appid"));
        PayReq request = new PayReq();
        request.appId = weiuiJson.getString(PayStatic.payParamets, "appid");
        request.partnerId = weiuiJson.getString(PayStatic.payParamets, "partnerid");
        request.prepayId = weiuiJson.getString(PayStatic.payParamets, "prepayid");
        request.packageValue = weiuiJson.getString(PayStatic.payParamets, "package");
        request.nonceStr = weiuiJson.getString(PayStatic.payParamets, "noncestr");
        request.timeStamp = weiuiJson.getString(PayStatic.payParamets, "timestamp");
        request.sign = weiuiJson.getString(PayStatic.payParamets, "sign");
        if (!api.sendReq(request)) {
            if (PayStatic.payCallback != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("status", -999);
                data.put("msg", "启动微信支付失败");
                PayStatic.payCallback.invoke(data);
                PayStatic.payCallback = null;
            }
        }
    }

    /**
     * 官方支付宝支付
     *
     * @param payData
     * @param callback
     */
    @JSMethod
    public void alipay(String payData, JSCallback callback) {
        final String orderInfo = payData;
        final String msgName = "JSCallback_" + weiuiCommon.randomString(6);
        msgJSCallback.put(msgName, callback);
        Runnable payRunnable = new Runnable() {

            @Override
            public void run() {
                PayTask alipay = new PayTask((Activity) mWXSDKInstance.getContext());
                Map<String, String> result = alipay.payV2(orderInfo, true);
                result.put("msgName", msgName);

                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    /**
     * 银联微信支付（无回调功能）
     *
     * @param payData
     */
    @JSMethod
    public void union_weixin(String payData) {
        UnifyPayRequest payRequest = new UnifyPayRequest();
        payRequest.payChannel = UnifyPayRequest.CHANNEL_WEIXIN;
        payRequest.payData = payData;
        UnifyPayPlugin.getInstance(mWXSDKInstance.getContext()).setListener(new UnifyPayListener() {
            @Override
            public void onResult(String resultCode, String resultInfo) {

            }
        }).sendPayRequest(payRequest);
    }

    /**
     * 银联支付宝支付（无回调功能）
     *
     * @param payData
     */
    @JSMethod
    public void union_alipay(String payData) {
        UnifyPayRequest payRequest = new UnifyPayRequest();
        payRequest.payChannel = UnifyPayRequest.CHANNEL_ALIPAY;
        payRequest.payData = payData;
        UnifyPayPlugin.getInstance(mWXSDKInstance.getContext()).setListener(new UnifyPayListener() {
            @Override
            public void onResult(String resultCode, String resultInfo) {

            }
        }).sendPayRequest(payRequest);
    }
}

