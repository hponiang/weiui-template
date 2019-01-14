package com.luck.picture.lib.weiui.ui.module;

import com.luck.picture.lib.weiui.ui.weiui_picture;
import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.common.WXModule;

public class WeexModule extends WXModule {

    private weiui_picture __obj;

    private weiui_picture myApp() {
        if (__obj == null) {
            __obj = new weiui_picture();
        }
        return __obj;
    }

    /***************************************************************************************************/
    /***************************************************************************************************/
    /***************************************************************************************************/

    /**
     * 打开相册
     * @param object
     * @param callback
     */
    @JSMethod
    public void create(String object, JSCallback callback) {
        myApp().create(mWXSDKInstance.getContext(), object, callback);
    }

    /**
     * 压缩图片
     * @param object
     * @param callback
     */
    @JSMethod
    public void compressImage(String object, JSCallback callback) {
        myApp().compressImage(mWXSDKInstance.getContext(), object, callback);
    }

    /**
     * 预览图片
     * @param position
     * @param array
     */
    @JSMethod
    public void picturePreview(int position, String array, JSCallback callback) {
        myApp().picturePreview(mWXSDKInstance.getContext(), position, array, callback);
    }

    /**
     * 预览视频
     * @param path
     */
    @JSMethod
    public void videoPreview(String path) {
        myApp().videoPreview(mWXSDKInstance.getContext(), path);
    }

    /**
     * 缓存清除，包括裁剪和压缩后的缓存，要在上传成功后调用，注意：需要系统sd卡权限
     */
    @JSMethod
    public void deleteCache() {
        myApp().deleteCache(mWXSDKInstance.getContext());
    }
}
