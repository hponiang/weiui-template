const commonConfig = require('./webpack.common.conf');
const utils = require('./utils');
const webpack = require('webpack');

const weexConfig = commonConfig[1];

webpack(weexConfig, (err, stats) => {
    if (!err) {
        utils.copySrcToDist(true);
        utils.syncFolderEvent();
    }
});

module.exports = [commonConfig[0], weexConfig];
