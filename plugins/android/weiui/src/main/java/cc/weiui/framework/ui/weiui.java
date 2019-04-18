package cc.weiui.framework.ui;

import android.app.Activity;
import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.taobao.weex.InitConfig.Builder;
import com.taobao.weex.WXSDKEngine;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.common.WXException;

import cc.weiui.framework.BuildConfig;
import cc.weiui.framework.activity.PageActivity;
import cc.weiui.framework.extend.adapter.DrawableLoader;
import cc.weiui.framework.extend.adapter.ImageAdapter;
import cc.weiui.framework.extend.annotation.ModuleEntry;
import cc.weiui.framework.extend.bean.PageBean;
import cc.weiui.framework.extend.integration.glide.Glide;
import cc.weiui.framework.extend.integration.glide.load.engine.DiskCacheStrategy;
import cc.weiui.framework.extend.integration.glide.request.RequestOptions;
import cc.weiui.framework.extend.integration.glide.request.target.SimpleTarget;
import cc.weiui.framework.extend.integration.glide.request.transition.Transition;
import cc.weiui.framework.extend.integration.iconify.Iconify;
import cc.weiui.framework.extend.integration.iconify.fonts.IoniconsModule;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cc.weiui.framework.extend.integration.swipebacklayout.BGAKeyboardUtil;
import cc.weiui.framework.extend.integration.swipebacklayout.BGASwipeBackHelper;
import cc.weiui.framework.extend.integration.xutils.x;
import cc.weiui.framework.extend.module.rxtools.rxtoolsModule;
import cc.weiui.framework.extend.module.rxtools.tool.RxEncryptTool;
import cc.weiui.framework.extend.module.utilcode.util.DeviceUtils;
import cc.weiui.framework.extend.module.utilcode.util.FileUtils;
import cc.weiui.framework.extend.module.utilcode.utilcodeModule;
import cc.weiui.framework.extend.module.weiuiAdDialog;
import cc.weiui.framework.extend.module.weiuiAjax;
import cc.weiui.framework.extend.module.weiuiAlertDialog;
import cc.weiui.framework.extend.module.weiuiCommon;
import cc.weiui.framework.extend.module.weiuiIhttp;
import cc.weiui.framework.extend.module.weiuiJson;
import cc.weiui.framework.extend.module.weiuiOpenApp;
import cc.weiui.framework.extend.module.weiuiPage;
import cc.weiui.framework.extend.module.weiuiParse;
import cc.weiui.framework.extend.module.weiuiScreenUtils;
import cc.weiui.framework.extend.module.weiuiShareUtils;
import cc.weiui.framework.extend.view.loading.LoadingDialog;
import cc.weiui.framework.extend.view.webviewBridge.JsCallback;
import cc.weiui.framework.ui.component.a.A;
import cc.weiui.framework.ui.component.banner.Banner;
import cc.weiui.framework.ui.component.button.Button;
import cc.weiui.framework.ui.component.grid.Grid;
import cc.weiui.framework.ui.component.icon.Icon;
import cc.weiui.framework.ui.component.marquee.Marquee;
import cc.weiui.framework.ui.component.navbar.Navbar;
import cc.weiui.framework.ui.component.navbar.NavbarItem;
import cc.weiui.framework.ui.component.recyler.Recyler;
import cc.weiui.framework.ui.component.ripple.Ripple;
import cc.weiui.framework.ui.component.scrollText.ScrollText;
import cc.weiui.framework.ui.component.sidePanel.SidePanel;
import cc.weiui.framework.ui.component.sidePanel.SidePanelMenu;
import cc.weiui.framework.ui.component.tabbar.Tabbar;
import cc.weiui.framework.ui.component.tabbar.TabbarPage;
import cc.weiui.framework.ui.component.webView.WebView;
import cc.weiui.framework.ui.module.WeexEventModule;
import cc.weiui.framework.ui.module.WeexModule;
import cc.weiui.framework.ui.module.WeexNavigationBarModule;
import cc.weiui.framework.ui.module.WeexNavigatorModule;
import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;

/**
 * Created by WDM on 2018/3/27.
 */

public class weiui {

    private static final String TAG = "weiui";

    private static Application application;

    private static LinkedList<Activity> mActivityList = new LinkedList<>();

    public static Application getApplication() {
        return application;
    }

    public static LinkedList<Activity> getActivityList() {
        return mActivityList;
    }

    public static void init(Application application) {
        register(application);
    }

    public static void reboot() {
        LinkedList<Activity> activityList = weiui.getActivityList();
        for (int i = 0; i < activityList.size() - 1; i++) {
            activityList.get(i).finish();
        }
        Activity lastActivity = activityList.getLast();
        Intent intent = lastActivity.getPackageManager().getLaunchIntentForPackage(lastActivity.getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            lastActivity.startActivity(intent);
            lastActivity.finish();
        }
    }

    private static void setTopActivity(final Activity activity) {
        if (mActivityList.contains(activity)) {
            if (!mActivityList.getLast().equals(activity)) {
                mActivityList.remove(activity);
                mActivityList.addLast(activity);
            }
        } else {
            mActivityList.addLast(activity);
        }
    }

    private static void register(Application app) {
        weiui.application = app;
        weiui.application.registerActivityLifecycleCallbacks(mCallbacks);

        weiuiIhttp.init(application);

        Iconify.with(new IoniconsModule());

        BGASwipeBackHelper.init(application, null);

        Builder mBuilder = new Builder();
        mBuilder.setImgAdapter(new ImageAdapter());
        mBuilder.setDrawableLoader(new DrawableLoader(app));
        WXSDKEngine.initialize(application, mBuilder.build());

        try {
            WXSDKEngine.registerModule("weiui", WeexModule.class);
            WXSDKEngine.registerModule("navigator", WeexNavigatorModule.class);
            WXSDKEngine.registerModule("navigationBar", WeexNavigationBarModule.class);
            //
            WXSDKEngine.registerModule("event", WeexEventModule.class);
            //
            WXSDKEngine.registerComponent("a", A.class);
            WXSDKEngine.registerComponent("banner", Banner.class);
            WXSDKEngine.registerComponent("button", Button.class);
            WXSDKEngine.registerComponent("grid", Grid.class);
            WXSDKEngine.registerComponent("icon", Icon.class);
            WXSDKEngine.registerComponent("marquee", Marquee.class);
            WXSDKEngine.registerComponent("navbar", Navbar.class);
            WXSDKEngine.registerComponent("navbar-item", NavbarItem.class);
            WXSDKEngine.registerComponent("ripple", Ripple.class);
            WXSDKEngine.registerComponent("scroll-text", ScrollText.class);
            WXSDKEngine.registerComponent("scroll-view", Recyler.class);
            WXSDKEngine.registerComponent("side-panel", SidePanel.class);
            WXSDKEngine.registerComponent("side-panel-menu", SidePanelMenu.class);
            WXSDKEngine.registerComponent("tabbar", Tabbar.class);
            WXSDKEngine.registerComponent("tabbar-page", TabbarPage.class);
            WXSDKEngine.registerComponent("web-view", WebView.class);
        } catch (WXException e) {
            e.printStackTrace();
        }

        try {
            weiuiPluginManager.init(application);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Application.ActivityLifecycleCallbacks mCallbacks = new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {
            setTopActivity(activity);
        }

        @Override
        public void onActivityStarted(Activity activity) {
            setTopActivity(activity);
        }

        @Override
        public void onActivityResumed(Activity activity) {
            setTopActivity(activity);
        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            mActivityList.remove(activity);
        }
    };

    /***************************************************************************************************/
    /***************************************************************************************************/
    /***************************************************************************************************/

    @SuppressWarnings("unchecked")
    static class weiuiPluginManager {
        static Field getField(String name, Class c) {
            try {
                return c.getDeclaredField(name);
            } catch (Exception e) {
                return null;
            }
        }

        static ClassLoader getClassLoader() {
            return Thread.currentThread().getContextClassLoader();
        }

        static Class<?> getClassByAddressName(String classAddressName) {
            Class mClass = null;
            try {
                mClass = Class.forName(classAddressName);
            } catch (Exception ignored) {
            }
            return mClass;
        }

        static <T> T getObjectFromField(Field field, Object arg) {
            try {
                field.setAccessible(true);
                return (T) field.get(arg);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        static ArrayList<DexFile> getMultiDex() {
            BaseDexClassLoader dexLoader = (BaseDexClassLoader) getClassLoader();
            Field f = getField("pathList", getClassByAddressName("dalvik.system.BaseDexClassLoader"));
            Object pathList = getObjectFromField(f, dexLoader);
            Field f2 = getField("dexElements", getClassByAddressName("dalvik.system.DexPathList"));
            Object[] list = getObjectFromField(f2, pathList);
            Field f3 = getField("dexFile", getClassByAddressName("dalvik.system.DexPathList$Element"));

            ArrayList<DexFile> res = new ArrayList<>();

            assert list != null;
            for (Object aList : list) {
                DexFile d = getObjectFromField(f3, aList);
                res.add(d);
            }
            return res;
        }

        static boolean canLoad(String pack) {
            List<String> ignore = new ArrayList<>();
            ignore.add("com.taobao.weex.");
            ignore.add("com.alibaba.fastjson.");
            ignore.add("com.alipay.security.");
            ignore.add("com.bumptech.glide.");
            ignore.add("com.luck.picture.");
            ignore.add("com.weiui.framework.extend.");
            ignore.add("$");
            if (contains(ignore, pack)) {
                return false;
            }
            List<String> need = new ArrayList<>();
            need.add(".entry.");
            return contains(need, pack);
        }

        public static boolean contains(List l, String s) {
            for (Object q : l) {
                if (s.contains(q + ""))
                    return true;
            }
            return false;
        }

        static void registerDex(DexFile dex, Context context) {
            if (dex == null) {
                return;
            }
            Enumeration<String> entries = dex.entries();
            PathClassLoader classLoader = (PathClassLoader) Thread.currentThread().getContextClassLoader();
            while (entries.hasMoreElements()) {
                String entryName = entries.nextElement();
                if (canLoad(entryName)) {
                    try {
                        Class entryClass = Class.forName(entryName, true, classLoader);
                        ModuleEntry wxentry = (ModuleEntry) entryClass.getAnnotation(ModuleEntry.class);
                        if (wxentry != null) {
                            Object instance = entryClass.newInstance();
                            Method entry = entryClass.getMethod("init", Context.class);
                            entry.invoke(instance, context);
                            Log.d(TAG, "执行模块初始化:" + entryClass);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } catch (NoClassDefFoundError e) {
                        e.printStackTrace();
                    } catch (ExceptionInInitializerError ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        static void init(Context context) {
            ArrayList<DexFile> list = getMultiDex();
            for (DexFile dex : list) {
                registerDex(dex, context);
            }
        }
    }

    /***************************************************************************************************/
    /***************************************************************************************************/
    /***************************************************************************************************/

    public static Object[] objectGroup(Object... var) {
        return var;
    }

    public static JSCallback MCallback(JsCallback callback) {
        if (callback == null) {
            return null;
        }
        return new JSCallback() {
            @Override
            public void invoke(Object data) {
                try {
                    callback.apply(data);
                } catch (JsCallback.JsCallbackException je) {
                    je.printStackTrace();
                }
            }

            @Override
            public void invokeAndKeepAlive(Object data) {
                try {
                    callback.setPermanent(true);
                    callback.apply(data);
                } catch (JsCallback.JsCallbackException je) {
                    je.printStackTrace();
                }
            }
        };
    }

    public static void HCallback(JsCallback callback, Object... data) {
        if (callback == null) {
            return;
        }
        try {
            callback.apply(data);
        } catch (JsCallback.JsCallbackException je) {
            je.printStackTrace();
        }
    }

    /****************************************************************************************/
    /****************************************************************************************/

    /**
     * 打开页面 或 打开网页（内置浏览器）
     * @param object
     * @param callback
     */
    public void openPage(Context context, String object, JSCallback callback) {
        JSONObject json = weiuiJson.parseObject(object);
        if (json.size() == 0) {
            json.put("url", object);
        }
        if (json.getString("url") == null || json.getString("url").isEmpty()) {
            return;
        }
        PageBean mBean = new PageBean();

        //网址
        mBean.setUrl(weiuiPage.rewriteUrl(context, json.getString("url")));
        //名称（默认：随机生成）
        if (json.getString("pageName") != null) {
            mBean.setPageName(json.getString("pageName"));
        }
        //标题
        if (json.getString("pageTitle") != null) {
            mBean.setPageTitle(json.getString("pageTitle"));
        }
        //类型（默认：weex）
        if (json.getString("pageType") != null) {
            mBean.setPageType(json.getString("pageType"));
        }
        //缓存（默认：0）
        if (json.getString("cache") != null) {
            mBean.setCache(json.getIntValue("cache"));
        }
        //转递数据（默认：无）
        if (json.get("params") != null) {
            mBean.setParams(json.get("params"));
        }
        //是否显示等待（默认：true）
        if (json.getBoolean("loading") != null) {
            mBean.setLoading(json.getBoolean("loading"));
        }
        //是否支持滑动返回（默认：false）
        if (json.getBoolean("swipeBack") != null) {
            mBean.setSwipeBack(json.getBoolean("swipeBack"));
        }
        //是否进入页面需要动画效果（默认：true）
        if (json.getBoolean("animated") != null) {
            mBean.setAnimated(json.getBoolean("animated"));
        }
        //状态栏样式（可选，等于fullscreen|immersion时statusBarType、statusBarAlpha无效）
        if (json.getString("statusBarType") != null) {
            mBean.setStatusBarType(json.getString("statusBarType"));
        }
        //状态栏颜色值（默认：#3EB4FF）
        if (json.getString("statusBarColor") != null) {
            mBean.setStatusBarColor(json.getString("statusBarColor"));
        }
        //状态栏透明度（默认：0）
        if (json.getInteger("statusBarAlpha") != null) {
            mBean.setStatusBarAlpha(json.getInteger("statusBarAlpha"));
        }
        //状态栏样式
        if (json.getString("statusBarStyle") != null) {
            mBean.setStatusBarStyle(json.getBooleanValue("statusBarStyle"));
        }
        //透明底色窗口（默认：false）
        if (json.getBoolean("translucent") != null) {
            mBean.setTranslucent(json.getBoolean("translucent"));
        }
        //页面背景颜色（默认：#ffffff）
        if (json.getString("backgroundColor") != null) {
            mBean.setBackgroundColor(json.getString("backgroundColor"));
        }
        //返回键关闭（默认：true）
        if (json.getBoolean("backPressedClose") != null) {
            mBean.setBackPressedClose(json.getBoolean("backPressedClose"));
        }

        //JS回调事件
        if (callback != null) {
            mBean.setCallback(callback);
        }

        weiuiPage.openWin(context, mBean);
    }

    /**
     * 获取页面信息
     * @param object
     * @return
     */
    public Object getPageInfo(Context context, String object) {
        String pageName = weiuiPage.getPageName(object);
        if (pageName.isEmpty()) {
            if (context instanceof PageActivity) {
                return ((PageActivity) context).getPageInfo().toMap();
            }
            return null;
        }
        return weiuiPage.getWinInfo(pageName).toMap();
    }

    /**
     * 获取页面传递的参数
     * @param object
     * @return
     */
    public Object getPageParams(Context context, String object) {
        String pageName = weiuiPage.getPageName(object);
        if (pageName.isEmpty()) {
            if (context instanceof PageActivity) {
                return ((PageActivity) context).getPageInfo().getParams();
            }
            return null;
        }
        return weiuiPage.getWinInfo(pageName).getParams();
    }

    /**
     * 重新加载页面（刷新）
     * @param object
     */
    public void reloadPage(Context context, String object) {
        String pageName = weiuiPage.getPageName(object);
        if (pageName.isEmpty()) {
            if (context instanceof PageActivity) {
                ((PageActivity) context).reload();
            }
            return;
        }
        weiuiPage.reloadWin(pageName);
    }

    /**
     * 关闭页面 或 关闭网页（内置浏览器）
     * @param object
     */
    public void closePage(Context context, String object) {
        String pageName = weiuiPage.getPageName(object);
        if (context instanceof PageActivity) {
            boolean animated = weiuiJson.getBoolean(weiuiJson.parseObject(object), "animated", true);
            ((PageActivity) context).getPageInfo().setAnimatedClose(animated);
        }
        if (pageName.isEmpty()) {
            BGAKeyboardUtil.closeKeyboard((Activity) context);
            weiuiPage.closeActivity((Activity) context);
            return;
        }
        weiuiPage.closeWin(pageName);
    }


    /**
     * 关闭页面至指定页面
     * @param object
     */
    public void closePageTo(Context context, String object) {
        String pageName = weiuiPage.getPageName(object);
        if (pageName.isEmpty()) {
            return;
        }
        boolean isClose = false;
        Activity lastActivity = null;
        LinkedList<Activity> array = weiui.getActivityList();
        for (int i  = 0; i < array.size(); i++) {
            if (isClose) {
                if (i + 1 == array.size()) {
                    lastActivity = array.get(i);
                }else{
                    weiuiPage.closeActivity(array.get(i));
                }
            }else {
                if (array.get(i) instanceof PageActivity) {
                    String mPageName = ((PageActivity) array.get(i)).getPageInfo().getPageName();
                    if (pageName.equals(mPageName)) {
                        isClose = true;
                    }
                }
            }
        }
        weiuiPage.closeActivity(lastActivity);
    }

    /**
     * 设置键盘弹出方式
     * @param object
     * @param mode
     */
    public void setSoftInputMode(Context context, String object, String mode) {
        String pageName = weiuiPage.getPageName(object);
        if (pageName.isEmpty()) {
            if (context instanceof PageActivity) {
                pageName = ((PageActivity) context).getPageInfo().getPageName();
            }
        }
        PageBean mPageBean = weiuiPage.getWinInfo(pageName);
        if (mPageBean == null) {
            return;
        }
        PageActivity mPageActivity = ((PageActivity) mPageBean.getContext());
        mPageActivity.setSoftInputMode(mode);
    }

    /**
     * 修改状态栏样式
     * @param context
     * @param isLight 是否亮色
     */
    public void setStatusBarStyle(Context context, boolean isLight) {
        if (context instanceof PageActivity) {
            ((PageActivity) context).setStatusBarStyle(isLight);
        } else {
            this.toast(context, "当前页面不支持状态栏字体变色");
        }
    }

    /**
     * 修改状态栏样式
     * @param context
     * @param isLight 是否亮色
     */
    public void statusBarStyle(Context context, boolean isLight) {
        if (context instanceof PageActivity) {
            ((PageActivity) context).setStatusBarStyle(isLight);
        } else {
            this.toast(context, "当前页面不支持状态栏字体变色");
        }
    }

    /**
     * 拦截返回按键事件
     * @param object
     * @param callback  为null时取消拦截
     */
    public void setPageBackPressed(Context context, String object, JSCallback callback) {
        String pageName = weiuiPage.getPageName(object);
        if (pageName.isEmpty()) {
            if (context instanceof PageActivity) {
                pageName = ((PageActivity) context).getPageInfo().getPageName();
            }
        }
        PageBean mPageBean = weiuiPage.getWinInfo(pageName);
        if (mPageBean == null) {
            return;
        }
        PageActivity mPageActivity = ((PageActivity) mPageBean.getContext());
        if (callback == null) {
            mPageActivity.setOnBackPressed(null);
        }else{
            mPageActivity.setOnBackPressed(() -> {
                callback.invokeAndKeepAlive(null);
                return true;
            });
        }
    }

    /**
     * 监听下拉刷新事件
     * @param object
     * @param callback  为null时取消监听
     */
    public void setOnRefreshListener(Context context, String object, JSCallback callback) {
        String pageName = weiuiPage.getPageName(object);
        if (pageName.isEmpty()) {
            if (context instanceof PageActivity) {
                pageName = ((PageActivity) context).getPageInfo().getPageName();
            }
        }
        PageBean mPageBean = weiuiPage.getWinInfo(pageName);
        if (mPageBean == null) {
            return;
        }
        PageActivity mPageActivity = ((PageActivity) mPageBean.getContext());
        if (callback == null) {
            mPageActivity.setOnRefreshListener(null);
        }else{
            mPageActivity.setOnRefreshListener(callback::invokeAndKeepAlive);
        }
    }

    /**
     * 设置下拉刷新状态
     * @param object
     * @param refreshing
     */
    public void setRefreshing(Context context, String object, boolean refreshing) {
        String pageName = weiuiPage.getPageName(object);
        if (pageName.isEmpty()) {
            if (context instanceof PageActivity) {
                pageName = ((PageActivity) context).getPageInfo().getPageName();
            }
        }
        PageBean mPageBean = weiuiPage.getWinInfo(pageName);
        if (mPageBean == null) {
            return;
        }
        PageActivity mPageActivity = ((PageActivity) mPageBean.getContext());
        mPageActivity.setRefreshing(refreshing);
    }

    /**
     * 监听页面状态变化
     * @param object
     * @param callback
     */
    public void setPageStatusListener(Context context, String object, JSCallback callback) {
        if (object == null) {
            return;
        }
        JSONObject json = weiuiJson.parseObject(object);
        String listenerName = weiuiJson.getString(json, "listenerName", object);
        if (listenerName.isEmpty()) {
            return;
        }
        String pageName = weiuiJson.getString(json, "pageName");
        if (pageName.isEmpty()) {
            if (context instanceof PageActivity) {
                pageName = ((PageActivity) context).getPageInfo().getPageName();
            }
        }
        PageBean mPageBean = weiuiPage.getWinInfo(pageName);
        if (mPageBean == null) {
            return;
        }
        PageActivity mPageActivity = ((PageActivity) mPageBean.getContext());
        mPageActivity.setPageStatusListener(listenerName, callback);
    }

    /**
     * 取消监听页面状态变化
     * @param object
     */
    public void clearPageStatusListener(Context context, String object) {
        if (object == null) {
            return;
        }
        JSONObject json = weiuiJson.parseObject(object);
        String listenerName = weiuiJson.getString(json, "listenerName", object);
        if (listenerName.isEmpty()) {
            return;
        }
        String pageName = weiuiJson.getString(json, "pageName");
        if (pageName.isEmpty()) {
            if (context instanceof PageActivity) {
                pageName = ((PageActivity) context).getPageInfo().getPageName();
            }
        }
        PageBean mPageBean = weiuiPage.getWinInfo(pageName);
        if (mPageBean == null) {
            return;
        }
        PageActivity mPageActivity = ((PageActivity) mPageBean.getContext());
        mPageActivity.clearPageStatusListener(listenerName);
    }

    /**
     * 手动执行(触发)页面状态
     * @param object
     * @param status
     */
    public void onPageStatusListener(Context context, String object, String status) {
        if (status == null) {
            status = object;
            object = null;
        }
        if (status == null) {
            return;
        }
        JSONObject json = weiuiJson.parseObject(object);
        String pageName = weiuiJson.getString(json, "pageName");
        if (pageName.isEmpty()) {
            if (context instanceof PageActivity) {
                pageName = ((PageActivity) context).getPageInfo().getPageName();
            }
        }
        PageBean mPageBean = weiuiPage.getWinInfo(pageName);
        if (mPageBean == null) {
            return;
        }
        PageActivity mPageActivity = ((PageActivity) mPageBean.getContext());
        mPageActivity.onPageStatusListener(weiuiJson.getString(json, "listenerName", object), status, json.get("extra"));
    }

    /**
     * 获取页面缓存大小
     */
    public void getCacheSizePage(Context context, JSCallback callback) {
        new weiuiIhttp.getCacheSize("page", callback).start();
    }

    /**
     * 清除缓存页面
     */
    public void clearCachePage(Context context) {
        new weiuiIhttp.clearCache("page").start();
    }

    /**
     * 打开网页（系统浏览器）
     * @param url
     */
    public void openWeb(Context context, String url) {
        if (url == null) {
            return;
        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        context.startActivity(intent);
    }

    /**
     * 返回桌面
     */
    public void goDesktop(Context context) {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        home.addCategory(Intent.CATEGORY_HOME);
        context.startActivity(home);
    }

    /****************************************************************************************/
    /****************************************************************************************/

    /**
     * 获取状态栏高度（屏幕像素）
     */
    public int getStatusBarHeight(Context context) {
        Object var = weiuiCommon.getVariate("__weiuiModule::getStatusBarHeight");
        if (var == null) {
            var = weiuiCommon.getStatusBarHeight(context);
            weiuiCommon.setVariate("__weiuiModule::getStatusBarHeight", var);
        }
        return weiuiParse.parseInt(var);
    }

    /**
     * 获取状态栏高度（weexPX单位）
     */
    public int getStatusBarHeightPx(Context context) {
        Object var = weiuiCommon.getVariate("__weiuiModule::getStatusBarHeightPx");
        if (var == null) {
            var = weiuiScreenUtils.weexDp2px(null, weiuiCommon.getStatusBarHeight(context));
            weiuiCommon.setVariate("__weiuiModule::getStatusBarHeightPx", var);
        }
        return weiuiParse.parseInt(var);
    }

    /**
     * 获取虚拟键盘高度（屏幕像素）
     */
    public int getNavigationBarHeight(Context context) {
        return weiuiCommon.getNavigationBarHeight(context);
    }

    /**
     * 获取虚拟键盘高度（weexPX单位）
     */
    public int getNavigationBarHeightPx(Context context) {
        return weiuiScreenUtils.weexDp2px(null, weiuiCommon.getNavigationBarHeight(context));
    }

    /**
     * 获取weiui版本号
     */
    public int getVersion(Context context) {
        Object var = weiuiCommon.getVariate("__weiuiModule::getVersion");
        if (var == null) {
            var = BuildConfig.VERSION_CODE;
            weiuiCommon.setVariate("__weiuiModule::getVersion", var);
        }
        return weiuiParse.parseInt(var);
    }

    /**
     * 获取weiui版本号名称
     */
    public String getVersionName(Context context) {
        Object var = weiuiCommon.getVariate("__weiuiModule::getVersionName");
        if (var == null) {
            var = BuildConfig.VERSION_NAME;
            weiuiCommon.setVariate("__weiuiModule::getVersionName", var);
        }
        return weiuiParse.parseStr(var);
    }

    /**
     * 获取本地软件版本号
     */
    public int getLocalVersion(Context context) {
        Object var = weiuiCommon.getVariate("__weiuiModule::getLocalVersion");
        if (var == null) {
            var = weiuiCommon.getLocalVersion(context);
            weiuiCommon.setVariate("__weiuiModule::getLocalVersion", var);
        }
        return weiuiParse.parseInt(var);
    }

    /**
     * 获取本地软件版本号名称
     */
    public String getLocalVersionName(Context context) {
        Object var = weiuiCommon.getVariate("__weiuiModule::getLocalVersionName");
        if (var == null) {
            var = weiuiCommon.getLocalVersionName(context);
            weiuiCommon.setVariate("__weiuiModule::getLocalVersionName", var);
        }
        return weiuiParse.parseStr(var);
    }

    /**
     * 比较版本号的大小,前者大则返回一个正数,后者大返回一个负数,相等则返回0
     * @param version1
     * @param version2
     * @return
     */
    public int compareVersion(Context context, String version1, String version2) {
        try {
            return weiuiCommon.compareVersion(version1, version2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取手机的IMEI
     */
    public String getImei(Context context) {
        Object var = weiuiCommon.getVariate("__weiuiModule::getImei");
        if (var == null) {
            var = weiuiCommon.getImei(context);
            if (!TextUtils.isEmpty(weiuiParse.parseStr(var))) {
                weiuiCommon.setVariate("__weiuiModule::getImei", var);
            }
        }
        return weiuiParse.parseStr(var);
    }

    /**
     * 获取手机的IFA
     */
    public String getIfa(Context context) {
        return this.getImei(context);
    }

    /**
     * 获取设备系统版本号
     */
    public int getSDKVersionCode(Context context) {
        Object var = weiuiCommon.getVariate("__weiuiModule::getSDKVersionCode");
        if (var == null) {
            var = DeviceUtils.getSDKVersionCode();
            weiuiCommon.setVariate("__weiuiModule::getSDKVersionCode", var);
        }
        return weiuiParse.parseInt(var);
    }

    /**
     * 获取设备系统版本名称
     */
    public String getSDKVersionName(Context context) {
        Object var = weiuiCommon.getVariate("__weiuiModule::getSDKVersionName");
        if (var == null) {
            var = DeviceUtils.getSDKVersionName();
            weiuiCommon.setVariate("__weiuiModule::getSDKVersionName", var);
        }
        return weiuiParse.parseStr(var);
    }

    /**
     * 是否IPhoneX系列设配
     * @return
     */
    public boolean isIPhoneXType(Context context) {
        return false;
    }

    /****************************************************************************************/
    /****************************************************************************************/

    /**
     * 保存缓存信息
     * @param key
     * @param value
     * @param expired
     */
    public void setCachesString(Context context, String key, String value, Long expired) {
        if (key == null || value == null) {
            return;
        }
        weiuiCommon.setCachesString(context, "weiuiCaches", key, value, weiuiParse.parseLong(expired));
    }

    /**
     * 获取缓存信息
     * @param key
     * @param defaultVal
     */
    public String getCachesString(Context context, String key, String defaultVal) {
        if (key == null) {
            return defaultVal;
        }
        return weiuiCommon.getCachesString(context, "weiuiCaches", key, defaultVal);
    }

    /**
     * 设置全局变量
     * @param key
     * @param value
     */
    public void setVariate(Context context, String key, String value) {
        if (key == null || value == null) {
            return;
        }
        weiuiCommon.setVariate(key, value);
    }

    /**
     * 获取全局变量
     * @param key
     * @param defaultVal
     */
    public String getVariate(Context context, String key, String defaultVal) {
        if (key == null) {
            return defaultVal;
        }
        return weiuiCommon.getVariateStr(key, defaultVal);
    }

    /****************************************************************************************/
    /****************************************************************************************/

    /**
     * 获取内部缓存目录大小
     * @param callback
     */
    public void getCacheSizeDir(Context context, JSCallback callback) {
        if (callback != null) {
            new Thread(() -> {
                Map<String, Object> data = new HashMap<>();
                data.put("size", FileUtils.getDirLength(context.getCacheDir()));
                callback.invoke(data);
            }).start();
        }
    }

    /**
     * 清空内部缓存目录
     */
    public void clearCacheDir(Context context, JSCallback callback) {
        new Thread(() -> {
            JSONObject json = weiuiCommon.deleteAllInDir(context.getCacheDir());
            if (callback != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("success", json.getIntValue("success"));
                data.put("error", json.getIntValue("error"));
                callback.invoke(data);
            }
        }).start();
    }

    /**
     * 获取内部文件目录大小
     * @param callback
     */
    public void getCacheSizeFiles(Context context, JSCallback callback) {
        if (callback != null) {
            new Thread(() -> {
                Map<String, Object> data = new HashMap<>();
                data.put("size", FileUtils.getDirLength(context.getFilesDir()));
                callback.invoke(data);
            }).start();
        }
    }

    /**
     * 清空内部文件目录
     */
    public void clearCacheFiles(Context context, JSCallback callback) {
        new Thread(() -> {
            JSONObject json = weiuiCommon.deleteAllInDir(context.getFilesDir());
            if (callback != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("success", json.getIntValue("success"));
                data.put("error", json.getIntValue("error"));
                callback.invoke(data);
            }
        }).start();
    }

    /**
     * 获取内部数据库目录大小
     * @param callback
     */
    public void getCacheSizeDbs(Context context, JSCallback callback) {
        if (callback != null) {
            new Thread(() -> {
                Map<String, Object> data = new HashMap<>();
                data.put("size", FileUtils.getDirLength(new File(context.getFilesDir().getParent(), "databases")));
                callback.invoke(data);
            }).start();
        }
    }

    /**
     * 清空内部数据库目录
     */
    public void clearCacheDbs(Context context, JSCallback callback) {
        new Thread(() -> {
            JSONObject json = weiuiCommon.deleteAllInDir(new File(context.getFilesDir().getParent(), "databases"));
            if (callback != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("success", json.getIntValue("success"));
                data.put("error", json.getIntValue("error"));
                callback.invoke(data);
            }
        }).start();
    }

    /****************************************************************************************/
    /****************************************************************************************/

    /**
     * weex px转dp
     * @param var
     */
    public int weexPx2dp(Context context, String var) {
        return weiuiScreenUtils.weexPx2dp(null, var);
    }

    /**
     * weex dp转px
     * @param var
     */
    public int weexDp2px(Context context, String var) {
        return weiuiScreenUtils.weexDp2px(null, var);
    }

    /****************************************************************************************/
    /****************************************************************************************/

    /**
     * alert 警告框
     */
    public void alert(Context context, String object, JSCallback callback) {
        weiuiAlertDialog.alert(context, object, callback);
    }

    /**
     * confirm 确认对话框
     */
    public void confirm(Context context, String object, JSCallback callback) {
        weiuiAlertDialog.confirm(context, object, callback);
    }

    /**
     * input 输入对话框
     */
    public void input(Context context, String object, JSCallback callback) {
        weiuiAlertDialog.input(context, object, callback);
    }

    /****************************************************************************************/
    /****************************************************************************************/

    /**
     * 显示等待图标
     * @param object        参数
     * @param callback      返回键或点击空白处取消回调事件
     * @return
     */
    public String loading(Context context, String object, JSCallback callback) {
        return LoadingDialog.init(context, object, callback);
    }

    /**
     * 关闭等待图标
     */
    public void loadingClose(Context context, String var) {
        LoadingDialog.close(var);
    }

    /****************************************************************************************/
    /****************************************************************************************/

    /**
     * 打开滑动验证码
     * @param imgUrl
     * @param callback
     */
    public void swipeCaptcha(Context context, String imgUrl, JSCallback callback) {
        PageActivity.startSwipeCaptcha(context, imgUrl, callback);
    }

    /****************************************************************************************/
    /****************************************************************************************/

    /**
     * 打开二维码扫描
     * @param object
     * @param callback
     */
    public void openScaner(Context context, String object, JSCallback callback) {
        PageActivity.startScanerCode(context, object, callback);
    }

    /****************************************************************************************/
    /****************************************************************************************/

    /**
     * 跨域异步请求
     * @param object
     * @param callback
     */
    public void ajax(Context context, String object, JSCallback callback) {
        JSONObject json = weiuiJson.parseObject(object);
        if (json.size() == 0) {
            json.put("url", object);
        }
        weiuiAjax.ajax(context, json, callback);
    }

    /**
     * 取消跨域异步请求
     * @param name
     */
    public void ajaxCancel(Context context, String name) {
        weiuiAjax.ajaxCancel(name);
    }

    /**
     * 获取异步请求缓存大小
     */
    public void getCacheSizeAjax(Context context, JSCallback callback) {
        new weiuiIhttp.getCacheSize("ajax", callback).start();
    }

    /**
     * 清除异步请求缓存
     */
    public void clearCacheAjax(Context context) {
        new weiuiIhttp.clearCache("ajax").start();
    }

    /**
     * 获取图片尺寸
     * @param context
     * @param url
     * @param callback
     */
    public void getImageSize(Context context, String url, JSCallback callback) {
        Glide.with(context)
                .asBitmap()
                .load(url)
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("status", "success");
                        data.put("width", resource.getWidth());
                        data.put("height", resource.getHeight());
                        callback.invoke(data);
                    }
                });
    }

    /****************************************************************************************/
    /****************************************************************************************/

    /**
     * 复制文本到剪贴板
     * @param var
     */
    public void copyText(Context context, String var) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("text", var));
        }
    }

    /**
     * 获取剪贴板的文本
     * @return
     */
    public CharSequence pasteText(Context context) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = clipboard.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0) {
                return clip.getItemAt(0).coerceToText(context);
            }
        }
        return null;
    }

    /****************************************************************************************/
    /****************************************************************************************/

    /**
     * 吐司(Toast)显示
     * @param object
     */
    public void toast(Context context, String object) {
        utilcodeModule.Toast(null, object);
    }

    /**
     * 吐司(Toast)隐藏
     */
    public void toastClose(Context context) {
        utilcodeModule.ToastClose();
    }

    /****************************************************************************************/
    /****************************************************************************************/

    /**
     * 图片广告弹窗
     * @param object
     * @param callback
     */
    public void adDialog(Context context, String object, JSCallback callback) {
        JSONObject json = weiuiJson.parseObject(object);
        if (json.size() == 0) {
            json.put("imgUrl", object);
        }
        weiuiAdDialog.create(null, context, json, callback);
    }

    /**
     * 手动关闭图片广告弹窗
     * @param dialogName
     */
    public void adDialogClose(Context context, String dialogName) {
        weiuiAdDialog.close(dialogName);
    }

    /****************************************************************************************/
    /****************************************************************************************/

    /**
     * 保存图片到本地
     * @param url
     */
    public void saveImage(Context context, String url, JSCallback callback) {
        weiuiCommon.saveImage(context, url, callback);
    }

    /****************************************************************************************/
    /****************************************************************************************/

    /**
     * 打开其他APP
     * @param type
     */
    public void openOtherApp(Context context, String type) {
        if (type == null) {
            return;
        }
        switch (type.toLowerCase()) {
            case "wx":
                weiuiOpenApp.openWeChat(context);
                break;

            case "qq":
                weiuiOpenApp.openQQ(context);
                break;

            case "alipay":
                weiuiOpenApp.openAlipay(context);
                break;

            case "jd":
                weiuiOpenApp.openJd(context);
                break;
        }
    }

    /****************************************************************************************/
    /****************************************************************************************/

    /**
     * 分享文字
     * @param text
     */
    public void shareText(Context context, String text) {
        weiuiShareUtils.shareText(context, text);
    }

    /**
     * 分享图片
     * @param imgUrl
     */
    public void shareImage(Context context, String imgUrl) {
        weiuiShareUtils.shareImage(context, imgUrl);
    }

    /****************************************************************************************/
    /****************************************************************************************/

    /**
     * 隐藏键盘
     * @return
     */
    public void keyboardHide(Context context) {
        utilcodeModule.KeyboardUtils((Activity) context, "hideSoftInput");
    }

    /**
     * 键盘相关
     * @return
     */
    public Boolean keyboardStatus(Context context) {
        return (Boolean) utilcodeModule.KeyboardUtils((Activity) context, "isSoftInputVisible");
    }

    /***************************************************************************************************/
    /***************************************************************************************************/
    /***************************************************************************************************/

    /**
     * App 相关
     * @param method
     * @return
     */
    public Object appUtils(Context context, String method, Object var0, Object var1) {
        return utilcodeModule.AppUtils(method, weiui.objectGroup(var0, var1));
    }

    /**
     * 设备相关
     * @param method
     * @return
     */
    public Object deviceUtils(Context context, String method) {
        return utilcodeModule.DeviceUtils(method);
    }

    /**
     * 键盘相关
     * @param method
     * @return
     */
    public Object keyboardUtils(Context context, String method) {
        return utilcodeModule.KeyboardUtils((Activity) context, method);
    }

    /**
     * 网络相关
     * @param method
     * @return
     */
    public Object networkUtils(Context context, String method, Object var0, Object var1) {
        return utilcodeModule.NetworkUtils(method, weiui.objectGroup(var0, var1));
    }

    /**
     * 权限相关
     * @param method
     * @return
     */
    public Object permissionUtils(Context context, String method, Object var0, Object var1) {
        return utilcodeModule.PermissionUtils(method, weiui.objectGroup(var0, var1));
    }

    /**
     * 手机相关
     * @param method
     * @return
     */
    public Object phoneUtils(Context context, String method, Object var0, Object var1, Object var2) {
        return utilcodeModule.PhoneUtils(method, weiui.objectGroup(var0, var1, var2));
    }

    /**
     * 进程相关
     * @param method
     * @return
     */
    public Object processUtils(Context context, String method, Object var0, Object var1) {
        return utilcodeModule.ProcessUtils(method, weiui.objectGroup(var0, var1));
    }

    /**
     * 屏幕相关
     * @param method
     * @return
     */
    public Object screenUtils(Context context, String method, Object var0, Object var1) {
        return utilcodeModule.ScreenUtils((Activity) context, method, weiui.objectGroup(var0, var1));
    }

    /**
     * 时间相关
     * @param method
     * @return
     */
    public Object timeUtils(Context context, String method, Object var0, Object var1, Object var2) {
        return utilcodeModule.TimeUtils(method, weiui.objectGroup(var0, var1, var2));
    }

    /**
     * 摄像机相关
     * @param method
     */
    public void cameraTool(Context context, String method) {
        rxtoolsModule.RxCameraTool(method);
    }

    /**
     * 定位相关
     * @param method
     * @return
     */
    public Object locationTool(Context context, String method, Object var0, Object var1, Object var2) {
        return rxtoolsModule.RxLocationTool(context, method, weiui.objectGroup(var0, var1, var2));
    }

    /**
     * 震动相关
     * @param method
     */
    public void vibrateTool(Context context, String method, Object var0, Object var1) {
        rxtoolsModule.RxVibrateTool(context, method, weiui.objectGroup(var0, var1));
    }
}
