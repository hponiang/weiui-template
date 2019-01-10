package cc.weiui.pay.weiui.ui.module;

import cc.weiui.framework.extend.delegate.IDataDelegate;
import cc.weiui.framework.extend.delegate.IDelegateFactory;
import cc.weiui.framework.extend.delegate.ModuleDelegate;
import cc.weiui.pay.weiui.ui.weiui_pay;

public class WebModule implements IDelegateFactory {

    private weiui_pay myApp;

    @Override
    public IDataDelegate getDataTransfer(String code) {
        if (myApp == null) myApp = new weiui_pay();
        return ModuleDelegate.newIDataDelegate(myApp, code);
    }

}

