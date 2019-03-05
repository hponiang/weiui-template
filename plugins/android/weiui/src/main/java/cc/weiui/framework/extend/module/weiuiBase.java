package cc.weiui.framework.extend.module;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.taobao.weex.bridge.JSCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.weiui.framework.BuildConfig;
import cc.weiui.framework.R;
import cc.weiui.framework.extend.integration.glide.Glide;
import cc.weiui.framework.extend.integration.glide.load.engine.DiskCacheStrategy;
import cc.weiui.framework.extend.integration.glide.request.RequestOptions;
import cc.weiui.framework.extend.integration.glide.request.target.SimpleTarget;
import cc.weiui.framework.extend.integration.glide.request.transition.Transition;
import cc.weiui.framework.extend.integration.xutils.common.Callback;
import cc.weiui.framework.extend.integration.xutils.http.RequestParams;
import cc.weiui.framework.extend.integration.xutils.x;
import cc.weiui.framework.extend.module.rxtools.tool.RxEncryptTool;
import cc.weiui.framework.extend.module.utilcode.util.FileUtils;
import cc.weiui.framework.extend.module.utilcode.util.ScreenUtils;
import cc.weiui.framework.extend.module.utilcode.util.TimeUtils;
import cc.weiui.framework.extend.module.utilcode.util.ZipUtils;
import cc.weiui.framework.ui.weiui;

public class weiuiBase {

    public static String appName = "WEIUI";
    public static String appGroup = "WEIUI";

    /**
     * 配置类
     */
    public static class config {

        private static boolean configDataIsDist;
        private static JSONObject configData;

        /**
         * 读取配置
         * @return
         */
        public static JSONObject get() {
            if (weiuiCommon.getVariateStr("configDataIsDist").equals("clear")) {
                weiuiCommon.setVariate("configDataIsDist", "");
                FileUtils.deleteDir(weiui.getApplication().getExternalFilesDir("dist"));
                FileUtils.deleteDir(weiui.getApplication().getExternalFilesDir("update"));
                clear();
            }
            if (configData == null) {
                File tempDir = weiui.getApplication().getExternalFilesDir("dist");
                File lockFile = new File(tempDir, weiuiCommon.getLocalVersion(weiui.getApplication()) + ".lock");
                File jsonFile = new File(tempDir, "config.json");
                if (lockFile.exists() && lockFile.isFile() && jsonFile.exists() && jsonFile.isFile()) {
                    try {
                        FileInputStream fis = new FileInputStream(jsonFile);
                        int length = fis.available();
                        byte [] buffer = new byte[length];
                        int read = fis.read(buffer);
                        fis.close();
                        if (read != -1) {
                            weiuiCommon.setVariate("configDataIsDist", "true");
                            configDataIsDist = true;
                            configData = weiuiJson.parseObject(new String(buffer));
                            return configData;
                        }
                    } catch (Exception ignored) { }
                }
                configData = weiuiJson.parseObject(weiuiCommon.getAssetsJson("weiui/config.json", weiui.getApplication()));
            }
            return configData;
        }

        /**
         * 清除配置
         */
        public static void clear() {
            weiuiCommon.setVariate("configDataIsDist", "false");
            configDataIsDist = false;
            configData = null;
        }

        /**
         * 获取配置值
         * @param key
         * @return
         */
        public static String getString(String key, String defaultVal) {
            return weiuiJson.getString(get(), key, defaultVal);
        }


        /**
         * 获取配置值
         * @param key
         * @return
         */
        public static JSONObject getObject(String key) {
            return weiuiJson.parseObject(get().get(key));
        }

        /**
         * 获取主页地址
         * @return
         */
        public static String getHome() {
            String homePage = weiuiJson.getString(get(), "homePage");
            if (homePage.length() == 0) {
                if (configDataIsDist) {
                    File tempDir = weiui.getApplication().getExternalFilesDir("dist");
                    File indexFile = new File(tempDir, "index.js");
                    if (indexFile.exists() && indexFile.isFile()) {
                        homePage = "file://" + indexFile.getPath();
                    }
                }
            }
            if (homePage.length() == 0) {
                homePage = "file://assets/weiui/index.js";
            }
            return homePage;
        }

        /**
         * 获取主页配置值
         * @param key
         * @param defaultVal
         * @return
         */
        public static String getHomeParams(String key, String defaultVal) {
            JSONObject params = getObject("homePageParams");
            if (params == null) {
                return defaultVal;
            }
            return weiuiJson.getString(params, key, defaultVal);
        }

        /**
         * 判断是否
         * @return
         */
        public static boolean isConfigDataIsDist() {
            return configDataIsDist;
        }
    }

    /**
     * 升级类
     */
    public static class update {

        private static String errorStr;
        private static boolean copySuccess;

        /**
         * 解压文件
         * @param zipFile
         * @param unDir
         * @param callback
         */
        public static void zipToDist(File zipFile, File unDir, Callback callback) {
            try {
                File distDir = weiui.getApplication().getExternalFilesDir("dist");
                List<File> unZipLists = ZipUtils.unzipFile(zipFile, unDir);
                weiuiToDist(new Callback() {
                    @Override
                    public void success() {
                        for (File fileRow : unZipLists) {
                            if (fileRow.isFile()) {
                                FileUtils.copyFile(fileRow, new File(distDir, fileRow.getPath().replaceFirst(unDir.getPath(), "")), () -> true);
                            }
                        }
                        FileUtils.deleteFile(zipFile);
                        FileUtils.deleteDir(unDir);
                        if (callback != null) callback.success();
                    }

                    @Override
                    public void error(String error) {
                        if (callback != null) callback.error(error);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                errorStr = e.getMessage();
                if (callback != null) callback.error(errorStr);
            }
        }

        /**
         * 事件接口
         */
        public interface Callback {
            void success();
            void error(String error);
        }

        /**
         * 将assets/weiui下的文件复制到dist
         * @param callback
         */
        private static void weiuiToDist(Callback callback) {
            File tempDir = weiui.getApplication().getExternalFilesDir("dist");
            File lockFile = new File(tempDir, weiuiCommon.getLocalVersion(weiui.getApplication()) + ".lock");
            if (lockFile.exists() && lockFile.isFile()) {
                if (callback != null) callback.success();
                return;
            }
            //
            copyHandler("weiui", "dist");
            //
            if (copySuccess) {
                try {
                    FileOutputStream fos = new FileOutputStream(lockFile);
                    byte[] bytes = TimeUtils.getNowString().getBytes();
                    fos.write(bytes);
                    fos.close();
                    if (callback != null) callback.success();
                } catch (Exception e) {
                    e.printStackTrace();
                    errorStr = e.getMessage();
                    if (callback != null) callback.error(errorStr);
                }
            } else {
                if (callback != null) callback.error(errorStr);
            }
        }

        /**
         * 复制assets方法
         * @param srcPath
         * @param dstPath
         */
        private static void copyHandler(String srcPath, String dstPath) {
            try {
                String fileNames[] = weiui.getApplication().getAssets().list(srcPath);
                if (fileNames.length > 0) {
                    for (String fileName : fileNames) {
                        if (srcPath.equals("")) {
                            copyHandler(fileName, dstPath + File.separator + fileName);
                        } else {
                            copyHandler(srcPath + File.separator + fileName, dstPath + File.separator + fileName);
                        }
                    }
                } else {
                    File tempDir = weiui.getApplication().getExternalFilesDir(null);
                    if (tempDir != null) {
                        File outFile = new File(tempDir, dstPath);
                        InputStream is = weiui.getApplication().getAssets().open(srcPath);
                        FileOutputStream fos = new FileOutputStream(outFile);
                        byte[] buffer = new byte[1024];
                        int byteCount;
                        while ((byteCount = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, byteCount);
                        }
                        fos.flush();
                        is.close();
                        fos.close();
                    }
                }
                copySuccess = true;
            } catch (Exception e) {
                e.printStackTrace();
                errorStr = e.getMessage();
                copySuccess = false;
            }
        }
    }

    /**
     * 云端类
     */
    public static class cloud {

        private static String apiUrl = "https://app.weiui.cc/";

        /**
         * 加载启动图
         * @param activity
         * @return
         */
        public static int welcome(Activity activity) {
            String welcome_image = weiuiCommon.getCachesString(weiui.getApplication(), "main", "welcome_image");
            if (welcome_image.isEmpty()) {
                return 0;
            }
            int welcome_wait = weiuiParse.parseInt(weiuiCommon.getCachesString(weiui.getApplication(), "main", "welcome_wait"));
            welcome_wait = welcome_wait > 100 ? welcome_wait : 2000;
            //
            File welcomeFile = new File(welcome_image);
            if (welcomeFile.isFile()) {
                Glide.with(activity).asBitmap().load(welcomeFile).apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE)).into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        ImageView tmpImage = activity.findViewById(R.id.fillimage);
                        tmpImage.setImageBitmap(resource);
                        activity.findViewById(R.id.fillbox).setVisibility(View.VISIBLE);
                        activity.findViewById(R.id.mainbox).setVisibility(View.GONE);
                    }
                });
            }
            //
            ProgressBar fillload = activity.findViewById(R.id.fillload);
            new Handler().postDelayed(() -> fillload.post(() -> fillload.setVisibility(View.VISIBLE)), welcome_wait);
            //
            return welcome_wait;
        }

        /**
         * 云数据
         */
        public static void appData() {
            if (weiuiCommon.getVariateStr("configDataNoUpdate").equals("clear")) {
                weiuiCommon.setVariate("configDataNoUpdate", "");
                return;
            }
            String appkey = config.getString("appKey", "");
            if (appkey.length() == 0) {
                return;
            }
            //读取云配置
            Map<String, Object> data = new HashMap<>();
            data.put("appkey", appkey);
            data.put("package", weiui.getApplication().getPackageName());
            data.put("version", weiuiCommon.getLocalVersion(weiui.getApplication()));
            data.put("versionName", weiuiCommon.getLocalVersionName(weiui.getApplication()));
            data.put("screenWidth", ScreenUtils.getScreenWidth());
            data.put("screenHeight", ScreenUtils.getScreenHeight());
            data.put("platform", "android");
            data.put("debug", BuildConfig.DEBUG ? 1 : 0);
            weiuiIhttp.get("main", apiUrl + "api/client/app", data, new weiuiIhttp.ResultCallback() {
                @Override
                public void success(String resData, boolean isCache) {
                    JSONObject json = weiuiJson.parseObject(resData);
                    if (json.getIntValue("ret") == 1) {
                        JSONObject retData = json.getJSONObject("data");
                        saveWelcomeImage(retData.getString("welcome_image"), retData.getIntValue("welcome_wait"));
                        checkUpdateLists(retData.getJSONArray("uplists"), 0, false);
                    }
                }

                @Override
                public void error(String error) {

                }

                @Override
                public void complete() {

                }
            });
        }

        /**
         * 缓存启动图
         * @param url
         * @param wait
         */
        private static void saveWelcomeImage(String url, int wait) {
            if (url.startsWith("http")) {
                new Thread(() -> {
                    try {
                        Bitmap resource = Glide.with(weiui.getApplication()).asBitmap().load(url).apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)).submit().get();
                        if (resource != null) {
                            String path = weiuiCommon.saveImageToGallery(weiui.getApplication(), resource, "welcome_image", null);
                            weiuiCommon.setCachesString(weiui.getApplication(), "main", "welcome_image", path);
                        }
                    } catch (Exception ignored) {
                        weiuiCommon.removeCachesString(weiui.getApplication(), "main", "welcome_image");
                    }
                }).start();
            }else{
                weiuiCommon.removeCachesString(weiui.getApplication(), "main", "welcome_image");
            }
            weiuiCommon.setCachesString(weiui.getApplication(), "main", "welcome_wait", String.valueOf(wait));
        }

        /**
         * 更新部分
         * @param lists
         * @param number
         */
        private static void checkUpdateLists(JSONArray lists, int number, boolean isReboot) {
            if (lists == null || lists.size() == 0) {
                if (config.isConfigDataIsDist()) {
                    FileUtils.deleteDir(weiui.getApplication().getExternalFilesDir("dist"));
                    FileUtils.deleteDir(weiui.getApplication().getExternalFilesDir("update"));
                    reboot();
                }
                return;
            }
            if (number >= lists.size()) {
                if (isReboot) {
                    reboot();
                }
                return;
            }
            //
            JSONObject data = weiuiJson.parseObject(lists.get(number));
            String id = weiuiJson.getString(data, "id");
            String url = weiuiJson.getString(data, "path");
            if (!url.startsWith("http")) {
                checkUpdateLists(lists, number + 1, isReboot);
                return;
            }
            //
            File tempDir = weiui.getApplication().getExternalFilesDir("update");
            File lockFile = new File(tempDir, RxEncryptTool.encryptMD5ToString(url) + ".lock");
            if (lockFile.exists() && lockFile.isFile()) {
                checkUpdateLists(lists, number + 1, isReboot);
                return;
            }
            if (tempDir != null && (tempDir.exists() || tempDir.mkdirs())) {
                //下载zip文件
                File zipSaveFile = new File(tempDir, id + ".zip");
                File zipUnDir = new File(tempDir, id);
                RequestParams requestParams = new RequestParams(url);
                requestParams.setSaveFilePath(zipSaveFile.getPath());
                x.http().get(requestParams, new Callback.CommonCallback<File>() {
                    @Override
                    public void onSuccess(File result) {
                        //下载成功 > 解压 > 覆盖
                        update.zipToDist(zipSaveFile, zipUnDir, new update.Callback() {
                            @Override
                            public void success() {
                                try {
                                    FileOutputStream fos = new FileOutputStream(lockFile);
                                    byte[] bytes = TimeUtils.getNowString().getBytes();
                                    fos.write(bytes);
                                    fos.close();
                                    //
                                    weiuiIhttp.get("checkUpdateLists", apiUrl + "api/client/update/success?id=" + id, null, null);
                                    switch (weiuiJson.getInt(data, "reboot")) {
                                        case 1:
                                            checkUpdateLists(lists, number + 1, true);
                                            break;

                                        case 2:
                                            JSONObject rebootInfo = weiuiJson.parseObject(data.getJSONObject("reboot_info"));
                                            JSONObject newJson = new JSONObject();
                                            newJson.put("title", weiuiJson.getString(rebootInfo, "title"));
                                            newJson.put("message", weiuiJson.getString(rebootInfo, "message"));
                                            weiuiAlertDialog.confirm(weiui.getActivityList().getLast(), newJson, new JSCallback() {
                                                @Override
                                                public void invoke(Object data) {
                                                    Map<String, Object> retData = weiuiMap.objectToMap(data);
                                                    if (weiuiParse.parseStr(retData.get("status")).equals("click")) {
                                                        if (weiuiParse.parseStr(retData.get("title")).equals("确定")) {
                                                            if (weiuiJson.getBoolean(rebootInfo, "confirm_reboot")) {
                                                                reboot();
                                                                return;
                                                            }
                                                        }
                                                        checkUpdateLists(lists, number + 1, isReboot);
                                                    }
                                                }

                                                @Override
                                                public void invokeAndKeepAlive(Object data) {

                                                }
                                            });
                                            break;

                                        default:
                                            checkUpdateLists(lists, number + 1, isReboot);
                                            break;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void error(String error) {

                            }
                        });
                    }

                    @Override
                    public void onError(Throwable ex, boolean isOnCallback) {

                    }

                    @Override
                    public void onCancelled(CancelledException cex) {

                    }

                    @Override
                    public void onFinished() {

                    }
                });
            }
        }

        private static void reboot() {
            config.clear();
            weiui.reboot();
        }
    }
}
