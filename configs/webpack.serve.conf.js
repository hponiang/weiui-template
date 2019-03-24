const commonConfig = require('./webpack.common.conf');
const webpackMerge = require('webpack-merge');
const chalk = require('chalk');
const webpack = require('webpack');
const ip = require('ip').address();

const HtmlWebpackPlugin = require('html-webpack-plugin');
const ScriptExtHtmlWebpackPlugin = require('script-ext-html-webpack-plugin');
const FriendlyErrorsPlugin = require('friendly-errors-webpack-plugin');
const portfinder = require('portfinder');

const config = require('./config');
const utils = require('./utils');
const helper = require('./helper');

let serverStatus = 0;
let serverSyncPort = config.dev.port + 1;

const postMessageToOpenPage = (entry) => {
    let entrys = Object.keys(entry);
    let openpage = config.dev.openPage;
    entrys = entrys.filter(entry => entry !== 'vendor');
    if (entrys.indexOf('index') > -1) {
        openpage += `?page=index.js`;
    } else {
        openpage += `?page=${entrys[0]}.js`;
    }
    if (entrys.length > 1) {
        openpage += `&entrys=${entrys.join('|')}`
    }
    openpage += `&socket_host=${devWebpackConfig.devServer.host}`;
    openpage += `&socket_port=${serverSyncPort}`;
    return openpage;
};

const generateHtmlWebpackPlugin = (entry) => {
    let entrys = Object.keys(entry);
    entrys = entrys.filter(entry => entry !== 'vendor');
    return entrys.map(name => {
        return new HtmlWebpackPlugin({
            filename: name + '.html',
            template: helper.rootNode(`web/index.html`),
            isDevServer: true,
            chunksSortMode: 'dependency',
            inject: true,
            devScripts: config.dev.htmlOptions.devScripts,
            chunks: ['vendor', name]
        })
    });
};

const devWebpackConfig = webpackMerge(commonConfig[0], {
    module: {
        rules: utils.styleLoaders({sourceMap: config.dev.cssSourceMap, usePostCSS: true})
    },
    devtool: config.dev.devtool,
    plugins: [
        new webpack.DefinePlugin({
            'process.env': {
                'NODE_ENV': config.dev.env
            }
        }),
        ...generateHtmlWebpackPlugin(commonConfig[0].entry),
        new ScriptExtHtmlWebpackPlugin({
            defaultAttribute: 'defer'
        })
    ],
    devServer: {
        clientLogLevel: 'warning',
        compress: true,
        contentBase: config.dev.contentBase,
        host: config.dev.host,
        port: config.dev.port,
        historyApiFallback: config.dev.historyApiFallback,
        public: config.dev.public,
        open: config.dev.open,
        watchContentBase: config.dev.watchContentBase,
        overlay: config.dev.errorOverlay ? {warnings: false, errors: true} : false,
        proxy: config.dev.proxyTable,
        quiet: true,
        openPage: '',
        watchOptions: config.dev.watchOptions
    }
});

const weexConfig = webpackMerge(commonConfig[1], {
    watch: true
});

webpack(weexConfig, (err, stats) => {
    if (err) {
        console.err('COMPILE ERROR:', err.stack);
    } else {
        utils.copySrcToDist();
        if (serverStatus === 0) {
            serverStatus = 1;
            utils.syncFolderEvent(devWebpackConfig.devServer.host, devWebpackConfig.devServer.port, serverSyncPort, true);
        }else{
            utils.syncFolderEvent(devWebpackConfig.devServer.host, devWebpackConfig.devServer.port, serverSyncPort, false);
        }
    }
});

module.exports = new Promise((resolve, reject) => {
    portfinder.basePort = devWebpackConfig.devServer.port;
    portfinder.getPort((err, port) => {
        if (err) {
            reject(err);
            return;
        }
        devWebpackConfig.devServer.port = port;
        devWebpackConfig.devServer.public = `${ip}:${port}`;
        utils.portIsOccupied(serverSyncPort, (err, sPort) => {
            if (err) {
                reject(err);
                return;
            }
            serverSyncPort = sPort;
            devWebpackConfig.devServer.openPage = encodeURI(postMessageToOpenPage(commonConfig[0].entry));
            devWebpackConfig.plugins.push(new FriendlyErrorsPlugin({
                compilationSuccessInfo: {
                    messages: [`Your application is running here: ${chalk.blue.underline(`http://${devWebpackConfig.devServer.host}:${port}`)}.`],
                },
                onErrors: config.dev.notifyOnErrors ? utils.createNotifierCallback() : undefined
            }));
            resolve(devWebpackConfig)
        });
    })
});
