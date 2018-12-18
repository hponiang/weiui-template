const path = require('path');
const notifier = require('node-notifier');
const config = require('./config');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const packageConfig = require('../package.json');

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
        }
        else {
            return loaders
        }
    };

    // https://vue-loader.vuejs.org/en/configurations/extract-css.html
    return {
        less: generateLoaders('less'),
        sass: generateLoaders('sass', {indentedSyntax: true}),
        scss: generateLoaders('sass'),
        stylus: generateLoaders('stylus'),
        styl: generateLoaders('stylus')
    }
};

// Generate loaders for standalone style files (outside of .vue)
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

const fs = require('fs');
const fsEx = require('fs-extra');
const helper = require('./helper');
const weiuiConfig = require('../weiui.config');
const uuid = require('node-uuid');
const chalk = require('chalk');

let socketAlready = false;
let socketClients = [];

let getQueryString = (search, name) => {
    let reg = new RegExp("(^|&|\\?)" + name + "=([^&]*)", "i");
    let r = search.match(reg);
    if (r != null) return (r[2]);
    return "";
};

/**
 * 复制src其他资源到dist
 */
exports.copySrcToDist = (cover) => {
    let copyJsEvent = (originDir, newDir) => {
        let lists = fs.readdirSync(originDir);
        lists.forEach((item) => {
            let originPath = originDir + "/" + item;
            let newPath = newDir + "/" + item;
            fs.stat(originPath, (err, stats) => {
                if (typeof stats === 'object') {
                    if (stats.isFile()) {
                        if (/(\.(png|jpe?g|gif)$|^((?!font).)*\.svg$)/.test(originPath)) {
                            if (cover || !fs.existsSync(newPath)) {
                                fsEx.copy(originPath, newPath);
                            }
                        }
                    } else if (stats.isDirectory()) {
                        copyJsEvent(originPath, newPath)
                    }
                }
            });
        });
    };
    copyJsEvent(helper.rootNode('src'), helper.rootNode('dist'));
};

/**
 * 生成同步文件
 * @param host
 * @param port
 * @param socketPort
 */
exports.syncFolderEvent = (host, port, socketPort) => {
    let jsonData = weiuiConfig;
    jsonData.socketHost = host ? host : '';
    jsonData.socketPort = socketPort ? socketPort : '';
    //
    let isSocket = !!(host && socketPort);
    if (isSocket) {
        jsonData.homePage = 'http://' + host + ':' + port + '/dist/index.js';
    }
    //
    let copyJsEvent = (originDir, newDir) => {
        let lists = fs.readdirSync(originDir);
        lists.forEach((item) => {
            let originPath = originDir + "/" + item;
            let newPath = newDir + "/" + item;
            fs.stat(originPath, (err, stats) => {
                if (typeof stats === 'object') {
                    if (stats.isFile()) {
                        if (originPath.substr(-7) !== ".web.js" && originPath.substr(-8) !== ".web.map") {
                            fsEx.copy(originPath, newPath);
                        }
                    } else if (stats.isDirectory()) {
                        copyJsEvent(originPath, newPath)
                    }
                }
            });
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
                        fsEx.remove(helper.rootNode(assetsPath), function(err) {
                            if (!err) {
                                fsEx.outputFile(helper.rootNode(assetsPath + '/config.json'), JSON.stringify(jsonData));
                                if (!jsonData.homePage) {
                                    copyJsEvent(helper.rootNode('dist'), helper.rootNode(assetsPath));
                                }
                            }
                        });
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
                        fsEx.remove(helper.rootNode(bundlejsPath), function(err) {
                            if (!err) {
                                fsEx.outputFile(helper.rootNode(bundlejsPath + '/config.json'), JSON.stringify(jsonData));
                                if (!jsonData.homePage) {
                                    copyJsEvent(helper.rootNode('dist'), helper.rootNode(bundlejsPath));
                                }
                            }
                        });
                    }
                });
            });
        }
    });
    //WebSocket
    if (isSocket) {
        if (socketAlready === false) {
            socketAlready = true;
            let WebSocketServer = require('ws').Server,
                wss = new WebSocketServer({ port: socketPort });
            wss.on('connection', (ws, info) => {
                let deviceId = uuid.v4();
                socketClients.push({ deviceId, ws });
                //console.log("[WebSocketServer]", `Your device ${deviceId} had been connected to the socket server.`);
                ws.on('close', () => {
                    for (let i = 0, len = socketClients.length; i < len; i++) {
                        if (socketClients[i].deviceId === deviceId) {
                            socketClients.splice(i, 1);
                            //console.log("[WebSocketServer]", `Your device ${deviceId} had left this socket.`);
                            break;
                        }
                    }
                });
                ws.on('error', (e) => {
                    //console.log("[WebSocketServer]", 'Socket error:' + e);
                });
                //
                let mode = getQueryString(info.url, "mode");
                switch (mode) {
                    case "initialize":
                        ws.send('HOMEPAGE:' + jsonData.homePage);
                        break;

                    case "back":
                        ws.send('HOMEPAGEBACK:' + jsonData.homePage);
                        break;
                }
            });
        } else {
            socketClients.map((client) => {
                (client.ws.readyState !== 2) && client.ws.send('RELOADPAGE')
            });
        }
        notifier.notify({
            title: 'WiFi真机同步',
            message: jsonData.socketHost + ':' + jsonData.socketPort,
            contentImage: path.join(__dirname, 'logo.png')
        });
    }else{
        notifier.notify({
            title: 'Weex Weiui',
            message: "Build successful",
            contentImage: path.join(__dirname, 'logo.png')
        });
    }
};

const http = require('http');

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
