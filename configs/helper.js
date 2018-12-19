// Helper functions
const path = require('path');
const ROOT = path.resolve(__dirname, '..');

module.exports = {
    root: (args) => {
        return path.join(ROOT, 'src', args);
    },

    rootNode: (args) => {
        return path.join(ROOT, args);
    },

    resolve: (dir) => {
        return path.join(__dirname, '..', dir)
    },

    getQueryString: (search, name) => {
        let reg = new RegExp("(^|&|\\?)" + name + "=([^&]*)", "i");
        let r = search.match(reg);
        if (r != null) return (r[2]);
        return "";
    },

    getMiddle(string, start, end) {
        if (this.isHave(start) && this.strExists(string, start)) {
            string = string.substring(string.indexOf(start) + start.length);
        } else {
            return "";
        }
        if (this.isHave(end) && this.strExists(string, end)) {
            string = string.substring(0, string.indexOf(end));
        } else {
            return "";
        }
        return string;
    },

    isHave(set) {
        return !!(set !== null && set !== "null" && set !== undefined && set !== "undefined" && set);
    },

    strExists(string, find, lower) {
        string += "";
        find += "";
        if (lower !== true) {
            string = string.toLowerCase();
            find = find.toLowerCase();
        }
        return (string.indexOf(find) !== -1);
    },

    isNullOrUndefined(obj) {
        return typeof obj === "undefined" || obj === null;
    },

    likeArray(obj) {
        return this.isNullOrUndefined(obj) ? false : typeof obj.length === 'number';
    },

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

    each(elements, callback) {
        let i, key;
        if (this.likeArray(elements)) {
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

    getObject(obj, keys) {
        let object = obj;
        if (this.count(obj) > 0 && this.count(keys) > 0) {
            let arr = keys.replace(/,/g, "|").replace(/\./g, "|").split("|");
            this.each(arr, (index, key) => {
                if (typeof object[key] !== "undefined") {
                    object = object[key];
                }
            });
        }
        return object;
    },
};
