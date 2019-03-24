const commonConfig = require('./webpack.common.conf');
const utils = require('./utils');
const config = require('./config');
const webpack = require('webpack');

const weexConfig = commonConfig[1];
weexConfig.watch = true;

let serverStatus = 0;
let serverPort = config.dev.portOnlyDev;
let socketPort = config.dev.portOnlyDev + 1;

webpack(weexConfig, (err, stats) => {
    if (err) {
        console.err('COMPILE ERROR:', err.stack);
    } else {
        if (serverStatus === 0) {
            serverStatus = 1;
            utils.portIsOccupied(serverPort, (err, port) => {
                if (err) throw err;
                utils.portIsOccupied(socketPort, (err, sPort) => {
                    if (err) throw err;
                    serverStatus = 200;
                    serverPort = port;
                    socketPort = sPort;
                    utils.createServer(config.dev.contentBase, serverPort);
                    utils.copySrcToDist();
                    utils.syncFolderEvent(config.dev.host, serverPort, socketPort, true);
                });
            });
        } else if (serverStatus === 200) {
            utils.copySrcToDist();
            utils.syncFolderEvent(config.dev.host, serverPort, socketPort, false);
        }
    }
});

module.exports = [commonConfig[0], weexConfig];
