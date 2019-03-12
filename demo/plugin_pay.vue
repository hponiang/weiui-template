<template>
    <div class="app">

        <weiui_navbar class="navbar">
            <weiui_navbar_item type="back"></weiui_navbar_item>
            <weiui_navbar_item type="title">
                <text class="title">微信/支付宝支付</text>
            </weiui_navbar_item>
            <weiui_navbar_item type="right" @click="viewCode('module/plugin/pay')">
                <weiui_icon content="md-code-working" class="iconr"></weiui_icon>
            </weiui_navbar_item>
        </weiui_navbar>

        <div class="content">

            <text class="info">{{info}}</text>
            <text class="button" @click="weixinPay">微信支付</text>
            <text class="button" @click="alipayPay">支付宝支付</text>

        </div>

    </div>
</template>

<style>
    .app {
        width: 750px;
        flex: 1;
    }

    .navbar {
        width: 750px;
        height: 100px;
    }

    .title {
        font-size: 28px;
        color: #ffffff
    }

    .iconr {
        width: 100px;
        height: 100px;
        color: #ffffff;
    }

    .content {
        flex: 1;
        justify-content: center;
        align-items: center;
    }

    .info {
        font-size: 22px;
        margin-bottom: 20px
    }

    .button {
        font-size: 24px;
        text-align: center;
        margin-top: 32px;
        padding-top: 20px;
        padding-bottom: 20px;
        width: 280px;
        color: #ffffff;
        background-color: #00B4FF;
    }
</style>

<script>
    import {openViewCode} from "../statics/js/app";

    const weiui = weex.requireModule('weiui');
    const pay = weex.requireModule('pay');

    export default {
        data() {
            return {
                info: '',
            }
        },
        methods: {
            viewCode(str) {
                openViewCode(str);
            },

            weixinPay() {
                if (typeof pay === 'undefined') {
                    weiui.alert({
                        title: '温馨提示',
                        message: "检测到未安装pay插件，安装详细请登录http://weiui.cc/",
                    });
                    return;
                }
                weiui.loading();
                weiui.ajax({
                    url: 'https://app.weiui.cc/api/wxpay'
                }, (result) => {
                    if (result.status === 'complete') {
                        weiui.loadingClose();
                    }
                    if (result.status === 'success') {
                        let data = result.result;
                        if (data.ret === 1) {
                            this.info = "";
                            pay.weixin(data.data, (res) => {
                                this.info = res;
                            });
                        }else{
                            weiui.alert(data.msg);
                        }
                    }
                });
            },

            alipayPay() {
                if (typeof pay === 'undefined') {
                    weiui.alert({
                        title: '温馨提示',
                        message: "检测到未安装pay插件，安装详细请登录http://weiui.cc/",
                    });
                    return;
                }
                weiui.loading();
                weiui.ajax({
                    url: 'https://app.weiui.cc/api/alipay'
                }, (result) => {
                    if (result.status === 'complete') {
                        weiui.loadingClose();
                    }
                    if (result.status === 'success') {
                        let data = result.result;
                        if (data.ret === 1) {
                            this.info = "";
                            pay.alipay(data.data.response, (res) => {
                                this.info = res;
                            });
                        }else{
                            weiui.alert(data.msg);
                        }
                    }
                });

            }
        }
    };
</script>
