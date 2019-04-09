import {runNum} from "./global";

let weiui = weex.requireModule('weiui');

let app = {

    jshome: 'https://weiui.app/dist/0.3.0/',

    openViewCode(str) {
        app.openViewUrl("https://weiui.app/" + str + ".html");
    },

    openViewUrl(url) {
        weiui.openPage({
            url: app.jshome + 'index_browser.js',
            pageType: 'weex',
            statusBarColor: "#3EB4FF",
            params: {
                title: "WEIUI",
                url: url,
            }
        });
    },

    checkVersion(compareVersion) {
        if (typeof weiui.getVersion !== "function") {
            return false;
        }
        return runNum(weiui.getVersion()) >= runNum(compareVersion);
    },

};

module.exports = app;
