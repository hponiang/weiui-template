package cc.weiui.framework.ui.component.tabbar;

import android.content.Context;
import android.support.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;

import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.ui.action.BasicComponentData;
import com.taobao.weex.ui.component.WXVContainer;

import java.util.HashMap;
import java.util.Map;

import cc.weiui.framework.extend.module.weiuiCommon;
import cc.weiui.framework.extend.module.weiuiJson;
import cc.weiui.framework.extend.module.weiuiParse;
import cc.weiui.framework.ui.component.tabbar.bean.TabbarBean;


/**
 * Created by WDM on 2018/3/9.
 */

public class TabbarPage extends WXVContainer<TabbarPageView> {

    private TabbarPageView mView;

    public TabbarPage(WXSDKInstance instance, WXVContainer parent, BasicComponentData basicComponentData) {
        super(instance, parent, basicComponentData);
    }

    @Override
    protected TabbarPageView initComponentHostView(@NonNull Context context) {
        if (getParent() instanceof Tabbar) {
            mView = new TabbarPageView(context);
            formatAttrs(getAttrs());
            return mView;
        }
        return null;
    }

    private void formatAttrs(Map<String, Object> attr) {
        if (attr != null) {
            TabbarBean barBean = mView.getBarBean();
            for (String key : attr.keySet()) {
                Object value = attr.get(key);
                switch (key) {
                    case "weiui":
                        JSONObject json = weiuiJson.parseObject(weiuiParse.parseStr(value, null));
                        if (json.size() > 0) {
                            Map<String, Object> data = new HashMap<>();
                            for (Map.Entry<String, Object> entry : json.entrySet()) {
                                data.put(entry.getKey(), entry.getValue());
                            }
                            formatAttrs(data);
                        }
                        break;
                }
                barBean = setBarAttr(barBean, key, value);
            }
            mView.setBarBean(barBean);
        }
    }

    public static TabbarBean setBarAttr(TabbarBean barBean, String key, Object value) {
        if (barBean == null) {
            barBean = new TabbarBean();
        }
        switch (weiuiCommon.camelCaseName(key)) {
            case "tabName":
                barBean.setTabName(weiuiParse.parseStr(value, barBean.getTabName()));
                break;

            case "title":
                barBean.setTitle(weiuiParse.parseStr(value, barBean.getTitle()));
                break;

            case "url":
                barBean.setUrl(weiuiParse.parseStr(value, ""));
                break;

            case "unSelectedIcon":
                barBean.setUnSelectedIcon(weiuiParse.parseStr(value, ""));
                break;

            case "selectedIcon":
                barBean.setSelectedIcon(weiuiParse.parseStr(value, ""));
                break;

            case "cache":
                barBean.setCache(weiuiParse.parseLong(value, 0));
                break;

            case "params":
                barBean.setParams(value);
                break;

            case "message":
                barBean.setMessage(weiuiParse.parseInt(value, 0));
                break;

            case "dot":
                barBean.setDot(weiuiParse.parseBool(value, false));
                break;

            case "statusBarColor":
                barBean.setStatusBarColor(weiuiParse.parseStr(value, "#00000000"));
                break;
        }
        return barBean;
    }
}
