package cc.weiui.framework.extend.module;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
import cc.weiui.framework.extend.interfaces.OnStringListener;
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

        private static JSONObject configData;
        private static JSONArray verifyDir;

        /**
         * 读取配置
         * @return
         */
        public static JSONObject get() {
            if (configData == null) {
                String temp = "file://assets/weiui/config.json";
                String vTemp = verifyFile(temp);
                if (!temp.equals(vTemp)) {
                    vTemp = vTemp.substring(7);
                    try {
                        FileInputStream fis = new FileInputStream(new File(vTemp));
                        int length = fis.available();
                        byte [] buffer = new byte[length];
                        int read = fis.read(buffer);
                        fis.close();
                        if (read != -1) {
                            weiuiCommon.setVariate("configDataIsDist", "true");
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
            configData = null;
            verifyDir = null;
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
         * 转换修复文件路径
         * @param originalUrl
         * @return
         */
        public static String verifyFile(String originalUrl) {
            if (originalUrl == null ||
                    originalUrl.startsWith("http://") ||
                    originalUrl.startsWith("https://") ||
                    originalUrl.startsWith("ftp://") ||
                    originalUrl.startsWith("data:image/")) {
                return originalUrl;
            }
            String rootPath = "file://assets/weiui";
            if (!originalUrl.startsWith(rootPath)) {
                return originalUrl;
            }
            rootPath+= "/";

            String originalPath = originalUrl.replace(rootPath, "");
            File path = weiui.getApplication().getExternalFilesDir("update");
            if (path == null) {
                return originalUrl;
            }

            if (verifyDir == null) {
                verifyDir = new JSONArray();
                File[] files = path.listFiles();
                List<File> fileList = Arrays.asList(files);
                Collections.sort(fileList, (o1, o2) -> {
                    if (o1.isDirectory() && o2.isFile()) {
                        return -1;
                    }else if (o1.isFile() && o2.isDirectory()) {
                        return 1;
                    }
                    return o1.getName().compareTo(o2.getName());
                });
                Collections.reverse(fileList);
                for (File file1 : files) {
                    if (file1.isDirectory()) {
                        verifyDir.add(file1.getName());
                    }
                }
            }

            String newUrl = "";
            for (int i = 0; i < verifyDir.size(); i++) {
                File tempPath = weiui.getApplication().getExternalFilesDir("update/" + verifyDir.getString(i) + "/" + originalPath);
                if (tempPath != null) {
                    if (isDir(tempPath)) {
                        Log.d("gggggg", "verifyFile: 11");
                    }
                    if (isFile(tempPath)) {
                        Log.d("gggggg", "verifyFile: 22");
                        newUrl = "file://" + tempPath.getPath();
                        break;
                    }
                }
            }

            return newUrl.length() > 0 ? newUrl : originalUrl;
        }

        /**
         * 是否有升级文件
         * @return
         */
        public static boolean verifyIsUpdate() {
            File tempDir = weiui.getApplication().getExternalFilesDir("update");
            if (tempDir == null) {
                return false;
            }
            if (!isDir(tempDir)) {
                return false;
            }
            File[] files = tempDir.listFiles();
            if (files == null) {
                return false;
            }
            boolean isUpdate = false;
            for (File file : files) {
                if(isDir(file)){
                    isUpdate = true;
                    break;
                }
            }
            return isUpdate;
        }

        /**
         * 判断是否文件夹（不存在返回NO）
         * @param file
         * @return
         */
        public static boolean isDir(File file) {
            if (file == null) {
                return false;
            }
            if (!file.exists()) {
                return false;
            }
            return file.isDirectory();
        }

        /**
         * 判断是否文件（不存在返回NO）
         * @param file
         * @return
         */
        public static boolean isFile(File file) {
            if (file == null) {
                return false;
            }
            if (!file.exists()) {
                return false;
            }
            return file.isFile();
        }
    }

    /**
     * 云端类
     */
    public static class cloud {

        private static String apiUrl = "https://console.weiui.app/";

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
            if (config.isFile(welcomeFile)) {
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
                            weiuiCommon.saveImageToGallery(null, resource, "welcome_image", null, path -> weiuiCommon.setCachesString(weiui.getApplication(), "main", "welcome_image", path));
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
            int valid = weiuiJson.getInt(data, "valid");
            if (!url.startsWith("http")) {
                checkUpdateLists(lists, number + 1, isReboot);
                return;
            }
            //
            File tempDir = weiui.getApplication().getExternalFilesDir("update");
            File lockFile = new File(tempDir, RxEncryptTool.encryptMD5ToString(url) + ".lock");
            File zipSaveFile = new File(tempDir, id + ".zip");
            File zipUnDir = new File(tempDir, id);
            if (valid == 1) {
                //开始修复
                if (config.isFile(lockFile)) {
                    checkUpdateLists(lists, number + 1, isReboot);
                    return;
                }
                if (tempDir != null && (tempDir.exists() || tempDir.mkdirs())) {
                    //下载zip文件
                    RequestParams requestParams = new RequestParams(url);
                    requestParams.setSaveFilePath(zipSaveFile.getPath());
                    x.http().get(requestParams, new Callback.CommonCallback<File>() {
                        @Override
                        public void onSuccess(File result) {
                            //下载成功 > 解压
                            try {
                                ZipUtils.unzipFile(zipSaveFile, zipUnDir);
                                FileUtils.deleteFile(zipSaveFile);
                                //
                                FileOutputStream fos = new FileOutputStream(lockFile);
                                byte[] bytes = TimeUtils.getNowString().getBytes();
                                fos.write(bytes);
                                fos.close();
                                //
                                weiuiIhttp.get("checkUpdateLists", apiUrl + "api/client/update/success?id=" + id, null, null);
                                checkUpdateHint(lists, data, number, isReboot);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
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
            }else if (valid == 2) {
                //开始删除
                boolean isDelete = false;
                if (config.isFile(lockFile)) {
                    FileUtils.deleteFile(lockFile);
                    isDelete = true;
                }
                if (config.isDir(zipUnDir)) {
                    FileUtils.deleteDir(zipUnDir);
                    isDelete = true;
                }
                if (!isDelete) {
                    checkUpdateLists(lists, number + 1, isReboot);
                    return;
                }
                weiuiIhttp.get("checkUpdateLists", apiUrl + "api/client/update/delete?id=" + id, null, null);
                checkUpdateHint(lists, data, number, isReboot);
            }
        }

        /**
         * 更新部分(提示处理)
         * @param lists
         * @param number
         */
        private static void checkUpdateHint(JSONArray lists, JSONObject data, int number, boolean isReboot) {
            weiuiBase.config.clear();
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
        }

        /**
         * 重启
         */
        public static void reboot() {
            config.clear();
            weiui.reboot();
        }

        /**
         * 清除热更新缓存
         */
        public static void clearUpdate() {
            FileUtils.deleteDir(weiui.getApplication().getExternalFilesDir("update"));
            reboot();
        }
    }
}
