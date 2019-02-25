<template>

    <div class="app">

        <weiui_navbar class="navbar">
            <weiui_navbar_item type="left" @click="scaner">
                <weiui_icon class="navbar-icon" :weiui="{content: 'tb-scan'}"></weiui_icon>
            </weiui_navbar_item>

            <weiui_navbar_item type="title">
                <text class="navbar-title">WEIUI</text>
            </weiui_navbar_item>

            <weiui_navbar_item type="right" @click="refresh">
                <weiui_icon class="navbar-icon" :weiui="{content: 'md-refresh'}"></weiui_icon>
            </weiui_navbar_item>
        </weiui_navbar>

        <scroller class="list">

            <text class="list-title">Components</text>

            <div class="list-item" v-for="(item, index) in components" :key="index" @click="openUrl(item.url)">
                <div class="list-item-left">
                    <weiui_icon class="list-left-icon" :weiui="{content: item.icon}"></weiui_icon>
                    <text class="list-left-title">{{item.title}}</text>
                </div>
                <div class="list-item-right">
                    <text class="list-right-title"><{{item.title_en}}></text>
                    <weiui_icon class="list-right-icon" :weiui="{content: 'tb-right 70%'}"></weiui_icon>
                </div>
            </div>

            <text class="list-title">Module</text>

            <div class="list-item" v-for="(item, index) in module" :key="index" @click="openUrl(item.url)">
                <div class="list-item-left">
                    <weiui_icon class="list-left-icon" :weiui="{content: item.icon}"></weiui_icon>
                    <text class="list-left-title">{{item.title}}</text>
                </div>
                <div class="list-item-right">
                    <text class="list-right-title">{{item.title_en}}</text>
                    <weiui_icon class="list-right-icon" :weiui="{content: 'tb-right 70%'}"></weiui_icon>
                </div>
            </div>

            <text class="list-title">Third Module</text>

            <div class="list-item" v-for="(item, index) in third_module" :key="index" @click="openThird(item.url)">
                <div class="list-item-left">
                    <weiui_icon class="list-left-icon" :weiui="{content: item.icon}"></weiui_icon>
                    <text class="list-left-title">{{item.title}}</text>
                </div>
                <div class="list-item-right">
                    <text class="list-right-title">{{item.title_en}}</text>
                    <weiui_icon class="list-right-icon" :weiui="{content: 'tb-right 70%'}"></weiui_icon>
                </div>
            </div>

            <text class="list-title">About Weiui</text>

            <div class="list-item" v-for="(item, index) in about_lists" :key="index" @click="openWeb(item.url)">
                <div class="list-item-left">
                    <weiui_icon class="list-left-icon" :weiui="{content: item.icon}"></weiui_icon>
                    <text class="list-left-title">{{item.title}}</text>
                </div>
                <div class="list-item-right">
                    <text class="list-right-title">{{item.title_en}}</text>
                    <weiui_icon class="list-right-icon" :weiui="{content: 'tb-right 70%'}"></weiui_icon>
                </div>
            </div>

            <div class="list-title-box" v-if="history.length > 0">
                <text class="list-title">扫码历史</text>
                <text class="list-subtitle" @click="clearHistory()">清空历史</text>
            </div>

            <div v-if="history.length > 0">

                <div class="list-item" v-for="(text, index) in history" :key="index" @click="openAuto(text)">
                    <div class="list-item-left">
                        <text class="list-left-title-history">{{text}}</text>
                    </div>
                    <div class="list-item-right">
                        <weiui_icon class="list-right-icon" :weiui="{content: 'tb-right 70%'}"></weiui_icon>
                    </div>
                </div>

            </div>

        </scroller>

    </div>

</template>

<style scoped>
    .app {
        flex: 1;
    }

    .navbar {
        width: 750px;
        height: 100px;
    }

    .navbar-title {
        font-size: 32px;
        color: #ffffff;
    }

    .navbar-icon {
        width: 100px;
        height: 100px;
        color: #ffffff;
    }

    .list {
        width: 750px;
        flex: 1;
    }

    .list-title-box {
        flex-direction: row;
        align-items: center;
    }

    .list-title {
        padding-top: 36px;
        padding-right: 24px;
        padding-bottom: 24px;
        padding-left: 24px;
        font-size: 28px;
        color: #757575;
    }

    .list-subtitle {
        position: absolute;
        right: 24px;
        bottom: 24px;
        font-size: 24px;
    }

    .list-item {
        flex-direction: row;
        align-items: center;
        justify-content: space-between;
        height: 100px;
        width: 750px;
        padding-left: 20px;
        padding-right: 20px;
        border-top-width: 1px;
        border-top-color: #e8e8e8;
        border-top-style: solid;
    }

    .list-item-left {
        flex-direction: row;
        align-items: center;
        justify-content: flex-start;
        height: 100px;
        flex: 1;
    }

    .list-left-icon {
        width: 60px;
        height: 60px;
        color: #3EB4FF;
    }

    .list-left-title {
        color: #242424;
        padding-left: 12px;
        width: 380px;
        font-size: 26px;
        text-overflow: ellipsis;
        lines: 1;
    }

    .list-left-title-history {
        color: #242424;
        padding-left: 12px;
        width: 600px;
        font-size: 26px;
        text-overflow: ellipsis;
        lines: 1;
    }

    .list-right-title {
        color: #a2a2a2;
        padding-right: 3px;
        font-size: 22px;
        text-overflow: ellipsis;
        lines: 1;
    }

    .list-right-icon {
        width: 40px;
        height: 40px;
        color: #C9C9CE;
    }

    .list-item-right {
        flex-direction: row;
        align-items: center;
        justify-content: flex-end;
        height: 100px;
    }
</style>

<script>
    import {jsonParse, jsonStringify} from "../statics/js/global";
    import {jshome, openViewCode, openViewUrl} from "../statics/js/app";

    const weiui = weex.requireModule('weiui');

    export default {
        data() {
            return {
                components: [{
                    title: '轮播控件',
                    title_en: 'weiui_banner',
                    icon: 'md-easel',
                    url: jshome + 'component_banner.js',
                }, {
                    title: '常用按钮',
                    title_en: 'weiui_button',
                    icon: 'logo-youtube',
                    url: jshome + 'component_button.js',
                }, {
                    title: '网格容器',
                    title_en: 'weiui_grid',
                    icon: 'md-grid',
                    url: jshome + 'component_grid.js',
                }, {
                    title: '字体图标',
                    title_en: 'weiui_icon',
                    icon: 'logo-ionic',
                    url: jshome + 'component_icon.js',
                }, {
                    title: '跑马文字',
                    title_en: 'weiui_marquee',
                    icon: 'md-code-working',
                    url: jshome + 'component_marquee.js',
                }, {
                    title: '导航栏',
                    title_en: 'weiui_navbar',
                    icon: 'md-menu',
                    url: jshome + 'component_navbar.js',
                }, {
                    title: '列表容器',
                    title_en: 'weiui_list',
                    icon: 'md-list',
                    url: jshome + 'component_list.js',
                }, {
                    title: '滚动文字',
                    title_en: 'weiui_scroll_text',
                    icon: 'ios-more',
                    url: jshome + 'component_scroll_text.js',
                }, {
                    title: '侧边栏',
                    title_en: 'weiui_side_panel',
                    icon: 'md-albums',
                    url: jshome + 'component_side_panel.js',
                }, {
                    title: '标签页面',
                    title_en: 'weiui_tabbar',
                    icon: 'md-filing',
                    url: jshome + 'component_tabbar.js',
                }],

                module: [{
                    title: '页面功能',
                    title_en: 'newPage',
                    icon: 'md-book',
                    url: jshome + 'module_page.js',
                }, {
                    title: '系统信息',
                    title_en: 'system',
                    icon: 'ios-cog',
                    url: jshome + 'module_system.js',
                }, {
                    title: '数据缓存',
                    title_en: 'caches',
                    icon: 'md-beaker',
                    url: jshome + 'module_caches.js',
                }, {
                    title: '单位转换',
                    title_en: 'weex px',
                    icon: 'md-calculator',
                    url: jshome + 'module_weexpx.js',
                }, {
                    title: '确认对话框',
                    title_en: 'alert',
                    icon: 'md-alert',
                    url: jshome + 'module_alert.js',
                }, {
                    title: '等待弹窗',
                    title_en: 'loading',
                    icon: 'tb-loading',
                    url: jshome + 'module_loading.js',
                }, {
                    title: '验证弹窗',
                    title_en: 'captcha',
                    icon: 'md-checkmark-circle',
                    url: jshome + 'module_captcha.js',
                }, {
                    title: '二维码扫描',
                    title_en: 'scaner',
                    icon: 'tb-scan',
                    url: jshome + 'module_scaner.js',
                }, {
                    title: '跨域异步请求',
                    title_en: 'ajax',
                    icon: 'md-git-pull-request',
                    url: jshome + 'module_ajax.js',
                }, {
                    title: '剪切板',
                    title_en: 'clipboard',
                    icon: 'md-copy',
                    url: jshome + 'module_plate.js',
                }, {
                    title: '提示消息',
                    title_en: 'toast',
                    icon: 'md-notifications',
                    url: jshome + 'module_toast.js',
                }, {
                    title: '广告弹窗',
                    title_en: 'adDialog',
                    icon: 'logo-buffer',
                    url: jshome + 'module_ad_dialog.js',
                }, {
                    title: '城市选择器',
                    title_en: 'citypicker',
                    icon: 'md-pin',
                    url: jshome + 'third_citypicker.js',
                }, {
                    title: '图片选择器',
                    title_en: 'pictureSelector',
                    icon: 'md-camera',
                    url: jshome + 'third_picture.js',
                }, {
                    title: '更多拓展模块',
                    title_en: 'expandModule',
                    icon: 'md-more',
                    url: jshome + 'index_expand.js',
                }],

                third_module: [{
                    title: '融云通信模块',
                    title_en: 'rongcloud',
                    icon: 'tb-community',
                    url: 'rongcloud',
                }, {
                    title: '友盟推送模块',
                    title_en: 'umeng',
                    icon: 'md-send',
                    url: 'umeng',
                }, {
                    title: '第三方支付',
                    title_en: 'pay',
                    icon: 'tb-sponsor',
                    url: 'pay',
                }/*, {
                    title: '视频播放器',
                    title_en: 'video',
                    icon: 'tb-video-fill',
                    url: 'video',
                }*/],

                about_lists: [{
                    title: '开发文档',
                    title_en: 'document',
                    icon: 'md-code-working',
                    url: 'http://weiui.cc',
                }, {
                    title: '托管平台',
                    title_en: 'github',
                    icon: 'logo-github',
                    url: 'https://github.com/kuaifan/weiui',
                }, {
                    title: '个人博客',
                    title_en: 'http://kuaifan.vip',
                    icon: 'logo-rss',
                    url: 'http://kuaifan.vip',
                }, {
                    title: 'WEIUI版本',
                    title_en: weiui.getVersionName(),
                    icon: 'md-information-circle',
                    url: 'http://weiui.cc',
                }],

                history: [],
            }
        },

        mounted() {
            this.history = jsonParse(weiui.getCachesString("scaner", []), []);
            //
            weiui.setPageBackPressed(null, function(){
                weiui.confirm({
                    title: "温馨提示",
                    message: "你确定要退出WEIUI吗？",
                    buttons: ["取消", "确定"]
                }, (result)=>{
                    if (result.status === "click" && result.title === "确定") {
                        weiui.closePage(null);
                    }
                });
            });
        },

        methods: {
            scaner() {
                weiui.openScaner(null, (res) => {
                    if (res.status === "success") {
                        this.history.unshift(res.text);
                        weiui.setCachesString("scaner", jsonStringify(this.history), 0);
                        this.openAuto(res.text);
                    }
                });
            },

            refresh() {
                weiui.reloadPage();
            },

            clearHistory() {
                weiui.confirm({
                    title: "删除提示",
                    message: "你确定要删除扫码记录吗？",
                    buttons: ["取消", "确定"]
                }, (result)=>{
                    if (result.status === "click" && result.title === "确定") {
                        this.history = [];
                        weiui.setCachesString("scaner", jsonStringify(this.history), 0);
                    }
                });
            },

            openUrl(url) {
                weiui.openPage({
                    url: url,
                    pageType: 'weex'
                });
            },

            openWeb(url) {
                openViewUrl(url);
            },

            openThird(url) {
                openViewCode("module/third/" + url);
            },

            openAuto(url) {
                weiui.openPage({
                    url: url,
                    pageType: 'auto'
                });
            },
        }
    };
</script>
