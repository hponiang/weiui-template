package cc.weiui.app;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.taobao.weex.bridge.JSCallback;

import java.util.Map;

import cc.weiui.framework.extend.bean.PageBean;
import cc.weiui.framework.extend.module.weiuiMap;
import cc.weiui.framework.extend.module.weiuiPage;
import cc.weiui.framework.extend.module.weiuiParse;
import cc.weiui.playground.R;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //
        new Handler().postDelayed(() -> {
            PageBean mPageBean = new PageBean();
            mPageBean.setUrl(Base.config.getHome());
            mPageBean.setPageName(Base.config.getHomeParams("pageName", "firstPage"));
            mPageBean.setPageType(Base.config.getHomeParams("pageType", "weex"));
            mPageBean.setParams(Base.config.getHomeParams("params", "{}"));
            mPageBean.setCache(weiuiParse.parseLong(Base.config.getHomeParams("cache", "0")));
            mPageBean.setLoading(weiuiParse.parseBool(Base.config.getHomeParams("loading", "true")));
            mPageBean.setStatusBarType(Base.config.getHomeParams("statusBarType", "normal"));
            mPageBean.setStatusBarColor(Base.config.getHomeParams("statusBarColor", "#3EB4FF"));
            mPageBean.setStatusBarAlpha(weiuiParse.parseInt(Base.config.getHomeParams("statusBarAlpha", "0")));
            mPageBean.setSoftInputMode(Base.config.getHomeParams("softInputMode", "auto"));
            mPageBean.setBackgroundColor(Base.config.getHomeParams("backgroundColor", "#f4f8f9"));
            mPageBean.setCallback(new JSCallback() {
                @Override
                public void invoke(Object data) {

                }

                @Override
                public void invokeAndKeepAlive(Object data) {
                    Map<String, Object> retData = weiuiMap.objectToMap(data);
                    String status = weiuiParse.parseStr(retData.get("status"));
                    if (status.equals("create")) {
                        Base.cloud.appData();
                    }
                }
            });
            weiuiPage.openWin(WelcomeActivity.this, mPageBean);
            finish();
        }, Base.cloud.welcome(this));
    }
}
