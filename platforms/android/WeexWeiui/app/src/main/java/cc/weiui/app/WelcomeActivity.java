package cc.weiui.app;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.taobao.weex.bridge.JSCallback;

import java.util.Map;

import cc.weiui.framework.extend.bean.PageBean;
import cc.weiui.framework.extend.module.weiuiBase;
import cc.weiui.framework.extend.module.weiuiMap;
import cc.weiui.framework.extend.module.weiuiPage;
import cc.weiui.framework.extend.module.weiuiParse;
import cc.weiui.framework.ui.weiui;
import cc.weiui.playground.R;

public class WelcomeActivity extends AppCompatActivity {

    private boolean isOpenNext = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(() -> openNext(""), weiuiBase.cloud.welcome(this, new weiuiBase.OnWelcomeListener() {
            @Override
            public void skip() {
                openNext("");
            }

            @Override
            public void finish() {
                openNext("");
            }

            @Override
            public void click(String var) {
                openNext(var);
            }
        }));
    }

    private void openNext(String pageUrl) {
        if (isOpenNext) {
            return;
        }
        isOpenNext = true;
        //
        PageBean mPageBean = new PageBean();
        mPageBean.setUrl(weiuiBase.config.getHome());
        mPageBean.setPageName(weiuiBase.config.getHomeParams("pageName", "firstPage"));
        mPageBean.setPageTitle(weiuiBase.config.getHomeParams("pageTitle", ""));
        mPageBean.setPageType(weiuiBase.config.getHomeParams("pageType", "app"));
        mPageBean.setParams(weiuiBase.config.getHomeParams("params", "{}"));
        mPageBean.setCache(weiuiParse.parseLong(weiuiBase.config.getHomeParams("cache", "0")));
        mPageBean.setLoading(weiuiParse.parseBool(weiuiBase.config.getHomeParams("loading", "true")));
        mPageBean.setStatusBarType(weiuiBase.config.getHomeParams("statusBarType", "normal"));
        mPageBean.setStatusBarColor(weiuiBase.config.getHomeParams("statusBarColor", "#3EB4FF"));
        mPageBean.setStatusBarAlpha(weiuiParse.parseInt(weiuiBase.config.getHomeParams("statusBarAlpha", "0")));
        String statusBarStyle = weiuiBase.config.getHomeParams("statusBarStyle", null);
        if (statusBarStyle != null) {
            mPageBean.setStatusBarStyle(weiuiParse.parseBool(statusBarStyle));
        }
        mPageBean.setSoftInputMode(weiuiBase.config.getHomeParams("softInputMode", "auto"));
        mPageBean.setBackgroundColor(weiuiBase.config.getHomeParams("backgroundColor", "#ffffff"));
        mPageBean.setFirstPage(true);
        mPageBean.setCallback(new JSCallback() {
            @Override
            public void invoke(Object data) {

            }

            @Override
            public void invokeAndKeepAlive(Object data) {
                Map<String, Object> retData = weiuiMap.objectToMap(data);
                String status = weiuiParse.parseStr(retData.get("status"));
                if (status.equals("create")) {
                    weiuiBase.cloud.appData();
                    //
                    if (!"".equals(pageUrl)) {
                        String pageName = weiuiParse.parseStr(retData.get("pageName"));
                        PageBean tmpBean = weiuiPage.getPageBean(pageName);
                        if (tmpBean != null) {
                            JSONObject json = new JSONObject();
                            json.put("url", pageUrl);
                            json.put("pageType", "app");
                            new weiui().openPage(tmpBean.getContext(), json.toJSONString(), null);
                        }
                    }
                }
            }
        });
        weiuiPage.openWin(WelcomeActivity.this, mPageBean);
        finish();
    }
}
