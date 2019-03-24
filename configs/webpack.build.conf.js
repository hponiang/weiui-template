const commonConfig = require('./webpack.common.conf');
const utils = require('./utils');
const webpack = require('webpack');
const webpackMerge = require('webpack-merge');

const weexConfig = webpackMerge(commonConfig[1], {
    plugins: [
        new webpack.ProgressPlugin(function handler(percentage, msg) {
            if (percentage === 1) {
                utils.copySrcToDist();
                utils.syncFolderEvent(null, null, null, true);
            }
        })
    ]
});

module.exports = [commonConfig[0], weexConfig];
