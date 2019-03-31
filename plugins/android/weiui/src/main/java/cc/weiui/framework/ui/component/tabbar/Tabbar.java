package cc.weiui.framework.ui.component.tabbar;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cc.weiui.framework.BuildConfig;
import cc.weiui.framework.activity.PageActivity;
import cc.weiui.framework.extend.module.weiuiConstants;

import cc.weiui.framework.extend.module.weiuiMap;
import cc.weiui.framework.extend.module.weiuiPage;
import cc.weiui.framework.extend.module.weiuiParse;
import cc.weiui.framework.extend.module.weiuiScreenUtils;
import cc.weiui.framework.extend.view.tablayout.CommonTabLayout;
import cc.weiui.framework.extend.view.tablayout.SlidingTabLayout;
import cc.weiui.framework.extend.view.tablayout.listener.CustomTabEntity;
import cc.weiui.framework.extend.view.tablayout.listener.OnTabSelectListener;

import com.taobao.weex.IWXRenderListener;
import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.bridge.ResultCallback;
import com.taobao.weex.common.Constants;
import com.taobao.weex.common.OnWXScrollListener;
import com.taobao.weex.common.WXRenderStrategy;
import com.taobao.weex.dom.CSSShorthand;
import com.taobao.weex.ui.action.BasicComponentData;
import com.taobao.weex.ui.component.WXComponent;
import cc.weiui.framework.R;
import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.ui.component.WXVContainer;
import cc.weiui.framework.extend.module.weiuiCommon;
import cc.weiui.framework.extend.module.weiuiJson;
import cc.weiui.framework.ui.component.tabbar.adapter.TabbarAdapter;
import cc.weiui.framework.extend.view.NoAnimationViewPager;
import cc.weiui.framework.ui.component.tabbar.bean.TabbarBean;
import cc.weiui.framework.ui.component.tabbar.bean.WXSDKBean;
import cc.weiui.framework.ui.component.tabbar.entity.TabbarEntity;
import cc.weiui.framework.ui.weiui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Tabbar extends WXVContainer<ViewGroup> {

    private static final String TAG = "Tabbar";

    private Handler mHandler = new Handler();

    private View mView;

    private int prevSelectedPosition;

    private String tabbarType;

    private String loadedViewTabName;

    private NoAnimationViewPager mViewPager;

    private TabbarAdapter mTabPagerAdapter;

    private CommonTabLayout mTabLayoutTop;

    private SlidingTabLayout mTabLayoutSlidingTop;

    private CommonTabLayout mTabLayoutBottom;

    private ResultCallback<String> resultCallback = result -> {
        for (Map.Entry<String, WXSDKBean> entry : mViewPager.WXSDKList.entrySet()) {
            if (entry.getValue().isLoaded()) {
                String url = String.valueOf(entry.getValue().getView());
                if (url.startsWith(result)) {
                    addWXSDKView(entry.getKey());
                }
            }
        }
    };

    public Tabbar(WXSDKInstance instance, WXVContainer parent, BasicComponentData basicComponentData) {
        super(instance, parent, basicComponentData);
        Map<String, Object> custom = new HashMap<>();
        custom.put("tabHeight", 100);
        custom.put("indicatorHeight", 4);
        custom.put("indicatorWidth", 20);
        custom.put("indicatorCornerRadius", 2);
        custom.put("dividerPadding", 12);
        custom.put("textSize", 26);
        custom.put("iconWidth", 40);
        custom.put("iconHeight", 40);
        custom.put("iconMargin", 10);
        Map<String, Object> newCustom = weiuiCommon.filterAlreadyStyle(basicComponentData, custom);
        newCustom.put(Constants.Name.FLEX_DIRECTION, "row");
        updateNativeStyles(newCustom);
    }

    @Override
    protected ViewGroup initComponentHostView(@NonNull Context context) {
        mView = ((Activity) context).getLayoutInflater().inflate(R.layout.layout_weiui_tabbar, null);
        initPagerView();
        //
        if (getEvents().contains(weiuiConstants.Event.READY)) {
            fireEvent(weiuiConstants.Event.READY, null);
        }
        //
        if (context instanceof PageActivity) {
            ((PageActivity) context).setPageStatusListener("__Tabbar::" + weiuiCommon.randomString(6), new JSCallback() {
                @Override
                public void invoke(Object data) {
                    Map<String, Object> retData = weiuiMap.objectToMap(data);
                    mViewPager.lifecycleListener(mViewPager.getCurrentItem(), weiuiParse.parseStr(retData.get("status")));
                }

                @Override
                public void invokeAndKeepAlive(Object data) {
                    Map<String, Object> retData = weiuiMap.objectToMap(data);
                    mViewPager.lifecycleListener(mViewPager.getCurrentItem(), weiuiParse.parseStr(retData.get("status")));
                }
            });
        }
        //
        if (BuildConfig.DEBUG) {
            PageActivity.setTabViewDebug(resultCallback);
        }
        //
        return (ViewGroup) mView;
    }

    @Override
    public void destroy() {
        if (BuildConfig.DEBUG) {
            PageActivity.removeTabViewDebug(resultCallback);
        }
        for (WXSDKBean item : mViewPager.WXSDKList.values()) {
            WXSDKInstance mSdk = item.getInstance();
            if (mSdk != null) mSdk.destroy();
        }
        super.destroy();
    }

    @Override
    public void addSubView(View view, int index) {
        if (view == null || getRealView() == null) {
            return;
        }
        if (view instanceof TabbarPageView) {
            setFirstDraw(true);
            TabbarPageView newView = (TabbarPageView) view;
            TabbarBean barBean = newView.getBarBean();
            barBean.setView(newView);
            addPageView(barBean);
            setTabData();
        }
    }

    @Override
    public ViewGroup.LayoutParams getChildLayoutParams(WXComponent child, View childView, int width, int height, int left, int right, int top, int bottom) {
        ViewGroup.LayoutParams lp = childView == null ? null : childView.getLayoutParams();
        if (lp == null) {
            lp = new FrameLayout.LayoutParams(width, height);
        } else {
            lp.width = width;
            lp.height = height;
        }
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            left = weiuiScreenUtils.weexDp2px(getInstance(), child.getMargin().get(CSSShorthand.EDGE.LEFT));
            left = weiuiScreenUtils.weexPx2dp(getInstance(), left, 0);
            ((ViewGroup.MarginLayoutParams) lp).setMargins(left, top, right, bottom);
        }
        return lp;
    }

    @Override
    protected boolean setProperty(String key, Object param) {
        return initProperty(key, param) || super.setProperty(key, param);
    }

    private boolean initProperty(String key, Object val) {
        switch (weiuiCommon.camelCaseName(key)) {
            case "weiui":
                JSONObject json = weiuiJson.parseObject(weiuiParse.parseStr(val, ""));
                if (json.size() > 0) {
                    for (Map.Entry<String, Object> entry : json.entrySet()) {
                        initProperty(entry.getKey(), entry.getValue());
                    }
                }
                return true;

            case "tabPages":
                removePageAll();
                JSONArray pagesArray = weiuiJson.parseArray(weiuiParse.parseStr(val, null));
                if (pagesArray.size() > 0) {
                    for (int i = 0; i < pagesArray.size(); i++) {
                        JSONObject item = pagesArray.getJSONObject(i);
                        if (item != null) {
                            TabbarBean barBean = new TabbarBean();
                            for (Map.Entry<String, Object> entry : item.entrySet()) {
                                barBean = TabbarPage.setBarAttr(barBean, entry.getKey(), entry.getValue());
                            }
                            barBean.setView(weiuiPage.rewriteUrl(getContext(), barBean.getUrl()));
                            addPageView(barBean);
                        }
                    }
                    setTabData();
                }
                return true;

            case "tabType":
                setTabType(val);
                return true;

            case "tabHeight":
                setTabHeight(val);
                return true;

            case "indicatorStyle":
                mTabLayoutTop.setIndicatorStyle(weiuiParse.parseInt(val, 0));
                mTabLayoutSlidingTop.setIndicatorStyle(weiuiParse.parseInt(val, 0));
                mTabLayoutBottom.setIndicatorStyle(weiuiParse.parseInt(val, 0));
                return true;

            case "tabPadding":
                mTabLayoutTop.setTabPadding(weiuiScreenUtils.weexPx2dp(getInstance(), val, 0));
                mTabLayoutSlidingTop.setTabPadding(weiuiScreenUtils.weexPx2dp(getInstance(), val, 0));
                mTabLayoutBottom.setTabPadding(weiuiScreenUtils.weexPx2dp(getInstance(), val, 0));
                return true;

            case "tabBackgroundColor":
                setTabBackgroundColor(val);
                return true;

            case "tabSpaceEqual":
                mTabLayoutTop.setTabSpaceEqual(weiuiParse.parseBool(val, false));
                mTabLayoutSlidingTop.setTabSpaceEqual(weiuiParse.parseBool(val, false));
                mTabLayoutBottom.setTabSpaceEqual(weiuiParse.parseBool(val, false));
                return true;

            case "tabWidth":
                mTabLayoutTop.setTabWidth(weiuiScreenUtils.weexPx2dp(getInstance(), val, 0));
                mTabLayoutSlidingTop.setTabWidth(weiuiScreenUtils.weexPx2dp(getInstance(), val, 0));
                mTabLayoutBottom.setTabWidth(weiuiScreenUtils.weexPx2dp(getInstance(), val, 0));
                return true;

            case "tabPageAnimated":
                setTabPageAnimated(weiuiParse.parseBool(val, false));
                return true;

            case "indicatorColor":
                mTabLayoutTop.setIndicatorColor(weiuiParse.parseColor(String.valueOf(val)));
                mTabLayoutSlidingTop.setIndicatorColor(weiuiParse.parseColor(String.valueOf(val)));
                mTabLayoutBottom.setIndicatorColor(weiuiParse.parseColor(String.valueOf(val)));
                return true;

            case "indicatorHeight":
                mTabLayoutTop.setIndicatorHeight(weiuiScreenUtils.weexPx2dp(getInstance(), val, 0));
                mTabLayoutSlidingTop.setIndicatorHeight(weiuiScreenUtils.weexPx2dp(getInstance(), val, 0));
                mTabLayoutBottom.setIndicatorHeight(weiuiScreenUtils.weexPx2dp(getInstance(), val, 0));
                return true;

            case "indicatorWidth":
                mTabLayoutTop.setIndicatorWidth(weiuiScreenUtils.weexPx2dp(getInstance(), val, 0));
                mTabLayoutSlidingTop.setIndicatorWidth(weiuiScreenUtils.weexPx2dp(getInstance(), val, 0));
                mTabLayoutBottom.setIndicatorWidth(weiuiScreenUtils.weexPx2dp(getInstance(), val, 0));
                return true;

            case "indicatorCornerRadius":
                mTabLayoutTop.setIndicatorCornerRadius(weiuiScreenUtils.weexPx2dp(getInstance(), val, 0));
                mTabLayoutSlidingTop.setIndicatorCornerRadius(weiuiScreenUtils.weexPx2dp(getInstance(), val, 0));
                mTabLayoutBottom.setIndicatorCornerRadius(weiuiScreenUtils.weexPx2dp(getInstance(), val, 0));
                return true;

            case "indicatorGravity":
                if (weiuiParse.parseInt(val, 0) == 1) {
                    mTabLayoutTop.setIndicatorGravity(Gravity.TOP);
                    mTabLayoutSlidingTop.setIndicatorGravity(Gravity.TOP);
                    mTabLayoutBottom.setIndicatorGravity(Gravity.TOP);
                }else{
                    mTabLayoutTop.setIndicatorGravity(Gravity.BOTTOM);
                    mTabLayoutSlidingTop.setIndicatorGravity(Gravity.BOTTOM);
                    mTabLayoutBottom.setIndicatorGravity(Gravity.BOTTOM);
                }
                return true;

            case "indicatorAnimDuration":
                mTabLayoutTop.setIndicatorAnimDuration(weiuiParse.parseLong(String.valueOf(val)));
                mTabLayoutBottom.setIndicatorAnimDuration(weiuiParse.parseLong(String.valueOf(val)));
                return true;

            case "indicatorAnimEnable":
                mTabLayoutTop.setIndicatorAnimEnable(weiuiParse.parseBool(val, true));
                mTabLayoutBottom.setIndicatorAnimEnable(weiuiParse.parseBool(val, true));
                return true;

            case "indicatorBounceEnable":
                mTabLayoutTop.setIndicatorBounceEnable(weiuiParse.parseBool(val, true));
                mTabLayoutBottom.setIndicatorBounceEnable(weiuiParse.parseBool(val, true));
                return true;

            case "underlineColor":
                mTabLayoutTop.setUnderlineColor(weiuiParse.parseColor(String.valueOf(val)));
                mTabLayoutSlidingTop.setUnderlineColor(weiuiParse.parseColor(String.valueOf(val)));
                mTabLayoutBottom.setUnderlineColor(weiuiParse.parseColor(String.valueOf(val)));
                return true;

            case "underlineHeight":
                mTabLayoutTop.setUnderlineHeight(weiuiScreenUtils.weexPx2dpFloat(getInstance(), val, 0));
                mTabLayoutSlidingTop.setUnderlineHeight(weiuiScreenUtils.weexPx2dpFloat(getInstance(), val, 0));
                mTabLayoutBottom.setUnderlineHeight(weiuiScreenUtils.weexPx2dpFloat(getInstance(), val, 0));
                return true;

            case "underlineGravity":
                if (weiuiParse.parseInt(val, 0) == 1) {
                    mTabLayoutTop.setUnderlineGravity(Gravity.TOP);
                    mTabLayoutSlidingTop.setUnderlineGravity(Gravity.TOP);
                    mTabLayoutBottom.setUnderlineGravity(Gravity.TOP);
                }else{
                    mTabLayoutTop.setUnderlineGravity(Gravity.BOTTOM);
                    mTabLayoutSlidingTop.setUnderlineGravity(Gravity.BOTTOM);
                    mTabLayoutBottom.setUnderlineGravity(Gravity.BOTTOM);
                }
                return true;

            case "dividerColor":
                mTabLayoutTop.setDividerColor(weiuiParse.parseColor(String.valueOf(val)));
                mTabLayoutSlidingTop.setDividerColor(weiuiParse.parseColor(String.valueOf(val)));
                mTabLayoutBottom.setDividerColor(weiuiParse.parseColor(String.valueOf(val)));
                return true;

            case "dividerWidth":
                mTabLayoutTop.setDividerWidth(weiuiScreenUtils.weexPx2dp(getInstance(), val, 0));
                mTabLayoutSlidingTop.setDividerWidth(weiuiScreenUtils.weexPx2dp(getInstance(), val, 0));
                mTabLayoutBottom.setDividerWidth(weiuiScreenUtils.weexPx2dp(getInstance(), val, 0));
                return true;

            case "dividerPadding":
                mTabLayoutTop.setDividerPadding(weiuiScreenUtils.weexPx2dp(getInstance(), val, 0));
                mTabLayoutSlidingTop.setDividerPadding(weiuiScreenUtils.weexPx2dp(getInstance(), val, 0));
                mTabLayoutBottom.setDividerPadding(weiuiScreenUtils.weexPx2dp(getInstance(), val, 0));
                return true;

            case "textSize":
            case "fontSize":
                setTabTextsize(val);
                return true;

            case "textSelectColor":
                setTabTextSelectColor(val);
                return true;

            case "textUnselectColor":
                setTabTextUnselectColor(val);
                return true;

            case "textBold":
                setTabTextBold(val);
                return true;

            case "iconVisible":
                setTabIconVisible(val);
                return true;

            case "iconGravity":
                if (weiuiParse.parseInt(val, 0) == 1) {
                    mTabLayoutTop.setIconGravity(Gravity.TOP);
                    mTabLayoutBottom.setIconGravity(Gravity.TOP);
                }else{
                    mTabLayoutTop.setIconGravity(Gravity.BOTTOM);
                    mTabLayoutBottom.setIconGravity(Gravity.BOTTOM);
                }
                return true;

            case "iconWidth":
                setTabIconWidth(val);
                return true;

            case "iconHeight":
                setTabIconHeight(val);
                return true;

            case "iconMargin":
                mTabLayoutTop.setIconMargin(weiuiScreenUtils.weexPx2dp(getInstance(), val, 0));
                mTabLayoutBottom.setIconMargin(weiuiScreenUtils.weexPx2dp(getInstance(), val, 0));
                return true;

            case "textAllCaps":
                mTabLayoutTop.setTextAllCaps(weiuiParse.parseBool(val, false));
                mTabLayoutSlidingTop.setTextAllCaps(weiuiParse.parseBool(val, false));
                mTabLayoutBottom.setTextAllCaps(weiuiParse.parseBool(val, false));
                return true;

            case "@styleScope":
                if (tabbarType == null) {
                    setTabType("bottom");
                }
                return false;

            default:
                return false;
        }
    }

    /**
     * 初始化组件
     */
    private void initPagerView() {
        mViewPager = mView.findViewById(R.id.viewpager);
        mTabLayoutTop = mView.findViewById(R.id.navigation_top);
        mTabLayoutSlidingTop = mView.findViewById(R.id.navigation_sliding_top);
        mTabLayoutBottom = mView.findViewById(R.id.navigation_bottom);
        //
        mTabPagerAdapter = new TabbarAdapter(mViewPager.ViewList);
        mViewPager.setSmoothScroll(true);
        mViewPager.setAdapter(mTabPagerAdapter);
        mViewPager.setCurrentItem(0);
        mViewPager.addOnPageChangeListener(mOnPageChangeListener);
        mTabLayoutTop.setOnTabSelectListener(mOnTabSelectListener);
        mTabLayoutSlidingTop.setOnTabSelectListener(mOnTabSelectListener);
        mTabLayoutBottom.setOnTabSelectListener(mOnTabSelectListener);
        //
        mViewPager.setAdapter(mTabPagerAdapter);
        mTabLayoutSlidingTop.setViewPager(mViewPager);
    }

    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (getEvents().contains(weiuiConstants.Event.PAGE_SCROLLED)) {
                Map<String, Object> data = new HashMap<>();
                data.put("position", position);
                data.put("positionOffset", positionOffset);
                data.put("positionOffsetPixels", positionOffsetPixels);
                fireEvent(weiuiConstants.Event.PAGE_SCROLLED, data);
            }
        }

        @Override
        public void onPageSelected(int position) {
            mTabLayoutTop.setCurrentTab(position);
            //mTabLayoutSlidingTop.setCurrentTab(position);
            mTabLayoutBottom.setCurrentTab(position);
            //
            if (getEvents().contains(weiuiConstants.Event.PAGE_SELECTED)) {
                Map<String, Object> data = new HashMap<>();
                data.put("position", position);
                fireEvent(weiuiConstants.Event.PAGE_SELECTED, data);
            }
            loadedView(position);
            //
            mViewPager.lifecycleListener(prevSelectedPosition, "pause");
            mViewPager.lifecycleListener(position, "resume");
            //
            prevSelectedPosition = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (getEvents().contains(weiuiConstants.Event.PAGE_SCROLL_STATE_CHANGED)) {
                Map<String, Object> data = new HashMap<>();
                data.put("state", state);
                fireEvent(weiuiConstants.Event.PAGE_SCROLL_STATE_CHANGED, data);
            }
        }
    };

    private OnTabSelectListener mOnTabSelectListener = new OnTabSelectListener() {
        @Override
        public void onTabSelect(int position) {
            mViewPager.setCurrentItem(position);
            if (getEvents().contains(weiuiConstants.Event.TAB_SELECT)) {
                Map<String, Object> data = new HashMap<>();
                data.put("position", position);
                fireEvent(weiuiConstants.Event.TAB_SELECT, data);
            }
        }

        @Override
        public void onTabReselect(int position) {
            if (getEvents().contains(weiuiConstants.Event.TAB_RESELECT)) {
                Map<String, Object> data = new HashMap<>();
                data.put("position", position);
                fireEvent(weiuiConstants.Event.TAB_RESELECT, data);
            }
        }
    };

    /**
     * 添加页面
     * @param barBean
     */
    private void addPageView(TabbarBean barBean) {
        View view = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.layout_weiui_tabbar_page, null);
        //
        WXSDKBean sdkBean = new WXSDKBean();
        sdkBean.setSwipeRefresh(view.findViewById(R.id.v_swipeRefresh));
        sdkBean.setContainer(view.findViewById(R.id.v_container));
        sdkBean.setProgress(view.findViewById(R.id.v_progress));
        sdkBean.setErrorView(view.findViewById(R.id.v_error));
        sdkBean.setErrorCodeView(view.findViewById(R.id.v_error_code));
        sdkBean.setCache(barBean.getCache());
        sdkBean.setParams(barBean.getParams());
        sdkBean.setView(barBean.getView());
        //
        if (!barBean.getStatusBarColor().isEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View statusBar = view.findViewById(R.id.v_statusBar);
            statusBar.setVisibility(View.VISIBLE);
            statusBar.setBackgroundColor(weiuiParse.parseColor(barBean.getStatusBarColor()));
            weiuiCommon.setViewWidthHeight(statusBar, -1, weiuiCommon.getStatusBarHeight(getContext()));
        }
        //
        if (barBean.getView() instanceof String) {
            sdkBean.setType("urlView");
            mViewPager.WXSDKList.put(barBean.getTabName(), sdkBean);
        }else if (barBean.getView() instanceof TabbarPageView) {
            sdkBean.setType("pageView");
            mViewPager.WXSDKList.put(barBean.getTabName(), sdkBean);
        }
        //
        view.findViewById(R.id.v_error_title).setOnClickListener(v -> {
            view.findViewById(R.id.v_error_cbox).setVisibility(View.VISIBLE);
        });
        view.findViewById(R.id.v_refresh).setOnClickListener(v -> {
            view.findViewById(R.id.v_error).setVisibility(View.GONE);
            view.findViewById(R.id.v_error_cbox).setVisibility(View.GONE);
            reload(barBean.getTabName());
        });
        view.findViewById(R.id.v_back).setVisibility(View.GONE);
        //
        if (getEvents().contains(weiuiConstants.Event.REFRESH_LISTENER)) {
            sdkBean.getSwipeRefresh().setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
            sdkBean.getSwipeRefresh().setOnRefreshListener(() -> {
                Map<String, Object> data = new HashMap<>();
                data.put("tabName", barBean.getTabName());
                data.put("position", getTabPosition(barBean.getTabName()));
                fireEvent(weiuiConstants.Event.REFRESH_LISTENER, data);
            });
            sdkBean.setSwipeRefreshEnable(true);
        }else{
            sdkBean.setSwipeRefreshEnable(false);
            sdkBean.getSwipeRefresh().setEnabled(false);
        }
        //
        view.setTag(barBean.getTitle());
        mViewPager.TabEntity.add(new TabbarEntity(barBean));
        mViewPager.ViewList.add(view);
        mTabPagerAdapter.setListViews(mViewPager.ViewList);
        mTabPagerAdapter.notifyDataSetChanged();
        mTabLayoutSlidingTop.addNewTab(barBean.getTitle());
        //
        if (mViewPager.WXSDKList.size() == 1) {
            loadedView(0);
        }
    }

    /**
     * 首次加载页面
     * @param position
     */
    private void loadedView(int position) {
        if (position < mViewPager.WXSDKList.size()) {
            String getTabName = getTabName(position);
            WXSDKBean sdkBean = mViewPager.WXSDKList.get(getTabName);
            if (sdkBean != null) {
                if (!sdkBean.isLoaded()) {
                    sdkBean.setLoaded(true);
                    mViewPager.WXSDKList.put(getTabName, sdkBean);
                    if (sdkBean.getType().equals("urlView")) {
                        addWXSDKView(getTabName);
                    }else if (sdkBean.getType().equals("pageView")) {
                        addWXPageView(getTabName);
                    }
                }
                if (sdkBean.getType().equals("urlView")) {
                    if (loadedViewTabName != null && !loadedViewTabName.equals(getTabName)) {
                        WXSDKBean lastSdkBean = mViewPager.WXSDKList.get(loadedViewTabName);
                        if (lastSdkBean != null) {
                            if (lastSdkBean.getInstance() != null) {
                                lastSdkBean.getInstance().onActivityPause();
                            }
                        }
                        if (sdkBean.getInstance() != null) {
                            sdkBean.getInstance().onActivityResume();
                        }
                    }
                    loadedViewTabName = getTabName;
                }
            }
        }
    }

    /**
     * 添加、刷新 浏览器页面
     * @param tabName
     */
    private void addWXSDKView(String tabName) {
        WXSDKBean sdkBean = mViewPager.WXSDKList.get(tabName);
        if (sdkBean == null || !sdkBean.getType().equals("urlView")) {
            return;
        }
        String url = String.valueOf(sdkBean.getView());
        //
        if (sdkBean.getInstance() != null) {
            sdkBean.getInstance().registerRenderListener(null);
            sdkBean.getInstance().destroy();
        }
        //
        sdkBean.getProgress().setVisibility(View.INVISIBLE);
        mHandler.postDelayed(()-> sdkBean.getProgress().post(()->{
            if (sdkBean.getProgress().getVisibility() == View.INVISIBLE) {
                sdkBean.getProgress().setVisibility(View.VISIBLE);
            }
        }), 200);
        sdkBean.setInstance(new WXSDKInstance(getContext()));
        sdkBean.getInstance().registerRenderListener(new IWXRenderListener() {
            @Override
            public void onViewCreated(WXSDKInstance instance, View view) {
                sdkBean.getProgress().setVisibility(View.GONE);
                sdkBean.getContainer().removeAllViews();
                sdkBean.getContainer().addView(view);
                //
                if (getEvents().contains(weiuiConstants.Event.VIEW_CREATED)) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("tabName", tabName);
                    data.put("url", url);
                    fireEvent(weiuiConstants.Event.VIEW_CREATED, data);
                }
                mViewPager.lifecycleListener(mViewPager.getCurrentItem(), "WXSDKViewCreated");
                mViewPager.lifecycleListener(mViewPager.getCurrentItem(), "resume");
            }
            @Override
            public void onRenderSuccess(WXSDKInstance instance, int width, int height) {

            }
            @Override
            public void onRefreshSuccess(WXSDKInstance instance, int width, int height) {

            }
            @Override
            public void onException(WXSDKInstance instance, String errCode, String msg) {
                if (errCode == null) {
                    errCode = "";
                }
                if (weiui.debug || errCode.equals("-1002") || errCode.equals("-1003")) {
                    sdkBean.getErrorView().setVisibility(View.VISIBLE);
                    sdkBean.getErrorCodeView().setText(String.valueOf(errCode));
                }
            }
        });
        //
        sdkBean.getInstance().registerOnWXScrollListener(new OnWXScrollListener() {
            @Override
            public void onScrolled(View view, int x, int y) {
                if (getEvents().contains(weiuiConstants.Event.SCROLLED)) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("tabName", tabName);
                    data.put("x", x);
                    data.put("y", y);
                    fireEvent(weiuiConstants.Event.SCROLLED, data);
                }
            }

            @Override
            public void onScrollStateChanged(View view, int x, int y, int newState) {
                if (getEvents().contains(weiuiConstants.Event.SCROLL_STATE_CHANGED)) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("tabName", tabName);
                    data.put("x", x);
                    data.put("y", y);
                    data.put("newState", newState);
                    fireEvent(weiuiConstants.Event.SCROLL_STATE_CHANGED, data);
                }
                if (sdkBean.getSwipeRefreshEnable()) {
                    if (y == 0) {
                        sdkBean.getSwipeRefresh().setEnabled(true);
                    }else{
                        sdkBean.getSwipeRefresh().setEnabled(false);
                    }
                }
            }
        });
        //
        weiuiPage.cachePage(url, sdkBean.getCache(), sdkBean.getParams(), new weiuiPage.OnCachePageCallback() {
            @Override
            public void success(Map<String, Object> resParams, String resData) {
                sdkBean.getInstance().render(tabName, resData, resParams, null, WXRenderStrategy.APPEND_ASYNC);
            }

            @Override
            public void error(Map<String, Object> resParams) {
                sdkBean.getInstance().renderByUrl("Tabbar:" + tabName, url, resParams, null, WXRenderStrategy.APPEND_ASYNC);
            }

            @Override
            public void complete(Map<String, Object> resParams) {

            }
        });
    }

    /**
     * 添加子页面
     * @param tabName
     */
    private void addWXPageView(String tabName) {
        WXSDKBean sdkBean = mViewPager.WXSDKList.get(tabName);
        if (sdkBean == null || !sdkBean.getType().equals("pageView")) {
            return;
        }
        TabbarPageView view = (TabbarPageView) sdkBean.getView();
        if (view == null) {
            return;
        }
        //
        ViewGroup parentViewGroup = (ViewGroup) view.getParent();
        if (parentViewGroup != null ) {
            parentViewGroup.removeView(view);
        }
        sdkBean.getProgress().setVisibility(View.GONE);
        sdkBean.getContainer().removeAllViews();
        sdkBean.getContainer().addView(view);
        //
        if (getEvents().contains(weiuiConstants.Event.VIEW_CREATED)) {
            Map<String, Object> data = new HashMap<>();
            data.put("tabName", tabName);
            data.put("url", null);
            fireEvent(weiuiConstants.Event.VIEW_CREATED, data);
        }
    }

    /**
     * 设置tab数据
     */
    private void setTabData() {
        mTabLayoutTop.setTabData(mViewPager.TabEntity);
        mTabLayoutBottom.setTabData(mViewPager.TabEntity);
        //
        if (mViewPager.TabEntity != null && mViewPager.TabEntity.size() > 0) {
            for (int i = 0; i < mViewPager.TabEntity.size(); i++) {
                CustomTabEntity temp = mViewPager.TabEntity.get(i);
                String tabName = temp.getTabName();
                int message = temp.getTabMessage();
                if (message > 0) {
                    showMsg(tabName, message);
                } else {
                    boolean dot = temp.isTabDot();
                    if (dot) {
                        showDot(tabName);
                    } else {
                        hideMsg(tabName);
                    }
                }
            }
        }
    }

    /***************************************************************************************************/
    /***************************************************************************************************/
    /***************************************************************************************************/

    /**
     * 根据名称获取位置
     * @param tabName
     * @return
     */
    @JSMethod(uiThread = false)
    public int getTabPosition(String tabName) {
        if (mViewPager == null) {
            return -1;
        }
        if (mViewPager.TabEntity != null && mViewPager.TabEntity.size() > 0) {
            for (int i = 0; i < mViewPager.TabEntity.size(); i++) {
                String temp = mViewPager.TabEntity.get(i).getTabName();
                if (temp != null && temp.equals(tabName)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 根据位置获取名称
     * @param position
     * @return
     */
    @JSMethod(uiThread = false)
    public String getTabName(int position) {
        if (mViewPager == null) {
            return null;
        }
        return mViewPager.getTabName(position);
    }

    /**
     * 显示未读信息
     * @param tabName
     * @param message
     */
    @JSMethod
    public void showMsg(String tabName, int message) {
        int position = getTabPosition(tabName);
        if (position > -1) {
            mTabLayoutTop.showMsg(position, message);
            mTabLayoutSlidingTop.showMsg(position, message);
            mTabLayoutBottom.showMsg(position, message);
        }
    }

    /**
     * 显示未读红点
     * @param tabName
     */
    @JSMethod
    public void showDot(String tabName){
        int position = getTabPosition(tabName);
        if (position > -1) {
            mTabLayoutTop.showDot(position);
            mTabLayoutSlidingTop.showDot(position);
            mTabLayoutBottom.showDot(position);
        }
    }

    /**
     * 隐藏未读信息、未读红点
     * @param tabName
     */
    @JSMethod
    public void hideMsg(String tabName){
        int position = getTabPosition(tabName);
        if (position > -1) {
            mTabLayoutTop.hideMsg(position);
            mTabLayoutSlidingTop.hideMsg(position);
            mTabLayoutBottom.hideMsg(position);
        }
    }

    /**
     * 删除所有页面
     */
    @JSMethod
    public void removePageAll() {
        if (mViewPager == null) {
            return;
        }
        mViewPager.WXSDKList = new HashMap<>();
        mViewPager.TabEntity = new ArrayList<>();
        setTabData();
        //
        mViewPager.ViewList = new ArrayList<>();
        mViewPager.removeAllViews();
        mTabPagerAdapter.setListViews(mViewPager.ViewList);
        mTabPagerAdapter.notifyDataSetChanged();
        mTabLayoutSlidingTop.notifyDataSetChanged();
    }

    /**
     * 删除指定页面
     * @param tabName
     */
    @JSMethod
    public void removePageAt(String tabName) {
        int position = getTabPosition(tabName);
        if (position > -1) {
            if (mViewPager.getCurrentItem() >= mViewPager.TabEntity.size() - 1) {
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
            }
            mViewPager.WXSDKList.remove(tabName);
            mViewPager.TabEntity.remove(position);
            setTabData();
            //
            mViewPager.ViewList.remove(position);
            mViewPager.removeAllViews();
            mTabPagerAdapter.setListViews(mViewPager.ViewList);
            mTabPagerAdapter.notifyDataSetChanged();
            mTabLayoutSlidingTop.notifyDataSetChanged();
        }
    }

    /**
     * 切换页面
     * @param tabName
     */
    @JSMethod
    public void setCurrentItem(String tabName) {
        int position = getTabPosition(tabName);
        if (position > -1) {
            mViewPager.setCurrentItem(position);
        }
    }

    /**
     * 设置下拉刷新状态
     * @param tabName
     * @param refreshing
     */
    @JSMethod
    public void setRefreshing(String tabName, boolean refreshing){
        if (mViewPager == null) {
            return;
        }
        WXSDKBean temp = mViewPager.WXSDKList.get(tabName);
        if (temp != null) {
            temp.getSwipeRefresh().setRefreshing(refreshing);
        }
    }

    /**
     * 跳转页面
     * @param tabName
     * @param url
     */
    @JSMethod
    public void goUrl(String tabName, String url) {
        if (mViewPager == null) {
            return;
        }
        WXSDKBean bean = mViewPager.WXSDKList.get(tabName);
        if (bean != null) {
            bean.setView(url);
            mViewPager.WXSDKList.put(tabName, bean);
            addWXSDKView(tabName);
        }
    }

    /**
     * 刷新页面
     * @param tabName
     */
    @JSMethod
    public void reload(String tabName) {
        addWXSDKView(tabName);
    }

    /**
     * 设置导航栏显示位置
     * @param var
     */
    @JSMethod
    public void setTabType(Object var) {
        String tabType = weiuiParse.parseStr(var, "bottom");
        tabbarType = tabType.toLowerCase();
        //
        if (tabbarType.contains("top")) {
            mTabLayoutTop.setVisibility(View.VISIBLE);
            mTabLayoutSlidingTop.setVisibility(View.GONE);
            mTabLayoutBottom.setVisibility(View.GONE);

        }
        if (tabbarType.contains("slidingtop")) {
            mTabLayoutTop.setVisibility(View.GONE);
            mTabLayoutSlidingTop.setVisibility(View.VISIBLE);
            mTabLayoutBottom.setVisibility(View.GONE);

        }
        if (tabbarType.contains("bottom")) {
            mTabLayoutTop.setVisibility(View.GONE);
            mTabLayoutSlidingTop.setVisibility(View.GONE);
            mTabLayoutBottom.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 设置导航栏高度
     * @param var
     */
    @JSMethod
    public void setTabHeight(Object var) {
        weiuiCommon.setViewWidthHeight(mTabLayoutTop, -1, weiuiScreenUtils.weexPx2dp(getInstance(), weiuiParse.parseInt(var, 0)));
        weiuiCommon.setViewWidthHeight(mTabLayoutSlidingTop, -1, weiuiScreenUtils.weexPx2dp(getInstance(), weiuiParse.parseInt(var, 0)));
        weiuiCommon.setViewWidthHeight(mTabLayoutBottom, -1, weiuiScreenUtils.weexPx2dp(getInstance(), weiuiParse.parseInt(var, 0)));
    }

    /**
     * 设置导航栏背景颜色
     * @param var
     */
    @JSMethod
    public void setTabBackgroundColor(Object var) {
        mTabLayoutTop.setBackgroundColor(weiuiParse.parseColor(weiuiParse.parseStr(var)));
        mTabLayoutSlidingTop.setBackgroundColor(weiuiParse.parseColor(weiuiParse.parseStr(var)));
        mTabLayoutBottom.setBackgroundColor(weiuiParse.parseColor(weiuiParse.parseStr(var)));
    }

    /**
     * 设置导航栏字体大小
     * @param var
     */
    @JSMethod
    public void setTabTextsize(Object var) {
        int size = weiuiScreenUtils.weexPx2dp(getInstance(), var, 26);
        mTabLayoutTop.setTextsizePx(size);
        mTabLayoutSlidingTop.setTextsizePx(size);
        mTabLayoutBottom.setTextsizePx(size);
    }

    /**
     * 设置导航栏字体加粗
     * @param var
     */
    @JSMethod
    public void setTabTextBold(Object var) {
        mTabLayoutTop.setTextBold(weiuiParse.parseInt(var, 0));
        mTabLayoutSlidingTop.setTextBold(weiuiParse.parseInt(var, 0));
        mTabLayoutBottom.setTextBold(weiuiParse.parseInt(var, 0));
    }

    /**
     * 设置导航栏未选字体颜色
     * @param var
     */
    @JSMethod
    public void setTabTextUnselectColor(Object var) {
        mTabLayoutTop.setTextUnselectColor(weiuiParse.parseColor(weiuiParse.parseStr(var)));
        mTabLayoutSlidingTop.setTextUnselectColor(weiuiParse.parseColor(weiuiParse.parseStr(var)));
        mTabLayoutBottom.setTextUnselectColor(weiuiParse.parseColor(weiuiParse.parseStr(var)));
    }

    /**
     * 设置导航栏选择字体颜色
     * @param var
     */
    @JSMethod
    public void setTabTextSelectColor(Object var) {
        mTabLayoutTop.setTextSelectColor(weiuiParse.parseColor(weiuiParse.parseStr(var)));
        mTabLayoutSlidingTop.setTextSelectColor(weiuiParse.parseColor(weiuiParse.parseStr(var)));
        mTabLayoutBottom.setTextSelectColor(weiuiParse.parseColor(weiuiParse.parseStr(var)));
    }

    /**
     * 设置导航栏图标可见
     * @param var
     */
    @JSMethod
    public void setTabIconVisible(Object var) {
        mTabLayoutTop.setIconVisible(weiuiParse.parseBool(var, true));
        mTabLayoutBottom.setIconVisible(weiuiParse.parseBool(var, true));
    }

    /**
     * 设置导航栏图标宽度
     * @param var
     */
    @JSMethod
    public void setTabIconWidth(Object var) {
        mTabLayoutTop.setIconWidth(weiuiScreenUtils.weexPx2dp(getInstance(), weiuiParse.parseInt(var, 0)));
        mTabLayoutBottom.setIconWidth(weiuiScreenUtils.weexPx2dp(getInstance(), weiuiParse.parseInt(var, 0)));
    }

    /**
     * 设置导航栏图标高度
     * @param var
     */
    @JSMethod
    public void setTabIconHeight(Object var) {
        mTabLayoutTop.setIconHeight(weiuiScreenUtils.weexPx2dp(getInstance(), weiuiParse.parseInt(var, 0)));
        mTabLayoutBottom.setIconHeight(weiuiScreenUtils.weexPx2dp(getInstance(), weiuiParse.parseInt(var, 0)));
    }

    /**
     * 指示器刷新
     */
    @JSMethod
    public void setFirstDraw(boolean isFirstDraw) {
        mTabLayoutTop.setFirstDraw(true);
        mTabLayoutBottom.setFirstDraw(true);
    }

    /**
     * 切换动画
     */
    @JSMethod
    public void setTabPageAnimated(boolean animated) {
        if (mViewPager == null) {
            return;
        }
        this.mViewPager.setSmoothScroll(animated);
    }
}
