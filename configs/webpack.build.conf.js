const commonConfig = require('./webpack.common.conf');
const utils = require('./utils');
const webpack = require('webpack');

const weexConfig = commonConfig[1];

webpack(weexConfig, (err, stats) => {
    if (err) {
        console.err('COMPILE ERROR:', err.stack);
    } else {
        utils.copySrcToDist();
        utils.syncFolderEvent(null, null, null, true);
    }
});

module.exports = [commonConfig[0], weexConfig];
