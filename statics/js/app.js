import {runNum} from "./global";

let weiui = weex.requireModule('weiui');

let app = {

    jshome: 'http://weiui.cc/dist/0.2.0/',

    openViewCode(str) {
        app.openViewUrl("http://weiui.cc/" + str + ".html");
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
