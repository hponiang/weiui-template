package cc.weiui.framework.extend.delegate;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.alibaba.fastjson.JSONObject;
import com.taobao.weex.bridge.JSCallback;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import cc.weiui.framework.extend.module.weiuiJson;
import cc.weiui.framework.extend.module.weiuiParse;
import cc.weiui.framework.extend.view.webviewBridge.JsCallback;
import cc.weiui.framework.ui.weiui;

public class ModuleDelegate {

    private final Map<String, IDelegateFactory> factoryMap = new HashMap<>();

    private static final ModuleDelegate mInstance = new ModuleDelegate();

    private ModuleDelegate() {

    }

    public static ModuleDelegate getInstance() {
        return mInstance;
    }

    public static void register(String key, IDelegateFactory factory) {
        mInstance.factoryMap.put(key, factory);
    }

    public static IDataDelegate newIDataDelegate(Object myApp, String code) {
        return (args, extras) -> {
            try {
                Method[] methods = myApp.getClass().getDeclaredMethods();
                for (Method method : methods) {
                    if (method.getModifiers() != Modifier.PUBLIC) {
                        continue;
                    }
                    String sign = method.getName();
                    if (!sign.equals(code)) {
                        continue;
                    }
                    Class[] argsTypes = method.getParameterTypes();
                    int len = argsTypes.length;
                    Object[] values = new Object[len];
                    for (int k = 0; k < len; k++) {
                        Object temp = k < extras.length ? extras[k] : null;
                        if (temp == null) {
                            values[k] = null;
                            continue;
                        }
                        Class cls = argsTypes[k];
                        if (cls == String.class) {
                            values[k] = weiuiParse.parseStr(extras[k]);
                        } else if (cls == int.class) {
                            values[k] = weiuiParse.parseInt(extras[k]);
                        } else if (cls == long.class) {
                            values[k] = weiuiParse.parseLong(extras[k]);
                        } else if (cls == float.class) {
                            values[k] = weiuiParse.parseFloat(extras[k]);
                        } else if (cls == double.class) {
                            values[k] = weiuiParse.parseDouble(extras[k]);
                        } else if (cls == boolean.class) {
                            values[k] = weiuiParse.parseBool(extras[k]);
                        } else if (cls == JSONObject.class) {
                            values[k] = weiuiJson.parseObject(extras[k]);
                        } else if (cls == JSCallback.class) {
                            values[k] = extras[k] instanceof JsCallback ? weiui.MCallback((JsCallback) extras[k]) : null;
                        } else if (cls == Context.class) {
                            values[k] = extras[k] instanceof View ? ((View) extras[k]).getContext() : (extras[k] instanceof Context ? ((Context) extras[k]) : null);
                        } else if (cls == Object.class) {
                            values[k] = extras[k];
                        }
                    }
                    if ("void".equals(method.getReturnType().getName())) {
                        method.invoke(myApp, values);
                    }else{
                        return method.invoke(myApp, values);
                    }
                }
            } catch (Exception e) {
                if (e.getCause() != null) {
                    Log.d("WebModule:" + code, "method execute error:" + e.getCause().getMessage());
                }
                Log.d("WebModule:" + code, "method execute error:" + e.getMessage());
            }
            return null;
        };
    }

    public Object getData(String factoryCode, String code, Object... extras) {
        IDelegateFactory factory = factoryMap.get(factoryCode);
        if (factory != null) {
            IDataDelegate transfer = factory.getDataTransfer(code);
            if (transfer != null) {
                try {
                    return transfer.getData(null, extras);
                } catch (DelegateException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public Object getData(String factoryCode, String code, Map<String, Object> args, Object... extras) {
        IDelegateFactory factory = factoryMap.get(factoryCode);
        if (factory != null) {
            IDataDelegate transfer = factory.getDataTransfer(code);
            if (transfer != null) {
                try {
                    return transfer.getData(args, extras);
                } catch (DelegateException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public Object getDataThrow(String factoryCode, String code, Map<String, Object> args, Object... extras) throws DelegateException {
        IDelegateFactory factory = factoryMap.get(factoryCode);
        if (factory != null) {
            IDataDelegate transfer = factory.getDataTransfer(code);
            if (transfer != null) {
                return transfer.getData(args, extras);
            }
        }
        throw new DelegateException("unknow data transfer");
    }
}
