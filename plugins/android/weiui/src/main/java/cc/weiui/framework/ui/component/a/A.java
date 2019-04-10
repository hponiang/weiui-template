package cc.weiui.framework.ui.component.a;

import com.alibaba.fastjson.JSONObject;
import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.dom.WXAttr;
import com.taobao.weex.ui.action.BasicComponentData;
import com.taobao.weex.ui.component.WXDiv;
import com.taobao.weex.ui.component.WXVContainer;
import com.taobao.weex.ui.view.WXFrameLayout;

import java.util.Map;

import cc.weiui.framework.extend.module.weiuiCommon;
import cc.weiui.framework.extend.module.weiuiJson;
import cc.weiui.framework.extend.module.weiuiParse;
import cc.weiui.framework.ui.weiui;


public class A extends WXDiv {

    private JSONObject params = new JSONObject();

    private weiui __obj;

    private weiui myApp() {
        if (__obj == null) {
            __obj = new weiui();
        }
        return __obj;
    }

    public A(WXSDKInstance instance, WXVContainer parent, BasicComponentData basicComponentData) {
        super(instance, parent, basicComponentData);
    }

    @Override
    protected void onHostViewInitialized(WXFrameLayout host) {
        addClickListener(() -> {
            WXAttr attr = getAttrs();
            for (Map.Entry<String, Object> entry : attr.entrySet()) {
                initProperty(entry.getKey(), entry.getValue());
            }
            String url = weiuiJson.getString(params, "url");
            if (url.equals("-1")) {
                myApp().closePage(getContext(), null);
            }else if (!url.equals("")) {
                myApp().openPage(getContext(), params.toJSONString(), null);
            }
        });
        super.onHostViewInitialized(host);
    }

    private void initProperty(String key, Object val) {
        String nkey = weiuiCommon.camelCaseName(key);
        switch (nkey) {
            case "weiui":
                JSONObject json = weiuiJson.parseObject(weiuiParse.parseStr(val, ""));
                if (json.size() > 0) {
                    for (Map.Entry<String, Object> entry : json.entrySet()) {
                        initProperty(entry.getKey(), entry.getValue());
                    }
                }
                return;

            case "href":
                params.put("url", weiuiParse.parseStr(val, null));
                return;

            default:
                if (nkey.startsWith("@")) {
                    return;
                }
                params.put(nkey, val);
        }
    }
}
