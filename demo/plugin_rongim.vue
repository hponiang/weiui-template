<template>
    <div class="app">

        <navbar class="navbar">
            <navbar-item type="back"></navbar-item>
            <navbar-item type="title">
                <text class="title">融云通信</text>
            </navbar-item>
            <navbar-item type="right" @click="viewCode('module/plugin/rongim')">
                <icon content="md-code-working" class="iconr"></icon>
            </navbar-item>
        </navbar>

        <div class="content">

            <text class="info">{{info}}</text>
            <text class="button" @click="login">连接登录</text>

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
    const rongim = weex.requireModule('rongim');

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

            login() {
                if (typeof rongim === 'undefined') {
                    weiui.alert({
                        title: '温馨提示',
                        message: "检测到未安装rongim插件，安装详细请登录http://weiui.cc/",
                    });
                    return;
                }
                weiui.loading();
                rongim.login({
                    userid: 'weiui_' + WXEnvironment.platform,
                    username: '测试会员',
                    userimg: 'https://www.baidu.com/img/baidu_resultlogo@2.png',
                }, (result) => {
                    weiui.loadingClose();
                    this.info = result;
                });
            }
        }
    };
</script>
