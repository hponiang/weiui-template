const commonConfig = require('./webpack.common.conf');
const utils = require('./utils');
const config = require('./config');
const webpack = require('webpack');

const weexConfig = commonConfig[1];
weexConfig.watch = true;

let serverStatus = 0;
let serverPort = config.dev.portOnlyDev;

webpack(weexConfig, (err, stats) => {
    if (!err) {
        if (serverStatus === 0) {
            serverStatus = 1;
            utils.portIsOccupied(serverPort, (err, port) => {
                if (err == null) {
                    serverStatus = 200;
                    serverPort = port;
                    utils.createServer(config.dev.contentBase, serverPort);
                    utils.copySrcToDist(false);
                    utils.syncFolderEvent(config.dev.host, serverPort, serverPort + 1);
                }
            });
        } else if (serverStatus === 200) {
            utils.copySrcToDist(false);
            utils.syncFolderEvent(config.dev.host, serverPort, serverPort + 1);
        }
    }
});

module.exports = [commonConfig[0], weexConfig];
