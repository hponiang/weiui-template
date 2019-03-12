<template>
    <div class="app">

        <weiui_navbar class="navbar">
            <weiui_navbar_item type="back"></weiui_navbar_item>
            <weiui_navbar_item type="title">
                <text class="title">友盟推送模块</text>
            </weiui_navbar_item>
            <weiui_navbar_item type="right" @click="viewCode('module/plugin/umeng')">
                <weiui_icon content="md-code-working" class="iconr"></weiui_icon>
            </weiui_navbar_item>
        </weiui_navbar>

        <div class="content">

            <text class="info">{{info}}</text>
            <text class="button" @click="getToken">获取token</text>

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
        margin-top: 20px;
        padding-top: 20px;
        padding-bottom: 20px;
        width: 220px;
        color: #ffffff;
        background-color: #00B4FF;
    }
</style>

<script>
    import {openViewCode} from "../statics/js/app";

    const weiui = weex.requireModule('weiui');
    const umeng = weex.requireModule('umeng');

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

            getToken() {
                if (typeof umeng === 'undefined') {
                    weiui.alert({
                        title: '温馨提示',
                        message: "检测到未安装umeng插件，安装详细请登录http://weiui.cc/",
                    });
                    return;
                }
                this.info = umeng.getToken();
            }
        }
    };
</script>
