package cc.weiui.framework.extend.delegate;

import java.util.Map;

public interface IDataDelegate {

    /**
     * 请Module之间存在数据调用关系的双发，协议Bundle的各个参数属性的Key和Type
     * Bundle是基本、序列化类型数据封装，Object...是复杂类型数据传递，也请各自协商
     *
     * @param args
     * @param extras
     * @return
     * @throws DelegateException
     */
    Object getData(Map<String, Object> args, Object... extras) throws DelegateException;

}
