const path = require('path');
const notifier = require('node-notifier');
const packageConfig = require('../package.json');
const fs = require('fs');
const fsEx = require('fs-extra');
const helper = require('./helper');
const weiuiConfig = require('../weiui.config');
const uuid = require('node-uuid');
const http = require('http');
const net = require('net');
const crypto = require('crypto');
const chalk = require('chalk');

let socketAlready = false;
let socketTimeout = null;
let socketClients = [];
let fileMd5Lists = {};

exports.cssLoaders = function (options) {
    options = options || {};
    const cssLoader = {
        loader: 'css-loader',
        options: {
            sourceMap: options.sourceMap
        }
    };

    const postcssLoader = {
        loader: 'postcss-loader',
        options: {
            sourceMap: options.sourceMap
        }
    };

    // generate loader string to be used with extract text plugin
    const generateLoaders = (loader, loaderOptions) => {
        let loaders = options.useVue ? [cssLoader] : [];
        if (options.usePostCSS) {
            loaders.push(postcssLoader)
        }
        if (loader) {
            loaders.push({
                loader: loader + '-loader',
                options: Object.assign({}, loaderOptions, {
                    sourceMap: options.sourceMap
                })
            })
        }
        if (options.useVue) {
            return ['vue-style-loader'].concat(loaders)
        } else {
            return loaders
        }
    };

    return {
        less: generateLoaders('less'),
        sass: generateLoaders('sass', {indentedSyntax: true}),
        scss: generateLoaders('sass'),
        stylus: generateLoaders('stylus'),
        styl: generateLoaders('stylus')
    }
};

exports.styleLoaders = function (options) {
    const output = [];
    const loaders = exports.cssLoaders(options);

    for (const extension in loaders) {
        const loader = loaders[extension];
        output.push({
            test: new RegExp('\\.' + extension + '$'),
            use: loader
        })
    }
    return output
};

exports.createNotifierCallback = () => {
    return (severity, errors) => {
        if (severity !== 'error') return;

        const error = errors[0];
        const filename = error.file && error.file.split('!').pop();

        notifier.notify({
            title: packageConfig.name,
            message: severity + ': ' + error.name,
            subtitle: filename || '',
            icon: path.join(__dirname, 'logo.png')
        })
    }
};

/**
 * 替换iOS plist文件项目内容
 * @param path
 * @param key
 * @param value
 */
exports.replaceDictString = (path, key, value) => {
    if (!fs.existsSync(path)) {
        return;
    }
    let content = fs.readFileSync(path, 'utf8');
    let matchs = content.match(/<dict>(.*?)<\/dict>/gs);
    if (matchs) {
        matchs.forEach(function (oldText) {
            oldText = oldText.substring(oldText.lastIndexOf('<dict>'), oldText.length);
            if (helper.strExists(oldText, '<string>' + key + '</string>', true)) {
                let searchValue = helper.getMiddle(oldText, '<array>', '</array>');
                if (searchValue) {
                    searchValue = '<array>' + searchValue + '</array>';
                    let stringValue = '<string>' + helper.getMiddle(searchValue, '<string>', '</string>') + '</string>';
                    let replaceValue = searchValue.replace(new RegExp(stringValue, "g"), '<string>' + value + '</string>');
                    let newText = oldText.replace(new RegExp(searchValue, "g"), replaceValue);
                    let result = fs.readFileSync(path, 'utf8').replace(new RegExp(oldText, "g"), newText);
                    if (result) {
                        fs.writeFileSync(path, result, 'utf8');
                    }
                }
            }
        });
    }
};

/**
 * 复制src其他资源到dist
 */
exports.copySrcToDist = () => {
    let _copyEvent = (originDir, newDir) => {
        let lists = fs.readdirSync(originDir);
        lists.forEach((item) => {
            let originPath = originDir + "/" + item;
            let newPath = newDir + "/" + item;
            fs.stat(originPath, (err, stats) => {
                if (typeof stats === 'object') {
                    if (stats.isFile()) {
                        if (/(\.(png|jpe?g|gif)$|^((?!font).)*\.svg$)/.test(originPath)) {
                            if (!fs.existsSync(newPath)) {
                                fsEx.copy(originPath, newPath);
                            }
                        }
                    } else if (stats.isDirectory()) {
                        _copyEvent(originPath, newPath)
                    }
                }
            });
        });
    };
    _copyEvent(helper.rootNode('src'), helper.rootNode('dist'));
};

/**
 * 复制文件，md5原文件是否已复制
 * @param originPath
 * @param newPath
 * @param callback
 */
exports.copyFileMd5 = (originPath, newPath, callback) => {
    let stream = fs.createReadStream(originPath);
    let md5sum = crypto.createHash('md5');
    stream.on('data', (chunk) => {
        md5sum.update(chunk);
    });
    stream.on('end', () => {
        let str = md5sum.digest("hex").toUpperCase();
        if (fileMd5Lists[newPath] !== str) {
            fileMd5Lists[newPath] = str;
            fsEx.copy(originPath, newPath, callback);
        }
    });
};

/**
 * 生成同步文件
 * @param host
 * @param port
 * @param socketPort
 * @param removeBundlejs
 */
exports.syncFolderEvent = (host, port, socketPort, removeBundlejs) => {
    let jsonData = weiuiConfig;
    jsonData.socketHost = host ? host : '';
    jsonData.socketPort = socketPort ? socketPort : '';
    jsonData.wxpay.appid = helper.getObject(jsonData, 'wxpay.appid');
    //
    let isSocket = !!(host && socketPort);
    let hostUrl = 'http://' + host + ':' + port + '/dist';
    //
    let random = Math.random();
    let deviceIds = {};
    //
    let copyJsEvent = (originDir, newDir) => {
        let lists = fs.readdirSync(originDir);
        lists.forEach((item) => {
            let originPath = originDir + "/" + item;
            let newPath = newDir + "/" + item;
            if (!/(\.web\.js|\.web\.map|\.DS_Store|__MACOSX)$/.exec(originPath)) {
                fs.stat(originPath, (err, stats) => {
                    if (typeof stats === 'object') {
                        if (stats.isFile()) {
                            this.copyFileMd5(originPath, newPath, (err) => {
                                //!err && console.log(newPath);
                                if (!err && socketAlready) {
                                    socketClients.map((client) => {
                                        let deviceKey = client.deviceId + hostUrl + '/' + item;
                                        if (client.ws.readyState !== 2 && deviceIds[deviceKey] !== random) {
                                            deviceIds[deviceKey] = random;
                                            client.ws.send('RELOADPAGE:' + hostUrl + '/' + item);
                                        }
                                    });
                                }
                            });
                        } else if (stats.isDirectory()) {
                            copyJsEvent(originPath, newPath)
                        }
                    }
                });
            }
        });
    };
    //syncFile Android
    fs.stat(helper.rootNode('platforms/android'), (err, stats) => {
        if (typeof stats === 'object' && stats.isDirectory()) {
            let androidLists = fs.readdirSync(helper.rootNode('platforms/android'));
            androidLists.forEach((item) => {
                let path = 'platforms/android/' + item + '/app/src/main';
                let assetsPath = path + '/assets/weiui';
                fs.stat(helper.rootNode(path), (err, stats) => {
                    if (typeof stats === 'object' && stats.isDirectory()) {
                        if (removeBundlejs) {
                            fsEx.remove(helper.rootNode(assetsPath), function (err) {
                                if (err) throw err;
                                fsEx.outputFile(helper.rootNode(assetsPath + '/config.json'), JSON.stringify(jsonData));
                                copyJsEvent(helper.rootNode('dist'), helper.rootNode(assetsPath));
                            });
                        }else{
                            copyJsEvent(helper.rootNode('dist'), helper.rootNode(assetsPath));
                        }
                    }
                });
            });
        }
    });
    //syncFile iOS
    fs.stat(helper.rootNode('platforms/ios'), (err, stats) => {
        if (typeof stats === 'object' && stats.isDirectory()) {
            let iosLists = fs.readdirSync(helper.rootNode('platforms/ios'));
            iosLists.forEach((item) => {
                let path = 'platforms/ios/' + item;
                let bundlejsPath = path + '/bundlejs/weiui';
                fs.stat(helper.rootNode(path), (err, stats) => {
                    if (typeof stats === 'object' && stats.isDirectory()) {
                        if (removeBundlejs) {
                            fsEx.remove(helper.rootNode(bundlejsPath), function (err) {
                                if (err) throw err;
                                fsEx.outputFile(helper.rootNode(bundlejsPath + '/config.json'), JSON.stringify(jsonData));
                                copyJsEvent(helper.rootNode('dist'), helper.rootNode(bundlejsPath));
                            });
                        }else{
                            copyJsEvent(helper.rootNode('dist'), helper.rootNode(bundlejsPath));
                        }
                    }
                });
                let plistPath = 'platforms/ios/' + item + '/WeexWeiui/Info.plist';
                this.replaceDictString(helper.rootNode(plistPath), 'weiuiAppWxappid', jsonData.wxpay.appid);
            });
        }
    });
    //WebSocket
    if (isSocket) {
        if (socketAlready === false) {
            socketAlready = true;
            let WebSocketServer = require('ws').Server,
                wss = new WebSocketServer({port: socketPort});
            wss.on('connection', (ws, info) => {
                let deviceId = uuid.v4();
                socketClients.push({deviceId, ws});
                ws.on('close', () => {
                    for (let i = 0, len = socketClients.length; i < len; i++) {
                        if (socketClients[i].deviceId === deviceId) {
                            socketClients.splice(i, 1);
                            break;
                        }
                    }
                });
                //
                let mode = helper.getQueryString(info.url, "mode");
                switch (mode) {
                    case "initialize":
                        ws.send('HOMEPAGE:' + hostUrl + '/index.js');
                        break;

                    case "back":
                        ws.send('HOMEPAGEBACK:' + hostUrl + '/index.js');
                        break;

                    case "reconnect":
                        //ws.send('REFRESH');
                        break;
                }
            });
        }
        notifier.notify({
            title: 'WiFi真机同步',
            message: jsonData.socketHost + ':' + jsonData.socketPort,
            contentImage: path.join(__dirname, 'logo.png')
        });
        socketTimeout && clearInterval(socketTimeout);
        socketTimeout = setTimeout(() => {
            let msg = '    ';
            msg+= chalk.bgBlue.bold.black(`【WiFI真机同步】`);
            msg+= chalk.bgBlue.black(`IP地址: `);
            msg+= chalk.bgBlue.bold.black.underline(`${jsonData.socketHost}`);
            msg+= chalk.bgBlue.black(`、端口号: `);
            msg+= chalk.bgBlue.bold.black.underline(`${jsonData.socketPort}`);
            console.log(); console.log(msg); console.log();
        }, 1800);
    } else {
        notifier.notify({
            title: 'Weex Weiui',
            message: "Build successful",
            contentImage: path.join(__dirname, 'logo.png')
        });
    }
};

/**
 * 创建访问服务
 * @param contentBase
 * @param port
 */
exports.createServer = (contentBase, port) => {
    http.createServer(function (req, res) {
        let url = req.url;
        let file = contentBase + url;
        fs.readFile(file, function (err, data) {
            if (err) {
                fs.readFile(contentBase + '/statics/js/404.js', function (err, data) {
                    if (err) {
                        res.writeHeader(404, {
                            'content-type': 'text/html;charset="utf-8"'
                        });
                        res.write('<h1>404错误</h1><p>你要找的页面不存在</p>');
                        res.end();
                    } else {
                        res.writeHeader(200, {
                            'content-type': 'text/plain; charset=utf-8'
                        });
                        res.write(data);
                        res.end();
                    }
                });
                return;
            }
            res.writeHeader(200, {
                'content-type': 'text/plain; charset=utf-8'
            });
            res.write(data);
            res.end();
        });
    }).listen(port);
};

/**
 * 检测端口是否被占用
 * @param port
 * @param callback
 */
exports.portIsOccupied = function (port, callback = (err, port) => {}) {
    const server = net.createServer().listen(port);
    server.on('listening', () => {
        server.close();
        callback(null, port);
    });
    server.on('error', (err) => {
        if (err.code === 'EADDRINUSE') {
            this.portIsOccupied(port + 1, callback);
        } else {
            callback(err)
        }
    });
};