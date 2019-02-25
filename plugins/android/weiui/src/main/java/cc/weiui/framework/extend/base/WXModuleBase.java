package cc.weiui.framework.extend.base;

import android.content.Context;
import com.taobao.weex.common.WXModule;
import cc.weiui.framework.activity.PageActivity;

public class WXModuleBase extends WXModule {

    public PageActivity getActivity() {
        return (PageActivity) mWXSDKInstance.getContext();
    }

    public Context getContext() {
        return mWXSDKInstance.getContext();
    }
}
