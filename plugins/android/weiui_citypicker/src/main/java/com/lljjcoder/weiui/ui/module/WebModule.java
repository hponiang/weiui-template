package com.lljjcoder.weiui.ui.module;

import com.lljjcoder.weiui.ui.weiui_citypicker;

import cc.weiui.framework.extend.delegate.IDataDelegate;
import cc.weiui.framework.extend.delegate.IDelegateFactory;
import cc.weiui.framework.extend.delegate.ModuleDelegate;


public class WebModule implements IDelegateFactory {

    private weiui_citypicker myApp;

    @Override
    public IDataDelegate getDataTransfer(final String code) {
        if (myApp == null) myApp = new weiui_citypicker();
        return ModuleDelegate.newIDataDelegate(myApp, code);
    }
}