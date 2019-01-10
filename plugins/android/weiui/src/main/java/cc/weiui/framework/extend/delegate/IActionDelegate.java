package cc.weiui.framework.extend.delegate;

import android.os.Bundle;

public interface IActionDelegate {

    /**
     *
     * @param args
     * @param callback 请Module之间根据协议，回调这4个状态做相应的业务处理
     */
    void runAction(Bundle args, IActionCallback callback, Object... extras);

    interface IActionCallback {

        void onActionPrepare();

        void onActionSuccess(Object... result);

        void onActionFailed(int code, String message);

        void onActionFinished();

    }

}
