import {runNum} from "./global";

let weiui = app.requireModule('weiui');

let common = {

    jshome: 'https://weiui.app/dist/0.4.8/pages/',

    openViewCode(str) {
        common.openViewUrl("https://weiui.app/" + str + ".html");
    },

    openViewUrl(url) {
        weiui.openPage({
            url: common.jshome + 'index_browser.js',
            pageType: 'app',
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

module.exports = common;
