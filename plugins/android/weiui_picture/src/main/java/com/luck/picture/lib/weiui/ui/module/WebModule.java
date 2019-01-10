package com.luck.picture.lib.weiui.ui.module;


import com.luck.picture.lib.weiui.ui.weiui_picture;

import cc.weiui.framework.extend.delegate.IDataDelegate;
import cc.weiui.framework.extend.delegate.IDelegateFactory;
import cc.weiui.framework.extend.delegate.ModuleDelegate;

public class WebModule implements IDelegateFactory {

    private weiui_picture myApp;

    @Override
    public IDataDelegate getDataTransfer(String code) {
        if (myApp == null) myApp = new weiui_picture();
        return ModuleDelegate.newIDataDelegate(myApp, code);
    }

}

