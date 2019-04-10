package cc.weiui.framework.extend.adapter;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.taobao.weex.WXSDKManager;
import com.taobao.weex.adapter.DrawableStrategy;
import com.taobao.weex.adapter.IDrawableLoader;

import java.io.IOException;
import java.io.InputStream;

import cc.weiui.framework.activity.PageActivity;
import cc.weiui.framework.extend.bean.PageBean;
import cc.weiui.framework.extend.integration.glide.Glide;
import cc.weiui.framework.extend.integration.glide.RequestBuilder;
import cc.weiui.framework.extend.integration.glide.load.engine.DiskCacheStrategy;
import cc.weiui.framework.extend.integration.glide.request.RequestOptions;
import cc.weiui.framework.extend.integration.glide.request.target.SimpleTarget;
import cc.weiui.framework.extend.integration.glide.request.transition.Transition;
import cc.weiui.framework.extend.module.weiuiHtml;

public class DrawableLoader implements IDrawableLoader {

    private Context mContext;

    public DrawableLoader(Context context) {
        mContext = context;
    }

    @Override
    public void setDrawable(String url, DrawableTarget drawableTarget, DrawableStrategy drawableStrategy) {
        if (url.contains("/./")) {
            url = url.replaceAll("/\\./", "/");
        }
        String finalUrl = url;
        WXSDKManager.getInstance().postOnUiThread(() -> {

            String tempUrl = finalUrl;
            if (tempUrl.startsWith("//")) {
                tempUrl = "http:" + finalUrl;
            } else if (!tempUrl.startsWith("http") && !tempUrl.startsWith("ftp:") && !tempUrl.startsWith("file:") && !tempUrl.startsWith("data:image/")) {
                if (mContext instanceof PageActivity) {
                    PageBean mPageBean = ((PageActivity) mContext).getPageInfo();
                    if (mPageBean != null) {
                        tempUrl = weiuiHtml.repairUrl(tempUrl, mPageBean.getUrl());
                    }
                }
            }

            try {
                RequestBuilder<Drawable> myLoad;
                if (tempUrl.startsWith("file://assets/")) {
                    Bitmap myBitmap = getImageFromAssetsFile(mContext, tempUrl.substring(14));
                    myLoad = Glide.with(mContext).load(myBitmap);
                }else if (tempUrl.startsWith("file:///assets/")) {
                    Bitmap myBitmap = getImageFromAssetsFile(mContext, tempUrl.substring(15));
                    myLoad = Glide.with(mContext).load(myBitmap);
                }else{
                    myLoad = Glide.with(mContext).load(tempUrl);
                }
                //
                RequestOptions myOptions = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL);
                myLoad.apply(myOptions).into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        drawableTarget.setDrawable(resource, true);
                    }
                });
            } catch (IllegalArgumentException ignored) {

            } catch (Exception ignored) {

            }
        }, 0);
    }

    private Bitmap getImageFromAssetsFile(Context context, String fileName)
    {
        Bitmap image = null;
        AssetManager am = context.getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }
}
