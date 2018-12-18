package cc.weiui.framework.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.AndroidRuntimeException;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import cc.weiui.framework.BuildConfig;
import cc.weiui.framework.R;
import cc.weiui.framework.extend.integration.swipebacklayout.BGAKeyboardUtil;
import cc.weiui.framework.extend.module.rxtools.module.scaner.CameraManager;
import cc.weiui.framework.extend.module.rxtools.module.scaner.CaptureActivityHandler;
import cc.weiui.framework.extend.module.rxtools.module.scaner.decoding.InactivityTimer;
import cc.weiui.framework.extend.module.rxtools.tool.RxAnimationTool;
import cc.weiui.framework.extend.module.rxtools.tool.RxBeepTool;
import cc.weiui.framework.extend.module.rxtools.tool.RxPhotoTool;
import cc.weiui.framework.extend.module.rxtools.tool.RxQrBarTool;
import cc.weiui.framework.extend.module.utilcode.constant.PermissionConstants;
import cc.weiui.framework.extend.module.weiuiAlertDialog;
import cc.weiui.framework.extend.module.weiuiConstants;
import cc.weiui.framework.extend.module.weiuiJson;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import cc.weiui.framework.extend.integration.glide.Glide;
import cc.weiui.framework.extend.integration.glide.request.target.SimpleTarget;
import cc.weiui.framework.extend.integration.glide.request.transition.Transition;

import cc.weiui.framework.extend.integration.zxing.Result;

import com.rabtman.wsmanager.WsManager;
import com.rabtman.wsmanager.listener.WsStatusListener;
import com.skyline.widget.dialog.ActionDialog;
import com.taobao.weex.IWXRenderListener;
import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.bridge.WXBridgeManager;
import com.taobao.weex.common.OnWXScrollListener;
import com.taobao.weex.common.WXRenderStrategy;
import com.taobao.weex.dom.WXEvent;
import com.taobao.weex.ui.component.WXComponent;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import cc.weiui.framework.extend.integration.statusbarutil.StatusBarUtil;
import cc.weiui.framework.extend.integration.swipebacklayout.BGASwipeBackHelper;
import cc.weiui.framework.extend.bean.PageBean;
import cc.weiui.framework.extend.module.utilcode.util.PermissionUtils;
import cc.weiui.framework.extend.module.utilcode.util.ScreenUtils;
import cc.weiui.framework.extend.module.utilcode.util.SizeUtils;
import cc.weiui.framework.extend.module.weiuiCommon;
import cc.weiui.framework.extend.module.weiuiIhttp;
import cc.weiui.framework.extend.module.weiuiMap;
import cc.weiui.framework.extend.module.weiuiPage;
import cc.weiui.framework.extend.module.weiuiParse;
import cc.weiui.framework.extend.view.FloatDragView;
import cc.weiui.framework.extend.view.ProgressWebView;
import cc.weiui.framework.extend.view.SwipeCaptchaView;
import cc.weiui.framework.ui.weiui;

import static android.widget.Toast.LENGTH_SHORT;

public class PageActivity extends AppCompatActivity {

    private static final String TAG = "PageActivity";

    private Handler mHandler = new Handler();

    private PageBean mPageInfo;
    private String lifecycleLastStatus;

    private OnBackPressed mOnBackPressed;
    public interface OnBackPressed { boolean onBackPressed(); }

    private OnRefreshListener mOnRefreshListener;
    public interface OnRefreshListener { void refresh(String pageName); }

    private Map<String, JSCallback> mOnPageStatusListeners = new HashMap<>();

    //模板部分
    private ViewGroup mBody, mWeb, mAuto, mError;
    private TextView mErrorCode;
    private ViewGroup mErrorCbox;
    private ViewGroup mWeexView;
    private FrameLayout mWeexProgress;
    private ImageView mWeexProgressBg;
    private SwipeRefreshLayout mWeexSwipeRefresh;
    private ProgressWebView mWebView;
    private WXSDKInstance mWXSDKInstance;
    private BGASwipeBackHelper mSwipeBackHelper;

    //申请权限部分
    private PermissionUtils mPermissionInstance;

    //滑动验证码部分
    private SwipeCaptchaView v_swipeCaptchaView;
    private SeekBar v_swipeDragBar;
    private int v_swipeNum;

    //二维码与条形码部分
    private RelativeLayout scan_containter, scan_main;
    private InactivityTimer scan_inactivityTimer;
    private CaptureActivityHandler scan_handler;
    private boolean scan_hasSurface;
    private int scan_cropWidth = 0;
    private int scan_cropHeight = 0;
    private boolean scan_flashing = true;
    private boolean scan_vibrate = true;

    /****************************************************************************************************/
    /****************************************************************************************************/
    /****************************************************************************************************/

    /**
     * 申请权限专用
     * @param context
     */
    public static void startPermission(final Context context) {
        PageBean mBean = new PageBean();
        mBean.setPageType("permission");
        mBean.setTranslucent(true);
        weiuiPage.openWin(context, mBean);
    }

    /**
     * 滑动验证码专用
     * @param context
     * @param img
     * @param callback
     */
    public static void startSwipeCaptcha(Context context, String img, JSCallback callback) {
        PageBean mBean = new PageBean();
        mBean.setUrl(img);
        mBean.setPageType("swipeCaptcha");
        mBean.setTranslucent(true);
        mBean.setCallback(callback);
        weiuiPage.openWin(context, mBean);
    }

    /**
     * 扫描二维码与条形码专用
     * @param context
     * @param obj
     * @param callback
     */
    public static void startScanerCode(Context context, String obj, JSCallback callback) {
        JSONObject json = weiuiJson.parseObject(obj);
        if (json.size() == 0 && obj != null && obj.equals("null")) {
            json.put("desc", String.valueOf(obj));
        }
        json.put("successClose", weiuiJson.getBoolean(json, "successClose", true));
        //
        PermissionUtils.permission(PermissionConstants.CAMERA)
                .rationale(shouldRequest -> PermissionUtils.showRationaleDialog(context, shouldRequest))
                .callback(new PermissionUtils.FullCallback() {
                    @Override
                    public void onGranted(List<String> permissionsGranted) {
                        PageBean mBean = new PageBean();
                        mBean.setUrl(weiuiJson.getString(json, "desc", "将二维码图片对准扫描框即可自动扫描"));
                        mBean.setPageType("scanerCode");
                        mBean.setTranslucent(true);
                        mBean.setCallback(callback);
                        mBean.setOtherObject(json);
                        weiuiPage.openWin(context, mBean);
                    }

                    @Override
                    public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied) {
                        if (!permissionsDeniedForever.isEmpty()) {
                            PermissionUtils.showOpenAppSettingDialog(context);
                        }
                    }
                }).request();
    }

    /**
     * 透明页面专用专用
     * @param context
     * @param callback
     */
    public static void startTransparentPage(Context context, JSCallback callback) {
        PageBean mBean = new PageBean();
        mBean.setPageType("transparentPage");
        mBean.setTranslucent(true);
        mBean.setCallback(callback);
        weiuiPage.openWin(context, mBean);
    }

    /****************************************************************************************************/
    /****************************************************************************************************/
    /****************************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        mPageInfo = weiuiPage.getPageBean(intent.getStringExtra("name"));

        if (mPageInfo == null) {
            mPageInfo = new PageBean();
        } else {
            setPageStatusListener("__" + mPageInfo.getPageName(), mPageInfo.getCallback());
        }

        switch (mPageInfo.getPageType()) {
            case "permission":
                mPermissionInstance = PermissionUtils.getInstance();
                if (mPermissionInstance.getThemeCallback() != null) {
                    mPermissionInstance.getThemeCallback().onActivityCreate(this);
                }
                break;

            case "swipeCaptcha":
                break;

            case "scanerCode":
                initSwipeBackFinish();
                break;

            case "transparentPage":
                break;

            default:
                initSwipeBackFinish();
                break;
        }

        super.onCreate(savedInstanceState);
        try{ getWindow().requestFeature(Window.FEATURE_NO_TITLE); }catch (AndroidRuntimeException ignored) { }
        if (getSupportActionBar() != null){ getSupportActionBar().hide(); }

        if (mPageInfo.getPageName() != null) {
            mPageInfo.setContext(this);
            weiuiPage.setPageBean(mPageInfo.getPageName(), mPageInfo);
        }

        switch (mPageInfo.getPageType()) {
            case "permission":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (mPermissionInstance.rationale(this)) {
                        finish();
                        return;
                    }
                    if (mPermissionInstance.getPermissionsRequest() != null) {
                        int size = mPermissionInstance.getPermissionsRequest().size();
                        requestPermissions(mPermissionInstance.getPermissionsRequest().toArray(new String[size]), 1);
                    }
                }
                setImmersionStatusBar();
                break;

            case "swipeCaptcha":
                setContentView(R.layout.activity_page_swipe_captcha);
                initSwipeCaptchaPageView();
                break;

            case "scanerCode":
                setContentView(R.layout.activity_page_scaner_code);
                setImmersionStatusBar();
                initScanerCodePageView();
                break;

            case "transparentPage":
                setContentView(R.layout.activity_page_transparent);
                setImmersionStatusBar();
                break;

            default:
                setContentView(R.layout.activity_page);
                if (mPageInfo.getUrl() == null || mPageInfo.getUrl().isEmpty()) {
                    finish();
                    return;
                }
                initDefaultPage();
                break;
        }
        invokeAndKeepAlive("create", null);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mWXSDKInstance != null) {
            mWXSDKInstance.onActivityStart();
        }
        invokeAndKeepAlive("start", null);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mWXSDKInstance != null) {
            mWXSDKInstance.onActivityResume();
        }
        if (scan_containter != null) {
            SurfaceView surfaceView = findViewById(R.id.scan_preview);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            if (scan_hasSurface) {
                //Camera初始化
                initScanerCodeCamera(surfaceHolder);
            } else {
                surfaceHolder.addCallback(new SurfaceHolder.Callback() {
                    @Override
                    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                    }

                    @Override
                    public void surfaceCreated(SurfaceHolder holder) {
                        if (!scan_hasSurface) {
                            scan_hasSurface = true;
                            initScanerCodeCamera(holder);
                        }
                    }

                    @Override
                    public void surfaceDestroyed(SurfaceHolder holder) {
                        scan_hasSurface = false;
                    }
                });
                surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            }
        }
        invokeAndKeepAlive("resume", null);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mWXSDKInstance != null) {
            mWXSDKInstance.onActivityPause();
        }
        if (scan_containter != null) {
            if (scan_handler != null) {
                scan_handler.quitSynchronously();
                scan_handler = null;
            }
            CameraManager.get().closeDriver();
        }
        invokeAndKeepAlive("pause", null);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mWXSDKInstance != null) {
            mWXSDKInstance.onActivityStop();
        }
        invokeAndKeepAlive("stop", null);
    }

    @Override
    public void onRestart() {
        super.onRestart();
        invokeAndKeepAlive("restart", null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (mPermissionInstance != null) {
            mPermissionInstance.onRequestPermissionsResult(this);
            finish();
        }else{
            if (mWXSDKInstance != null) {
                mWXSDKInstance.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mWXSDKInstance != null) {
            mWXSDKInstance.onActivityResult(requestCode, resultCode, data);
        }
        if (scan_containter != null && resultCode == Activity.RESULT_OK) {
            ContentResolver resolver = getContentResolver();
            Uri originalUri = data.getData();
            try {
                Bitmap photo = MediaStore.Images.Media.getBitmap(resolver, originalUri);
                Result result = RxQrBarTool.decodeFromPhoto(photo);
                if (result != null) {
                    RxBeepTool.playBeep(this, scan_vibrate);
                    Map<String, Object> retData = new HashMap<>();
                    retData.put("source", "photo");
                    retData.put("result", result);
                    retData.put("format", result.getBarcodeFormat());
                    retData.put("text", result.getText());
                    invokeAndKeepAlive("success", retData);
                } else {
                    Map<String, Object> retData = new HashMap<>();
                    retData.put("source", "photo");
                    invokeAndKeepAlive("error", retData);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //
        Map<String, Object> retData = new HashMap<>();
        retData.put("requestCode", requestCode);
        retData.put("resultCode", resultCode);
        retData.put("resultData", data);
        invokeAndKeepAlive("activityResult", retData);
        //
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        if (scan_containter != null) {
            scan_inactivityTimer.shutdown();
        }
        if (mWXSDKInstance != null) {
            mWXSDKInstance.onActivityDestroy();
        }
        if (mWebView != null) {
            mWebView.onDestroy();
        }
        if (mPageInfo != null) {
            weiuiIhttp.cancel(mPageInfo.getPageName());
            weiuiPage.removePageBean(mPageInfo.getPageName());
        }
        invoke("destroy", null);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // 正在滑动返回的时候取消返回按钮事件
        if (mSwipeBackHelper != null) {
            if (mSwipeBackHelper.isSliding()) {
                return;
            }
        }
        if (mWebView != null) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
                return;
            }
        }
        if (!mPageInfo.isBackPressedClose()) {
            return;
        }
        if (mOnBackPressed != null) {
            if (mOnBackPressed.onBackPressed()) {
                return;
            }
        }
        BGAKeyboardUtil.closeKeyboard(this);
        super.onBackPressed();
    }

    /****************************************************************************************************/
    /****************************************************************************************************/
    /****************************************************************************************************/

    /**
     * 初始化滑动验证视图
     */
    private void initSwipeCaptchaPageView() {
        v_swipeCaptchaView = findViewById(R.id.v_swipeCaptchaView);
        v_swipeDragBar = findViewById(R.id.v_swipeDragBar);
        //
        int bodyWidth = (int) (ScreenUtils.getScreenWidth() * 0.8f);
        weiuiCommon.setViewWidthHeight(findViewById(R.id.v_swipeBody), bodyWidth, -1);
        findViewById(R.id.v_swipeClose).setOnClickListener(view -> finish());
        //
        v_swipeCaptchaView.setOnCaptchaMatchCallback(new SwipeCaptchaView.OnCaptchaMatchCallback() {
            @Override
            public void matchSuccess(SwipeCaptchaView mSwipeCaptchaView) {
                invokeAndKeepAlive("success", null);
                //
                v_swipeDragBar.setEnabled(false);
                mHandler.postDelayed(()-> finish(), 300);
            }

            @Override
            public void matchFailed(SwipeCaptchaView mSwipeCaptchaView) {
                invokeAndKeepAlive("failed", null);
                //
                if (v_swipeNum > 1) {
                    v_swipeNum = 0;
                    mSwipeCaptchaView.createCaptcha();
                }else{
                    v_swipeNum++;
                    mSwipeCaptchaView.resetCaptcha();
                }
                v_swipeDragBar.setProgress(0);
            }
        });
        v_swipeDragBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                v_swipeCaptchaView.setCurrentSwipeValue(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                v_swipeDragBar.setMax(v_swipeCaptchaView.getMaxSwipeValue());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                v_swipeCaptchaView.matchCaptcha();
            }
        });
        //
        Glide.with(this)
                .asBitmap()
                .load(mPageInfo.getUrl() != null && !mPageInfo.getUrl().isEmpty() ? mPageInfo.getUrl() : R.drawable.swipecaptcha_bg)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        ViewGroup.LayoutParams params = v_swipeCaptchaView.getLayoutParams();
                        params.width = bodyWidth - SizeUtils.dp2px(28);
                        params.height = (int) weiuiCommon.scaleHeight(params.width, resource.getWidth(), resource.getHeight());
                        v_swipeCaptchaView.setLayoutParams(params);
                        //
                        v_swipeCaptchaView.setImageBitmap(resource);
                        v_swipeCaptchaView.createCaptcha();
                    }
                });
    }

    /**
     * 初始化二维码与条形码视图
     */
    private void initScanerCodePageView() {
        scan_containter = findViewById(R.id.scan_containter);
        scan_main = findViewById(R.id.scan_main);
        //
        ImageView mQrLineView = findViewById(R.id.capture_scan_line);
        RxAnimationTool.ScaleUpDowm(mQrLineView);
        //
        CameraManager.init(this);
        scan_hasSurface = false;
        scan_inactivityTimer = new InactivityTimer(this);
        //
        if (mPageInfo.getUrl() != null) {
            ((TextView) findViewById(R.id.scan_desc)).setText(getPageInfo().getUrl());
        }
    }

    /**
     * 初始化默认页
     */
    private void initDefaultPage() {
        mBody = findViewById(R.id.v_body);
        mError = findViewById(R.id.v_error);
        mErrorCode = findViewById(R.id.v_error_code);
        mErrorCbox = findViewById(R.id.v_error_cbox);
        //
        findViewById(R.id.v_error_title).setOnClickListener(view -> mErrorCbox.setVisibility(View.VISIBLE));
        findViewById(R.id.v_refresh).setOnClickListener(view -> reload());
        findViewById(R.id.v_back).setOnClickListener(view -> finish());
        //
        mSwipeBackHelper.setSwipeBackEnable(mPageInfo.isSwipeBack());
        mBody.setBackgroundColor(Color.parseColor(mPageInfo.getBackgroundColor()));
        //
        switch (mPageInfo.getStatusBarType()) {
            case "fullscreen":
                //全屏
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                if (mPageInfo.getSoftInputMode().equals("auto")) {
                    mPageInfo.setSoftInputMode("pan");
                }
                break;
            case "immersion":
                //沉浸式
                setImmersionStatusBar();
                if (mPageInfo.getSoftInputMode().equals("auto")) {
                    mPageInfo.setSoftInputMode("pan");
                }
                break;
            default:
                //默认
                if (mPageInfo.isSwipeBack()) {
                    StatusBarUtil.setColorForSwipeBack(this, Color.parseColor(mPageInfo.getStatusBarColor()), mPageInfo.getStatusBarAlpha());
                }else{
                    StatusBarUtil.setColor(this, Color.parseColor(mPageInfo.getStatusBarColor()), mPageInfo.getStatusBarAlpha());
                }
                break;
        }
        //
        setSoftInputMode(mPageInfo.getSoftInputMode());
        initDefaultPageView();
    }

    /**
     * 初始化默认视图
     */
    private void initDefaultPageView() {
        switch (mPageInfo.getPageType()) {
            case "web":
                mWeb = findViewById(R.id.v_web);
                mWeb.setVisibility(View.VISIBLE);
                mWebView = findViewById(R.id.v_webview);
                mWebView.setProgressbarVisibility(mPageInfo.isLoading());
                //
                mWebView.setOnStatusClient(new ProgressWebView.StatusCall() {
                    @Override
                    public void onStatusChanged(WebView view, String status) {
                        Map<String, Object> retData = new HashMap<>();
                        retData.put("webStatus", status);
                        invokeAndKeepAlive("statusChanged", retData);
                    }

                    @Override
                    public void onErrorChanged(WebView view, int errorCode, String description, String failingUrl) {
                        mError.setVisibility(View.VISIBLE);
                        mErrorCode.setText(String.valueOf(errorCode));
                        Map<String, Object> retData = new HashMap<>();
                        retData.put("webStatus", "error");
                        retData.put("errCode", errorCode);
                        retData.put("errMsg", description);
                        retData.put("errUrl", failingUrl);
                        invokeAndKeepAlive("errorChanged", retData);
                    }

                    @Override
                    public void onTitleChanged(WebView view, String title) {
                        Map<String, Object> retData = new HashMap<>();
                        retData.put("webStatus", "title");
                        retData.put("title", title);
                        invokeAndKeepAlive("titleChanged", retData);
                    }
                });
                mWebView.loadUrl(mPageInfo.getUrl());
                break;

            case "weex":
                mWeexView = findViewById(R.id.v_weexview);
                mWeexProgress = findViewById(R.id.v_weexprogress);
                mWeexProgressBg = findViewById(R.id.v_weexprogressbg);

                mWeexSwipeRefresh = findViewById(R.id.v_weexswiperefresh);
                mWeexSwipeRefresh.setVisibility(View.VISIBLE);
                mWeexSwipeRefresh.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
                mWeexSwipeRefresh.setOnRefreshListener(() -> {
                    if (mOnRefreshListener != null) mOnRefreshListener.refresh(mPageInfo.getPageName());
                });
                mWeexSwipeRefresh.setEnabled(mOnRefreshListener != null);

                weexLoad();
                break;

            case "auto":
                if (mPageInfo.getUrl().endsWith(".bundle.wx")) {
                    mPageInfo.setPageType("weex");
                    initDefaultPageView();
                    break;
                }
                if (mPageInfo.getUrl().contains("?_wx_tpl=")) {
                    mPageInfo.setPageType("weex");
                    mPageInfo.setUrl(weiuiCommon.getMiddle(mPageInfo.getUrl(), "?_wx_tpl=", null));
                    initDefaultPageView();
                    break;
                }
                mAuto = findViewById(R.id.v_auto);
                mAuto.setVisibility(View.VISIBLE);
                weiuiIhttp.getContentType(mPageInfo.getUrl(), result -> {
                    if (result == null) {
                        finish();
                        return;
                    }
                    String res = result.toLowerCase();
                    mPageInfo.setPageType(res.contains("javascript") ? "weex" : "web");
                    initDefaultPageView();
                    mAuto.setVisibility(View.GONE);
                });
                break;

            default:
                finish();
        }
    }

    /****************************************************************************************************/
    /****************************************************************************************************/
    /****************************************************************************************************/

    /**
     * SwipeBack
     * 初始化滑动返回
     */
    private void initSwipeBackFinish() {
        mSwipeBackHelper = new BGASwipeBackHelper(this, swipeBackDelegate());
        // 设置滑动返回是否可用。默认值为 true
        mSwipeBackHelper.setSwipeBackEnable(true);
        // 设置是否仅仅跟踪左侧边缘的滑动返回。默认值为 true
        mSwipeBackHelper.setIsOnlyTrackingLeftEdge(true);
        // 设置是否是微信滑动返回样式。默认值为 true
        mSwipeBackHelper.setIsWeChatStyle(true);
        // 设置阴影资源 id。默认值为 R.drawable.bga_sbl_shadow
        mSwipeBackHelper.setShadowResId(R.drawable.bga_sbl_shadow);
        // 设置是否显示滑动返回的阴影效果。默认值为 true
        mSwipeBackHelper.setIsNeedShowShadow(true);
        // 设置阴影区域的透明度是否根据滑动的距离渐变。默认值为 true
        mSwipeBackHelper.setIsShadowAlphaGradient(true);
        // 设置触发释放后自动滑动返回的阈值，默认值为 0.3f
        mSwipeBackHelper.setSwipeBackThreshold(0.3f);
        // 设置底部导航条是否悬浮在内容上，默认值为 false
        mSwipeBackHelper.setIsNavigationBarOverlap(false);
    }

    /**
     * SwipeBack
     * @return
     */
    private BGASwipeBackHelper.Delegate swipeBackDelegate() {
        return new BGASwipeBackHelper.Delegate() {
            /**
             * SwipeBack
             * 是否支持滑动返回
             * @return
             */
            @Override
            public boolean isSupportSwipeBack() {
                return true;
            }

            /**
             * SwipeBack
             * 正在滑动返回
             * @param slideOffset 从 0 到 1
             */
            @Override
            public void onSwipeBackLayoutSlide(float slideOffset) {
            }

            /**
             * SwipeBack
             * 没达到滑动返回的阈值，取消滑动返回动作，回到默认状态
             */
            @Override
            public void onSwipeBackLayoutCancel() {
            }

            /**
             * SwipeBack
             * 滑动返回执行完毕，销毁当前 Activity
             */
            @Override
            public void onSwipeBackLayoutExecuted() {
                if (mSwipeBackHelper != null) {
                    mSwipeBackHelper.swipeBackward();
                }
            }
        };
    }

    /****************************************************************************************************/
    /****************************************************************************************************/
    /****************************************************************************************************/


    /**
     * Scaner
     * 初始化二维码与条形码相机
     */
    private void initScanerCodeCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
            Point point = CameraManager.get().getCameraResolution();
            AtomicInteger width = new AtomicInteger(point.y);
            AtomicInteger height = new AtomicInteger(point.x);
            int cropWidth = scan_main.getWidth() * width.get() / scan_containter.getWidth();
            int cropHeight = scan_main.getHeight() * height.get() / scan_containter.getHeight();
            setScanCropWidth(cropWidth);
            setScanCropHeight(cropHeight);
        } catch (IOException | RuntimeException ioe) {
            return;
        }
        if (scan_handler == null) {
            scan_handler = new CaptureActivityHandler(PageActivity.this);
        }
    }

    /**
     * Scaner
     * @param view
     */
    public void scanClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.scan_light) {
            scanLight();
        } else if (viewId == R.id.scan_back) {
            finish();
        } else if (viewId == R.id.scan_picture) {
            RxPhotoTool.openLocalImage(this);
        } else if (viewId == R.id.scan_image_qr) {
            weiuiCommon.setViewWidthHeight(scan_main, SizeUtils.dp2px(240), SizeUtils.dp2px(240));
            invokeAndKeepAlive("changeQr", null);
        } else if (viewId == R.id.scan_image_bar) {
            weiuiCommon.setViewWidthHeight(scan_main, SizeUtils.dp2px(300), SizeUtils.dp2px(120));
            invokeAndKeepAlive("changeBar", null);
        }
    }

    /**
     * Scaner
     */
    private void scanLight() {
        if (scan_flashing) {    // 开闪光灯
            scan_flashing = false;
            CameraManager.get().openLight();
            invokeAndKeepAlive("openLight", null);
        } else {            // 关闪光灯
            scan_flashing = true;
            CameraManager.get().offLight();
            invokeAndKeepAlive("offLight", null);
        }
    }

    /**
     * Scaner
     * @return
     */
    public int getScanCropWidth() {
        return scan_cropWidth;
    }

    /**
     * Scaner
     * @param cropWidth
     */
    public void setScanCropWidth(int cropWidth) {
        scan_cropWidth = cropWidth;
        CameraManager.FRAME_WIDTH = scan_cropWidth;

    }

    /**
     * Scaner
     * @return
     */
    public int getScanCropHeight() {
        return scan_cropHeight;
    }

    /**
     * Scaner
     * @param cropHeight
     */
    public void setScanCropHeight(int cropHeight) {
        this.scan_cropHeight = cropHeight;
        CameraManager.FRAME_HEIGHT = scan_cropHeight;
    }

    /**
     * Scaner
     * @param result
     */
    public void handleScanDecode(Result result) {
        scan_inactivityTimer.onActivity();
        RxBeepTool.playBeep(this, scan_vibrate);
        //
        Map<String, Object> retData = new HashMap<>();
        retData.put("source", "camera");
        retData.put("result", result);
        retData.put("format", result.getBarcodeFormat());
        retData.put("text", result.getText());
        invokeAndKeepAlive("success", retData);
    }

    /**
     * Scaner
     * @return
     */
    public Handler getScanHandler() {
        return scan_handler;
    }

    /****************************************************************************************************/
    /****************************************************************************************************/
    /****************************************************************************************************/

    /**
     * Weex
     */
    private void weexLoad() {
        if (mPageInfo.isLoading()) {
            mWeexProgress.setVisibility(View.INVISIBLE);
            mHandler.postDelayed(()-> mWeexProgress.post(()->{
                if (mWeexProgress.getVisibility() == View.INVISIBLE) {
                    mWeexProgress.setVisibility(View.VISIBLE);
                    if (!mPageInfo.isTranslucent()) {
                        mWeexProgressBg.setVisibility(View.VISIBLE);
                    }
                }
            }), 100);
        }
        //
        weexCreateInstance();
        mWXSDKInstance.onActivityCreate();
        weexRenderPage();
    }

    /**
     * Weex
     */
    private void weexCreateInstance() {
        if (mWXSDKInstance != null) {
            mWXSDKInstance.registerRenderListener(null);
            mWXSDKInstance.registerOnWXScrollListener(null);
            mWXSDKInstance.destroy();
            mWXSDKInstance = null;
        }
        mWXSDKInstance = new WXSDKInstance(this);
        mWXSDKInstance.registerRenderListener(weexIWXRenderListener());
        mWXSDKInstance.registerOnWXScrollListener(weexOnWXScrollListener());
    }

    /**
     * Weex
     */
    private void weexRenderPage() {
        weiuiPage.cachePage(mPageInfo.getUrl(), mPageInfo.getCache(), mPageInfo.getParams(), new weiuiPage.OnCachePageCallback() {
            @Override
            public void success(Map<String, Object> resParams, String resData) {
                mWXSDKInstance.render(mPageInfo.getPageName(), resData, resParams, null, WXRenderStrategy.APPEND_ASYNC);
            }

            @Override
            public void error(Map<String, Object> resParams) {
                mWXSDKInstance.renderByUrl(mPageInfo.getPageName(), mPageInfo.getUrl(), resParams, null, WXRenderStrategy.APPEND_ASYNC);
            }

            @Override
            public void complete(Map<String, Object> resParams) {

            }
        });
    }

    /**
     * Weex
     * @return
     */
    private IWXRenderListener weexIWXRenderListener() {
        return new IWXRenderListener() {
            @Override
            public void onViewCreated(WXSDKInstance instance, View view) {
                if (mWeexView != null) {
                    mWeexView.removeAllViews();
                    mWeexView.addView(view);
                }
                invokeAndKeepAlive("viewCreated", null);
            }

            @Override
            public void onRenderSuccess(WXSDKInstance instance, int width, int height) {
                if (mWeexProgress != null) {
                    mWeexProgress.setVisibility(View.GONE);
                }
                invokeAndKeepAlive("renderSuccess", null);
            }

            @Override
            public void onRefreshSuccess(WXSDKInstance instance, int width, int height) {
                if (mWeexProgress != null) {
                    mWeexProgress.setVisibility(View.GONE);
                }
            }

            /**
             * Weex
             * @param instance
             * @param errCode
             * @param errMsg
             */
            @Override
            public void onException(WXSDKInstance instance, String errCode, String errMsg) {
                if (mWeexProgress != null) {
                    mWeexProgress.setVisibility(View.GONE);
                }
                if (errCode == null) {
                    errCode = "";
                }
                //
                Map<String, Object> retData = new HashMap<>();
                retData.put("errCode", errCode);
                retData.put("errMsg", errMsg);
                retData.put("errUrl", instance.getBundleUrl());
                invokeAndKeepAlive("error", retData);
                //
                if (weiui.debug || errCode.equals("-1002") || errCode.equals("-1003")) {
                    mError.setVisibility(View.VISIBLE);
                    mErrorCode.setText(String.valueOf(errCode));
                }
            }
        };
    }

    /**
     * Weex
     * @return
     */
    private OnWXScrollListener weexOnWXScrollListener() {
        return new OnWXScrollListener() {
            @Override
            public void onScrolled(View view, int x, int y) {

            }

            @Override
            public void onScrollStateChanged(View view, int x, int y, int newState) {
                if (mOnRefreshListener != null) {
                    if (y == 0) {
                        mWeexSwipeRefresh.setEnabled(true);
                    }else{
                        mWeexSwipeRefresh.setEnabled(false);
                    }
                }
            }
        };
    }

    /****************************************************************************************************/
    /****************************************************************************************************/
    /****************************************************************************************************/

    private void setImmersionStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    private void invoke(String status, Map<String, Object> retData) {
        if (status.equals(lifecycleLastStatus)) {
            return;
        }
        lifecycleLastStatus = status;
        lifecycleListener(status);
        //
        if (mOnPageStatusListeners.size() > 0) {
            if (retData == null) {
                retData = new HashMap<>();
            }
            retData.put("pageName", mPageInfo.getPageName());
            retData.put("status", status);
            for (String name : mOnPageStatusListeners.keySet()) {
                JSCallback call = mOnPageStatusListeners.get(name);
                if (call != null) {
                    call.invoke(retData);
                }
            }
        }
    }

    private void invokeAndKeepAlive(String status, Map<String, Object> retData) {
        if (status.equals(lifecycleLastStatus)) {
            return;
        }
        lifecycleLastStatus = status;
        lifecycleListener(status);
        //
        if (mOnPageStatusListeners.size() > 0) {
            for (String name : mOnPageStatusListeners.keySet()) {
                JSCallback call = mOnPageStatusListeners.get(name);
                if (call != null) {
                    if (retData == null) retData = new HashMap<>();
                    retData.put("pageName", mPageInfo.getPageName());
                    retData.put("status", status);
                    call.invokeAndKeepAlive(retData);
                }
            }
        }
        if (status.equals("success") && weiuiJson.getBoolean(mPageInfo.getOtherObject(), "successClose")) {
            finish();
        }
        //
        switch (status) {
            case "create":
                deBugButtonCreate();
                break;

            case "resume":
                deBugButtonRefresh(0);
                break;

            case "error":
            case "viewCreated":
                mHandler.postDelayed(this::deBugSocketInit, 300);
                break;
        }
    }

    private void lifecycleListener(String status) {
        if (mWXSDKInstance != null) {
            switch (status) {
                case "viewCreated":
                    status = "ready";
                    break;

                case "resume":
                case "pause":
                    break;

                default:
                    return;
            }
            WXComponent mWXComponent = mWXSDKInstance.getRootComponent();
            if (mWXComponent != null) {
                WXEvent events = mWXComponent.getEvents();
                boolean hasEvent = events.contains(weiuiConstants.Event.LIFECYCLE);
                if (hasEvent) {
                    Map<String, Object> retData = new HashMap<>();
                    retData.put("status", status);
                    WXBridgeManager.getInstance().fireEventOnNode(mWXSDKInstance.getInstanceId(), mWXComponent.getRef(), weiuiConstants.Event.LIFECYCLE, retData, null);
                    if (status.equals("ready")) {
                        lifecycleListener("resume");
                    }
                }
            }
        }
    }

    /****************************************************************************************************/
    /****************************************************************************************************/
    /****************************************************************************************************/

    /**
     * 获取页面详情
     * @return
     */
    public PageBean getPageInfo() {
        return mPageInfo;
    }

    /**
     * 刷新页面
     */
    public void reload() {
        if (mError != null) {
            mError.setVisibility(View.GONE);
        }
        if (mErrorCbox != null) {
            mErrorCbox.setVisibility(View.GONE);
        }
        //
        switch (mPageInfo.getPageType()) {
            case "web":
                mWebView.loadUrl(mPageInfo.getUrl());
                break;

            case "weex":
                weexLoad();
                break;
        }
    }

    /**
     * 设置键盘弹出方式
     * @param mode
     */
    public void setSoftInputMode(String mode) {
        if (mPageInfo == null || mode == null) {
            return;
        }
        switch (mode) {
            case "resize":
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                break;
            case "pan":
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                break;
            case "auto":
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED);
                break;
        }
        mPageInfo.setSoftInputMode(mode);
    }

    /**
     * 设置是否允许滑动返回
     * @param var
     */
    public void setSwipeBackEnable(Boolean var) {
        if (mPageInfo == null || mSwipeBackHelper == null) {
            return;
        }
        mPageInfo.setSwipeBack(var);
        mSwipeBackHelper.setSwipeBackEnable(var);
    }

    /**
     * 跳过禁止返回键关闭直接关闭
     */
    public void onBackPressedSkipBackPressedClose() {
        mPageInfo.setBackPressedClose(true);
        onBackPressed();
    }

    /**
     * 拦截返回按键事件
     * @param mOnBackPressed
     */
    public void setOnBackPressed(OnBackPressed mOnBackPressed) {
        this.mOnBackPressed = mOnBackPressed;
    }

    /**
     * 监听下拉刷新事件
     * @param mOnRefreshListener
     */
    public void setOnRefreshListener(OnRefreshListener mOnRefreshListener){
        this.mOnRefreshListener = mOnRefreshListener;
        if (mWeexSwipeRefresh != null) {
            mWeexSwipeRefresh.setEnabled(mOnRefreshListener != null);
        }
    }

    /**
     * 设置下拉刷新状态
     * @param refreshing
     */
    public void setRefreshing(boolean refreshing){
        if (mWeexSwipeRefresh != null) {
            mWeexSwipeRefresh.setRefreshing(refreshing);
        }
    }

    /**
     * 监听页面状态
     * @param listenerName
     * @param mOnPageStatusListener
     */
    public void setPageStatusListener(String listenerName, JSCallback mOnPageStatusListener){
        if (listenerName == null) {
            listenerName = weiuiCommon.randomString(8);
        }
        if (mOnPageStatusListener != null) {
            this.mOnPageStatusListeners.put(listenerName, mOnPageStatusListener);
        }
    }

    /**
     * 取消监听页面状态
     * @param listenerName
     */
    public void clearPageStatusListener(String listenerName){
        if (listenerName == null) {
            return;
        }
        this.mOnPageStatusListeners.remove(listenerName);
    }

    /**
     * 手动执行(触发)页面状态
     * @param listenerName
     * @param status
     */
    public void onPageStatusListener(String listenerName, String status, Object extra) {
        Map<String, Object> retData = new HashMap<>();
        retData.put("extra", extra);
        if (listenerName == null || listenerName.isEmpty()) {
            invokeAndKeepAlive(status, retData);
            return;
        }
        JSCallback callback = this.mOnPageStatusListeners.get(listenerName);
        if (callback != null) {
            retData.put("pageName", mPageInfo.getPageName());
            retData.put("status", status);
            callback.invokeAndKeepAlive(retData);
        }
    }

    /****************************************************************************************************/
    /****************************************************************************************************/
    /****************************************************************************************************/

    private TextView deBugButton;
    private WsManager deBugSocketWsManager;
    private int deBugButtonSize = 128;

    /**
     * 创建debug按钮
     */
    @SuppressLint("SetTextI18n")
    private void deBugButtonCreate() {
        if (!BuildConfig.DEBUG) {
            return;
        }
        if (deBugButton != null) {
            return;
        }
        if (!mPageInfo.getPageType().equals("weex")) {
            return;
        }
        deBugButton = new TextView(this);
        deBugButton.setText("DEV");
        deBugButton.setTextColor(Color.WHITE);
        deBugButton.setTextSize(14);
        deBugButton.setGravity(Gravity.CENTER);
        if (weiuiCommon.getVariateInt("__deBugSocket:Status") == 1) {
            deBugButton.setBackgroundResource(R.drawable.debug_button_success);
        }else{
            deBugButton.setBackgroundResource(R.drawable.debug_button_connect);
        }
        deBugButton.setOnClickListener(deBugClickListener);
        FloatDragView.Builder mFloatDragView = new FloatDragView.Builder();
        mFloatDragView.setActivity(this)
                .setDefaultLeft(weiuiParse.parseInt(weiuiCommon.getVariate("__pageActivity::FloatDrag:Left"), ScreenUtils.getScreenWidth() - deBugButtonSize))
                .setDefaultTop(weiuiParse.parseInt(weiuiCommon.getVariate("__pageActivity::FloatDrag:Top"), (ScreenUtils.getScreenHeight() - deBugButtonSize) / 2))
                .setNeedNearEdge(true)
                .setSize(deBugButtonSize)
                .setView(deBugButton)
                .setUpdateListener((left, top) -> {
                    weiuiCommon.setVariate("__pageActivity::FloatDrag:Left", left);
                    weiuiCommon.setVariate("__pageActivity::FloatDrag:Top", top);
                })
                .build();
    }

    /**
     * 刷新debug按钮
     * @param status
     */
    private void deBugButtonRefresh(int status) {
        if (deBugButton == null) {
            return;
        }
        if (status == 1) {
            deBugButton.setBackgroundResource(R.drawable.debug_button_success);
            weiuiCommon.setVariate("__deBugSocket:Status", 1);
        }else if (status == 2) {
            deBugButton.setBackgroundResource(R.drawable.debug_button_connect);
            weiuiCommon.setVariate("__deBugSocket:Status", 2);
        }
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(deBugButtonSize, deBugButtonSize);
        int left = weiuiParse.parseInt(weiuiCommon.getVariate("__pageActivity::FloatDrag:Left"), ScreenUtils.getScreenWidth() - deBugButtonSize);
        int top = weiuiParse.parseInt(weiuiCommon.getVariate("__pageActivity::FloatDrag:Top"), (ScreenUtils.getScreenHeight() - deBugButtonSize) / 2);
        layoutParams.setMargins(left, top, 0, 0);
        deBugButton.setLayoutParams(layoutParams);
    }

    /**
     * 初始化deBugSocket
     */
    private void deBugSocketInit() {
        if (deBugButton == null) {
            return;
        }
        if (weiuiCommon.getVariateInt("__deBugSocket:Init") == 0) {
            weiuiCommon.setVariate("__deBugSocket:Init", 1);
            //
            JSONObject jsonData = weiuiJson.parseObject(weiuiCommon.getAssetsJson("weiui/config.json", this));
            weiuiCommon.setVariate("__deBugSocket:Host", weiuiJson.getString(jsonData, "socketHost"));
            weiuiCommon.setVariate("__deBugSocket:Port", weiuiJson.getString(jsonData, "socketPort"));
            deBugSocketConnect("initialize");
        }
    }

    /**
     * 连接deBugSocket
     */
    private void deBugSocketConnect(String mode) {
        if (deBugButton == null) {
            return;
        }
        if (deBugSocketWsManager != null) {
            deBugSocketWsManager.stopConnect();
            deBugSocketWsManager = null;
        }
        String host = weiuiCommon.getVariateStr("__deBugSocket:Host");
        String port = weiuiCommon.getVariateStr("__deBugSocket:Port");
        if (host.length() == 0 || port.length() == 0) {
            return;
        }
        deBugSocketWsManager = new WsManager.Builder(this)
                .client(new OkHttpClient().newBuilder().pingInterval(15, TimeUnit.SECONDS).retryOnConnectionFailure(true).build())
                .wsUrl("ws://" + host + ":" + port + "?mode=" + mode)
                .needReconnect(false)
                .build();
        deBugSocketWsManager.setWsStatusListener(deBugWsStatusListener);
        try {
            deBugSocketWsManager.startConnect();
        } catch (IllegalArgumentException ignored) { }
    }

    /**
     * debug按钮点击事件
     */
    private View.OnClickListener deBugClickListener = v -> {
        ActionDialog dialog = new ActionDialog(this);
        dialog.setTitle("开发工具菜单");
        dialog.addAction(weiuiCommon.getVariateInt("__deBugSocket:Status") == 1 ? "WiFi真机同步 [已连接]" : "WiFi真机同步");
        dialog.addAction("扫一扫");
        dialog.addAction("刷新");
        dialog.addAction("重启APP");
        if ("true".equals(weiuiCommon.getVariateStr("configDataIsDist"))) {
            dialog.addAction("清除热更新数据");
        }
        dialog.setEventListener(new ActionDialog.OnEventListener() {
            @Override
            public void onActionItemClick(ActionDialog dialog, ActionDialog.ActionItem item, int position) {
                switch (item.title) {
                    case "WiFi真机同步 [已连接]":
                    case "WiFi真机同步":
                        String host = weiuiCommon.getVariateStr("__deBugSocket:Host");
                        String port = weiuiCommon.getVariateStr("__deBugSocket:Port");
                        String inputObject = "{title:\"WiFi真机同步配置\",message:\"配置成功后，可实现真机同步实时预览\",buttons:[\"取消\",\"连接\"],inputs:[{type:'text',placeholder:'请输入IP地址',value:'" + host + "',autoFocus:true},{type:'number',placeholder:'请输入端口号',value:'" + port + "'}]}";
                        weiuiAlertDialog.input(PageActivity.this, inputObject, new JSCallback() {
                            @Override
                            public void invoke(Object data) {
                                Map<String, Object> retData = weiuiMap.objectToMap(data);
                                if (weiuiParse.parseStr(retData.get("status")).equals("click") && weiuiParse.parseStr(retData.get("title")).equals("连接")) {
                                    JSONArray dData = weiuiJson.parseArray(retData.get("data"));
                                    weiuiCommon.setVariate("__deBugSocket:Host", dData.getString(0));
                                    weiuiCommon.setVariate("__deBugSocket:Port", dData.getString(1));
                                    List<Activity> activityList = weiui.getActivityList();
                                    Activity activity = activityList.get(0);
                                    if (activity instanceof PageActivity) {
                                        ((PageActivity) activity).deBugSocketConnect("initialize");
                                    }
                                }
                            }

                            @Override
                            public void invokeAndKeepAlive(Object data) {
                                //
                            }
                        });
                        break;

                    case "扫一扫":
                        PageActivity.startScanerCode(PageActivity.this, "{}", new JSCallback() {
                            @Override
                            public void invoke(Object data) {
                                //
                            }

                            @Override
                            public void invokeAndKeepAlive(Object data) {
                                Map<String, Object> retData = weiuiMap.objectToMap(data);
                                if (weiuiParse.parseStr(retData.get("status")).equals("success")) {
                                    String text = weiuiParse.parseStr(retData.get("text"));
                                    if (text.startsWith("http")) {
                                        String url = text, host = "", port = "";
                                        if (text.contains("?socket=")) {
                                            url = weiuiCommon.getMiddle(text, null, "?socket=");
                                            host = weiuiCommon.getMiddle(text, "?socket=", ":");
                                            port = weiuiCommon.getMiddle(text, "?socket=" + host + ":", "&");
                                        }
                                        //
                                        PageBean mPageBean = new PageBean();
                                        mPageBean.setUrl(url);
                                        mPageBean.setPageType("weex");
                                        weiuiPage.openWin(PageActivity.this, mPageBean);
                                        //
                                        if (host.length() > 0 && port.length() > 0) {
                                            weiuiCommon.setVariate("__deBugSocket:Host", host);
                                            weiuiCommon.setVariate("__deBugSocket:Port", port);
                                            List<Activity> activityList = weiui.getActivityList();
                                            Activity activity = activityList.get(0);
                                            if (activity instanceof PageActivity) {
                                                ((PageActivity) activity).deBugSocketConnect("back");
                                            }
                                        }
                                    } else {
                                        Toast.makeText(PageActivity.this, "识别内容：" + text, LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
                        break;

                    case "刷新":
                        reload();
                        break;

                    case "重启APP":
                        JSONObject newJson = new JSONObject();
                        newJson.put("title", "热重启APP");
                        newJson.put("message", "确认要关闭所有页面热重启APP吗？");
                        weiuiAlertDialog.confirm(weiui.getActivityList().getLast(), newJson, new JSCallback() {
                            @Override
                            public void invoke(Object data) {
                                Map<String, Object> retData = weiuiMap.objectToMap(data);
                                if (weiuiParse.parseStr(retData.get("status")).equals("click") && weiuiParse.parseStr(retData.get("title")).equals("确定")) {
                                    weiui.reboot();
                                }
                            }

                            @Override
                            public void invokeAndKeepAlive(Object data) {

                            }
                        });
                        break;

                    case "清除热更新数据":
                        weiuiCommon.setVariate("configDataIsDist", "clear");
                        weiuiCommon.setVariate("configDataNoUpdate", "clear");
                        weiui.reboot();
                        break;
                }
            }

            @Override
            public void onCancelItemClick(ActionDialog dialog) {

            }
        });
        dialog.show();
    };

    /**
     * deBugSocket事件
     */
    private WsStatusListener deBugWsStatusListener = new WsStatusListener() {
        @Override
        public void onOpen(Response response) {
            super.onOpen(response);
            //
            List<Activity> activityList = weiui.getActivityList();
            for (int i = activityList.size() - 1; i >= 0; --i) {
                Activity activity = activityList.get(i);
                if (activity instanceof PageActivity) {
                    ((PageActivity) activity).deBugButtonRefresh(1);
                }
            }
        }

        @Override
        public void onMessage(String text) {
            super.onMessage(text);
            //
            if (text.startsWith("HOMEPAGE:")) {
                List<Activity> activityList = weiui.getActivityList();
                for (int i = activityList.size() - 1; i >= 0; --i) {
                    Activity activity = activityList.get(i);
                    if (i == 0) {
                        if (activity instanceof PageActivity) {
                            PageActivity mActivity = ((PageActivity) activity);
                            String homePage = weiuiCommon.getCachesString(PageActivity.this, "__deBugSocket", "homePage");
                            String mHomePage = text.substring(9);
                            if (!homePage.equals(mHomePage))  {
                                weiuiCommon.setCachesString(PageActivity.this, "__deBugSocket", "homePage", mHomePage, 2);
                                mActivity.mPageInfo.setUrl(mHomePage);
                                mActivity.reload();
                                BGAKeyboardUtil.closeKeyboard(PageActivity.this);
                            }
                        }
                    }else{
                        activity.finish();
                    }
                }
            }else if (text.startsWith("HOMEPAGEBACK:")) {
                List<Activity> activityList = weiui.getActivityList();
                Activity activity = activityList.get(0);
                if (activity instanceof PageActivity) {
                    PageActivity mActivity = ((PageActivity) activity);
                    String mHomePage = text.substring(13);
                    weiuiCommon.setCachesString(PageActivity.this, "__deBugSocket", "homePage", mHomePage, 2);
                    mActivity.mPageInfo.setUrl(mHomePage);
                    mActivity.reload();
                    BGAKeyboardUtil.closeKeyboard(PageActivity.this);
                }
            }else if (text.equals("RELOADPAGE")) {
                List<Activity> activityList = weiui.getActivityList();
                Activity activity = activityList.get(activityList.size() - 1);
                if (activity instanceof PageActivity) {
                    ((PageActivity) activity).reload();
                    BGAKeyboardUtil.closeKeyboard(PageActivity.this);
                }
            }
        }

        @Override
        public void onClosed(int code, String reason) {
            super.onClosed(code, reason);
            //
            List<Activity> activityList = weiui.getActivityList();
            for (int i = activityList.size() - 1; i >= 0; --i) {
                Activity activity = activityList.get(i);
                if (activity instanceof PageActivity) {
                    ((PageActivity) activity).deBugButtonRefresh(2);
                }
            }
        }

        @Override
        public void onFailure(Throwable t, Response response) {
            super.onFailure(t, response);
            //
            List<Activity> activityList = weiui.getActivityList();
            for (int i = activityList.size() - 1; i >= 0; --i) {
                Activity activity = activityList.get(i);
                if (activity instanceof PageActivity) {
                    ((PageActivity) activity).deBugButtonRefresh(2);
                }
            }
        }
    };
}
