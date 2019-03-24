const commonConfig = require('./webpack.common.conf');
const webpackMerge = require('webpack-merge');
const os = require('os');
const webpack = require('webpack');

const UglifyJsparallelPlugin = require('webpack-uglify-parallel');

const config = require('./config');
const helper = require('./helper');
const utils = require('./utils');

const webConfig = webpackMerge(commonConfig[0], {
    devtool: config.prod.devtool,
    output: {
        path: helper.rootNode('./dist'),
        filename: '[name].web.js',
        sourceMapFilename: '[name].web.map'
    },
    plugins: [
        new webpack.DefinePlugin({
            'process.env': {
                'NODE_ENV': config.prod.env
            }
        }),
        new UglifyJsparallelPlugin({
            workers: os.cpus().length,
            mangle: true,
            compressor: {
                warnings: false,
                drop_console: true,
                drop_debugger: true
            }
        })
    ]
});

const weexConfig = webpackMerge(commonConfig[1], {
    plugins: [
        new UglifyJsparallelPlugin({
            workers: os.cpus().length,
            mangle: true,
            compressor: {
                warnings: false,
                drop_console: true,
                drop_debugger: true
            }
        }),
        new webpack.ProgressPlugin(function handler(percentage, msg) {
            if (percentage === 1) {
                utils.copySrcToDist();
                utils.syncFolderEvent(null, null, null, true);
            }
        }),
        ...commonConfig[1].plugins
    ]
});

module.exports = [webConfig, weexConfig];
