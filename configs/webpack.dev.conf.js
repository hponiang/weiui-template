const commonConfig = require('./webpack.common.conf');
const utils = require('./utils');
const config = require('./config');
const webpack = require('webpack');

const weexConfig = commonConfig[1];
weexConfig.watch = true;

let isCreateServer = false;

webpack(weexConfig, (err, stats) => {
    if (!err) {
        if (!isCreateServer) {
            isCreateServer = true;
            utils.createServer(config.dev.contentBase, config.dev.portOnlyDev);
        }
        utils.copySrcToDist(false);
        utils.syncFolderEvent(config.dev.host, config.dev.portOnlyDev, config.dev.portOnlyDev + 1);
    }
});

module.exports = [commonConfig[0], weexConfig];
