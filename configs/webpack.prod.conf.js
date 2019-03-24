const commonConfig = require('./webpack.common.conf');
const webpackMerge = require('webpack-merge');
const os = require('os');
const webpack = require('webpack');

const UglifyJsparallelPlugin = require('webpack-uglify-parallel');

const config = require('./config');
const helper = require('./helper');
const utils = require('./utils');

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
        ...commonConfig[1].plugins
    ]
});

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

webpack(webConfig, (err, stats) => {
    if (err) {
        console.err('COMPILE ERROR:', err.stack);
    } else {
        utils.copySrcToDist();
        utils.syncFolderEvent(null, null, null, true);
    }
});

module.exports = [weexConfig, webConfig];
