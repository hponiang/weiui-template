package cc.weiui.framework.ui.module.webView;

import cc.weiui.framework.extend.delegate.ModuleDelegate;
import cc.weiui.framework.extend.view.ExtendWebView;
import cc.weiui.framework.extend.view.webviewBridge.JsCallback;


public class weiuiPictureBridge {

    public static Class init() {
        return weiuiPictureBridge.class;
    }

    /***************************************************************************************************/
    /***************************************************************************************************/
    /***************************************************************************************************/

    /**
     * 打开相册
     * @param object
     * @param callback
     */
    public static void create(ExtendWebView webView, String object, JsCallback callback) {
        ModuleDelegate.getInstance().getData("weiui_picture", "create", webView, object, callback);
    }

    /**
     * 压缩图片
     * @param object
     * @param callback
     */
    public static void compressImage(ExtendWebView webView, String object, JsCallback callback) {
        ModuleDelegate.getInstance().getData("weiui_picture", "compressImage", webView, object, callback);
    }

    /**
     * 预览图片
     * @param position
     * @param array
     */
    public static void picturePreview(ExtendWebView webView, int position, String array, JsCallback callback) {
        ModuleDelegate.getInstance().getData("weiui_picture", "picturePreview", webView, position, array, callback);
    }

    /**
     * 预览视频
     * @param path
     */
    public static void videoPreview(ExtendWebView webView, String path) {
        ModuleDelegate.getInstance().getData("weiui_picture", "videoPreview", webView, path);
    }

    /**
     * 缓存清除，包括裁剪和压缩后的缓存，要在上传成功后调用，注意：需要系统sd卡权限
     */
    public static void deleteCache(ExtendWebView webView) {
        ModuleDelegate.getInstance().getData("weiui_picture", "deleteCache", webView);
    }

}
