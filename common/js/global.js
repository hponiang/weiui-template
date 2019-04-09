let global = {

    isNullOrUndefined(obj) {
        return typeof obj === "undefined" || obj === null;
    },

    isFunction(obj) {
        return global.isNullOrUndefined(obj) ? false : typeof obj === "function"
    },

    isObject(obj) {
        return global.isNullOrUndefined(obj) ? false : typeof obj === "object";
    },

    likeArray(obj) {
        return global.isNullOrUndefined(obj) ? false : typeof obj.length === 'number';
    },

    isJson(obj) {
        return global.isObject(obj) && !global.likeArray(obj);
    },

    getObject(obj, keys) {
        let object = obj;
        if (global.count(obj) === 0 || global.count(keys) === 0) {
            return "";
        }
        let arr = keys.replace(/,/g, "|").replace(/\./g, "|").split("|");
        global.each(arr, (index, key) => {
            object = typeof object[key] === "undefined" ? "" : object[key];
        });
        return object;
    },

    /**
     * 遍历数组、对象
     * @param elements
     * @param callback
     * @returns {*}
     */
    each(elements, callback) {
        let i, key;
        if (global.likeArray(elements)) {
            if (typeof elements.length === "number") {
                for (i = 0; i < elements.length; i++) {
                    if (callback.call(elements[i], i, elements[i]) === false) return elements
                }
            }
        } else {
            for (key in elements) {
                if (!elements.hasOwnProperty(key)) continue;
                if (callback.call(elements[key], key, elements[key]) === false) return elements
            }
        }

        return elements
    },

    /**
     * 获取数组最后一个值
     * @param array
     * @returns {*}
     */
    last(array) {
        let str = false;
        if (typeof array === 'object' && array.length > 0) {
            str = array[array.length - 1];
        }
        return str;
    },

    /**
     * 删除数组最后一个值
     * @param array
     * @returns {Array}
     */
    delLast(array) {
        let newArray = [];
        if (typeof array === 'object' && array.length > 0) {
            global.each(array, (index, item) => {
                if (index < array.length - 1) {
                    newArray.push(item);
                }
            });
        }
        return newArray;
    },


    /**
     * 字符串是否包含
     * @param string
     * @param find
     * @returns {boolean}
     */
    strExists(string, find) {
        string += "";
        find += "";
        return (string.indexOf(find) !== -1);
    },

    /**
     * 字符串是否左边包含
     * @param string
     * @param find
     * @returns {boolean}
     */
    leftExists(string, find) {
        string += "";
        find += "";
        return (string.substring(0, find.length) === find);
    },

    /**
     * 字符串是否右边包含
     * @param string
     * @param find
     * @returns {boolean}
     */
    rightExists(string, find) {
        string += "";
        find += "";
        return (string.substring(string.length - find.length) === find);
    },

    /**
     * 取字符串中间
     * @param string
     * @param start
     * @param end
     * @returns {*}
     */
    getMiddle(string, start, end) {
        string += "";
        if (global.ishave(start) && global.strExists(string, start)) {
            string = string.substring(string.indexOf(start) + start.length);
        }
        if (global.ishave(end) && global.strExists(string, end)) {
            string = string.substring(0, string.indexOf(end));
        }
        return string;
    },

    /**
     * 截取字符串
     * @param string
     * @param start
     * @param end
     * @returns {string}
     */
    subString(string, start, end) {
        string += "";
        if (!global.ishave(end)) {
            end = string.length;
        }
        return string.substring(start, end);
    },

    /**
     * 随机字符
     * @param len
     * @returns {string}
     */
    randomString(len) {
        len = len || 32;
        let $chars = 'ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678oOLl9gqVvUuI1';
        let maxPos = $chars.length;
        let pwd = '';
        for (let i = 0; i < len; i++) {
            pwd += $chars.charAt(Math.floor(Math.random() * maxPos));
        }
        return pwd;
    },

    /**
     * 判断是否有
     * @param set
     * @returns {boolean}
     */
    ishave(set) {
        return !!(set !== null && set !== "null" && set !== undefined && set !== "undefined" && set);
    },

    /**
     * 补零
     * @param str
     * @param length
     * @param after
     * @returns {*}
     */
    zeroFill(str, length, after) {
        str += "";
        if (str.length >= length) {
            return str;
        }
        let _str = '', _ret = '';
        for (let i = 0; i < length; i++) {
            _str += '0';
        }
        if (after || typeof after === 'undefined') {
            _ret = (_str + "" + str).substr(length * -1);
        } else {
            _ret = (str + "" + _str).substr(0, length);
        }
        return _ret;
    },

    /**
     * 时间戳转时间格式
     * @param format
     * @param v
     * @returns {string}
     */
    formatDate(format, v) {
        if (format === '') {
            format = 'Y-m-d H:i:s';
        }
        if (typeof v === 'undefined') {
            v = new Date().getTime();
        } else if (/^(-)?\d{1,10}$/.test(v)) {
            v = v * 1000;
        } else if (/^(-)?\d{1,13}$/.test(v)) {
            v = v * 1000;
        } else if (/^(-)?\d{1,14}$/.test(v)) {
            v = v * 100;
        } else if (/^(-)?\d{1,15}$/.test(v)) {
            v = v * 10;
        } else if (/^(-)?\d{1,16}$/.test(v)) {
            v = v * 1;
        } else {
            return v;
        }
        let dateObj = new Date(v);
        if (parseInt(dateObj.getFullYear()) + "" === "NaN") {
            return v;
        }
        //
        format = format.replace(/Y/g, dateObj.getFullYear());
        format = format.replace(/m/g, global.zeroFill(dateObj.getMonth() + 1, 2));
        format = format.replace(/d/g, global.zeroFill(dateObj.getDate(), 2));
        format = format.replace(/H/g, global.zeroFill(dateObj.getHours(), 2));
        format = format.replace(/i/g, global.zeroFill(dateObj.getMinutes(), 2));
        format = format.replace(/s/g, global.zeroFill(dateObj.getSeconds(), 2));
        return format;
    },

    /**
     * 是否日期格式
     * @returns {boolean}
     */
    isDate(string) {
        let reg = /^(\d{4})-(\d{2})-(\d{2})$/;
        let str = string + "";
        if (str === "") return false;
        return !(!reg.test(str) && RegExp.$2 <= 12 && RegExp.$3 <= 31);
    },

    /**
     * 检测手机号码格式
     * @param str
     * @returns {boolean}
     */
    isMobile(str) {
        return /^1(3|4|5|7|8)\d{9}$/.test(str);
    },

    /**
     * 手机号码中间换成****
     * @param phone
     * @returns {string}
     */
    formatMobile(phone) {
        if (global.count(phone) === 0) {
            return "";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length - 4);
    },

    /**
     * 克隆对象
     * @param myObj
     * @returns {*}
     */
    clone(myObj) {
        if (typeof(myObj) !== 'object') return myObj;
        if (myObj === null) return myObj;
        //
        if (global.likeArray(myObj)) {
            let [...myNewObj] = myObj;
            return myNewObj;
        } else {
            let {...myNewObj} = myObj;
            return myNewObj;
        }
    },

    /**
     * 统计数组或对象长度
     * @param obj
     * @returns {number}
     */
    count(obj) {
        try {
            if (typeof obj === "undefined") {
                return 0;
            }
            if (typeof obj === "number") {
                obj+= "";
            }
            if (typeof obj.length === 'number') {
                return obj.length;
            } else {
                let i = 0, key;
                for (key in obj) {
                    i++;
                }
                return i;
            }
        }catch (e) {
            return 0;
        }
    },

    /**
     * 相当于 intval
     * @param str
     * @param fixed
     * @returns {number}
     */
    runNum(str, fixed) {
        let _s = Number(str);
        if (_s + "" === "NaN") {
            _s = 0;
        }
        if (/^[0-9]*[1-9][0-9]*$/.test(fixed)) {
            _s = _s.toFixed(fixed);
            let rs = _s.indexOf('.');
            if (rs < 0) {
                _s += ".";
                for (let i = 0; i < fixed; i++) {
                    _s += "0";
                }
            }
        }
        return _s;
    },

    /**
     * 秒转化为天小时分秒字符串
     * @param value
     * @returns {string}
     */
    formatSeconds(value) {
        let theTime = parseInt(value);  // 秒
        let theTime1 = 0;               // 分
        let theTime2 = 0;               // 小时
        if (theTime > 60) {
            theTime1 = parseInt(theTime / 60);
            theTime = parseInt(theTime % 60);
            if (theTime1 > 60) {
                theTime2 = parseInt(theTime1 / 60);
                theTime1 = parseInt(theTime1 % 60);
            }
        }
        let result = parseInt(theTime) + "秒";
        if (theTime1 > 0) {
            result = parseInt(theTime1) + "分" + result;
        }
        if (theTime2 > 0) {
            result = parseInt(theTime2) + "小时" + result;
        }
        return result;
    },

    /**
     * 将一个 JSON 字符串转换为对象（已try）
     * @param str
     * @param defaultVal
     * @returns {*}
     */
    jsonParse(str, defaultVal) {
        try{
            return JSON.parse(str);
        }catch (e) {
            return defaultVal ? defaultVal : {};
        }
    },

    /**
     * 将 JavaScript 值转换为 JSON 字符串（已try）
     * @param json
     * @param defaultVal
     * @returns {string}
     */
    jsonStringify(json, defaultVal) {
        try{
            return JSON.stringify(json);
        }catch (e) {
            return defaultVal ? defaultVal : "";
        }
    },

    /**
     * 去除数组中的非数字项
     * @param value
     * @returns {Array}
     */
    removerNumberNaN(...value) {
        let array = [];
        value.forEach((ele) => {
            if (!isNaN(Number(ele))) {
                array.push(ele);
            }
        });
        return array;
    },

    /**
     * Math.max 过滤NaN
     * @param value
     * @returns {number}
     */
    runMax(...value) {
        return Math.max(...global.removerNumberNaN(...value));
    },

    /**
     * Math.min 过滤NaN
     * @param value
     * @returns {number}
     */
    runMin(...value) {
        return Math.min(...global.removerNumberNaN(...value));
    },

    /**
     * 链接字符串
     * @param value 第一个参数为连接符
     * @returns {string}
     */
    stringConnect(...value) {
        let s = null;
        let text = "";
        value.forEach((val) => {
            if (s === null) {
                s = val;
            }else if (val){
                if (val && text) text+= s;
                text+= val;
            }
        });
        return text;
    },

    /**
     * 字节转换
     * @param bytes
     * @returns {string}
     */
    bytesToSize(bytes) {
        if (bytes === 0) return '0 B';
        let k = 1024;
        let sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
        let i = Math.floor(Math.log(bytes) / Math.log(k));
        if (typeof sizes[i] === "undefined") {
            return '0 B';
        }
        return global.runNum((bytes / Math.pow(k, i)), 2) + ' ' + sizes[i];
    },
};

module.exports = global;
