package cc.weiui.framework.ui.component.ripple;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.ui.action.BasicComponentData;
import com.taobao.weex.ui.component.WXVContainer;

import cc.weiui.framework.R;
import cc.weiui.framework.extend.module.weiuiConstants;

/**
 * Created by WDM on 2018/4/12.
 */
public class Ripple extends WXVContainer<ViewGroup> {

    private static final String TAG = "Ripple";

    private View mView;

    private FrameLayout v_container;

    public Ripple(WXSDKInstance instance, WXVContainer parent, BasicComponentData basicComponentData) {
        super(instance, parent, basicComponentData);
    }

    @Override
    protected ViewGroup initComponentHostView(@NonNull Context context) {
        mView = ((Activity) context).getLayoutInflater().inflate(R.layout.layout_weiui_ripple, null);
        initPagerView();
        //
        if (getEvents().contains(weiuiConstants.Event.READY)) {
            fireEvent(weiuiConstants.Event.READY, null);
        }
        //
        return (ViewGroup) mView;
    }

    @Override
    public void addSubView(View view, int index) {
        if (view == null) {
            return;
        }
        ViewGroup parentViewGroup = (ViewGroup) view.getParent();
        if (parentViewGroup != null ) {
            parentViewGroup.removeView(view);
        }
        v_container.addView(view, index);
    }

    private void initPagerView() {
        v_container = mView.findViewById(R.id.v_container);
        if (getEvents().contains(weiuiConstants.Event.ITEM_CLICK)) {
            v_container.setOnClickListener(v -> fireEvent(weiuiConstants.Event.ITEM_CLICK, null));
        }
    }
}
