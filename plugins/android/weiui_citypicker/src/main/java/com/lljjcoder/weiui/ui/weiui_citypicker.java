package com.lljjcoder.weiui.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;

import com.alibaba.fastjson.JSONObject;
import com.lljjcoder.weiui.library.city.CityPickerView;
import com.lljjcoder.weiui.library.city.bean.CityBean;
import com.lljjcoder.weiui.library.city.bean.DistrictBean;
import com.lljjcoder.weiui.library.city.bean.ProvinceBean;
import com.lljjcoder.weiui.ui.module.WebModule;
import com.lljjcoder.weiui.ui.module.WeexModule;
import com.taobao.weex.WXSDKEngine;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.common.WXException;

import java.util.HashMap;
import java.util.Map;

import cc.weiui.framework.extend.delegate.ModuleDelegate;
import cc.weiui.framework.extend.integration.swipebacklayout.BGAKeyboardUtil;
import cc.weiui.framework.extend.module.weiuiJson;

public class weiui_citypicker {

    public static void init() {
        try {
            WXSDKEngine.registerModule("weiui_citypicker", WeexModule.class);
            ModuleDelegate.register("weiui_citypicker", new WebModule());
        } catch (WXException e) {
            e.printStackTrace();
        }
    }

    /****************************************************************************************/
    /****************************************************************************************/
    /****************************************************************************************/

    private CityPickerView cityPicker;

    /**
     * 选择地址
     * @param object
     * @param callback
     */
    public void select(Context context, Object object, final JSCallback callback) {
        JSONObject json = weiuiJson.parseObject(object);
        final String province = weiuiJson.getString(json, "province");
        final String city = weiuiJson.getString(json, "city");
        final String area = weiuiJson.getString(json, "area");
        //
        if (cityPicker == null) {
            cityPicker = new CityPickerView.Builder(context)
                    .textSize(17)
                    .areaOther(json.getBooleanValue("areaOther"))
                    .titleTextColor("#000000")
                    .confirTextColor("#333333")
                    .cancelTextColor("#333333")
                    .backgroundPop(0xa0000000)
                    .province(province)
                    .city(city)
                    .district(area)
                    .textColor(Color.parseColor("#000000"))
                    .provinceCyclic(false)
                    .cityCyclic(false)
                    .districtCyclic(false)
                    .visibleItemsCount(8)
                    .itemPadding(10)
                    .build();
        }
        cityPicker.setOnCityItemClickListener(new CityPickerView.OnCityItemClickListener() {
            @Override
            public void onSelected(ProvinceBean province, CityBean city, DistrictBean district) {
                if (callback != null) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("province", province.getName());
                    data.put("city", city.getName());
                    data.put("area", district != null ? district.getName() : "");
                    callback.invoke(data);
                }
            }

            @Override
            public void onCancel() {

            }
        });
        cityPicker.setProvince(province);
        cityPicker.setCity(city);
        cityPicker.setDistrict(area);
        cityPicker.show();
        BGAKeyboardUtil.closeKeyboard((Activity) context);
    }
}
