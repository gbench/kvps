/** =========================================================
 *  Key Value Pair (KVP) 的 操作函数
 *  KVP 一般用于 图表数据的构造
 *  该文件 依赖于 loadash 库
 * ============================================================*/

/**
* Value Key pair
 * 
 * @param {*} value 键值
 * @param {*} key 键名
 * @returns 
 */
function vkp(value, key) {
    return { key, value }
}

/**
 * Key Value Pair
 * 
 * @param {*} key 键名
 * @param {*} value  键值
 * @returns 
 */
function kvp(key, value) {
    return { key, value }
}

/**
 * Key Value Pairs 向量化的kvp
 * 
 * @param {*} keys 键名序列,键名向量
 * @param {*} values  键值序列，值向量
 * @returns kvp 的序列
 */
function kvps(keys, values) {
    const toarray = (xs) => !xs
        ? []
        : Array.isArray(xs)
            ? xs
            : (xs + "").trim().split(/[,\s，、]+/ig); // 键名列表;
    const zip2kvps = (ks, vs) => {
        const kk = toarray(ks); // 键名列表
        const vv = toarray(vs);  // 键值列表
        return _.zip(kk, vv).map(kvpa);
    }

    if (keys && values) {
        return zip2kvps(keys, values);
    } else if (keys) { // 只有keys的情况
        const type = typeof keys;
        if (type == "string") {
            ss = keys.split(/[;；。\n]/ig); // 行分组
            const kk = ss.length > 0 ? ss[0] : ["key"];
            const vv = ss.length > 1 ? ss[1] : ["1"]; //  values 默认为1
            return zip2kvps(ss[0], ss[1]);
        } else if (type == "object") {
            const object = keys; // key 是一个对象
            return Object.keys(object).map(k => {
                return { key: k, value: object[k] };
            });
        } else {
            return [];
        }
    } else {
        return [];
    }

}

/**
 * Key Value Pair from an array 小数组的 kvp
 * 
 * @param {*} aa 只要有两个元素的 数组对象 [key,value]
 * @returns 
 */
function kvpa(aa) {
    return { key: aa[0], value: aa[1] }
}

/**
 * 向量-向量 函数 
 * 
 * @param {*} aa 第一向量
 * @param {*} bb 第二向量
 * @param {*} fn (a,b)=>x
 * @returns 
 */
function vec_kvp_fn(aa, bb, fn) {
    const an = aa.length;
    const bn = bb.length;
    const n = Math.max(an, bn);
    const vv = [];
    const kk = [];
    for (var i = 0; i < n; i++) {
        let v = null, a = null, _a = null, b = null, _b = null; //  初始值
        try {
            a = aa[i % an].value;
            b = bb[i % bn].value;
            _a = parseFloat(a);
            _b = parseFloat(b);
        } catch (e) {
            // do nothing
        } // try

        v = fn(_a ? _a : a, _b ? _b : b);
        vv.push(v);
        kk.push(aa[i] && aa[i].key ? aa[i].key : i);
    }
    return vv.map((v, i) => kvp(kk[i], vv[i]));
}

/**
 * 向量-向量 函数 加法
 * @param {*} aa 
 * @param {*} bb  
 * @returns 
 */
function vec_kvp_plus(aa, bb, fn) {
    return vec_kvp_fn(aa, bb, (a, b) => a + b);
}

/**
 * 向量-向量 函数 减法
 * 
 * @param {*} aa 
 * @param {*} bb 
 * @returns 
 */
function vec_kvp_sub(aa, bb, fn) {
    return vec_kvp_fn(aa, bb, (a, b) => a - b);
}

/**
 * 向量-向量 函数 乘法
 * 
 * @param {*} aa 
 * @param {*} bb  
 * @returns 
 */
function vec_kvp_mul(aa, bb) {
    return vec_kvp_fn(aa, bb, (a, b) => a * b);
}

/**
 * 向量-向量 函数 除法
 * 
 * @param {*} aa 
 * @param {*} bb 
 * @returns 
 */
function vec_kvp_div(aa, bb) {
    return vec_kvp_fn(aa, bb, (a, b) => a / b);
}

/**
 * 向量-标量 函数 
 * @param {*} aa 第一向量
 * @param {*} b 第二向量
 * @param {*} fn (a,b)=>x
 * @returns 
 */
function aab_kvp_fn(aa, b, fn) {
    return vec_kvp_fn(aa, [kvp(0, b)], fn);
}

/**
* 向量-标量 函数  加法
* @param {*} aa 
* @param {*} b  
* @returns 
*/
function aab_kvp_plus(aa, b) {
    return aab_kvp_fn(aa, b, (a, b) => a + b);
}

/**
 * 向量-标量 函数  减法
 * 
 * @param {*} aa 
 * @param {*} b 
 * @returns 
 */
function aab_kvp_sub(aa, b, fn) {
    return aab_kvp_fn(aa, b, (a, b) => a - b);
}

/**
 * 向量-标量 函数  乘法
 * 
 * @param {*} aa 
 * @param {*} b  
 * @returns 
 */
function aab_kvp_mul(aa, b) {
    return aab_kvp_fn(aa, b, (a, b) => a * b);
}

/** 
 * 向量-标量 函数  除法
 * 
 * @param {*} aa 
 * @param {*} b
 * @returns 
 */
function aab_kvp_div(aa, b) {
    return aab_kvp_fn(aa, b, (a, b) => a / b);
}

/**
 * 向量-标量 函数 
 * @param {*} a 第一向量
 * @param {*} bb 第二向量
 * @param {*} fn (a,b)=>x
 * @returns 
 */
function abb_kvp_fn(a, bb, fn) {
    const _aa = bb.map((b, i) => kvp(b.key ? b.key : i, a));
    return vec_kvp_fn(_aa, bb, fn);
}

/**
* 标量-向量 函数  加法
* @param {*} a
* @param {*} bb
* @returns 
*/
function abb_kvp_plus(a, bb) {
    return abb_kvp_fn(a, bb, (a, b) => a + b);
}

/**
 * 标量-向量 函数  减法
 * 
 * @param {*} a 
 * @param {*} bb 
 * @returns 
 */
function abb_kvp_sub(a, bb, fn) {
    return abb_kvp_fn(a, bb, (a, b) => a - b);
}

/**
 * 标量-向量 函数   乘法
 * 
 * @param {*} a 
 * @param {*} bb 
 * @returns 
 */
function abb_kvp_mul(a, bb) {
    return abb_kvp_fn(a, bb, (a, b) => a * b);
}

/**
 * 标量-向量 函数   除法
 * 
 * @param {*} a
 * @param {*} bb
 * @returns 
 */
function abb_kvp_div(a, bb) {
    return abb_kvp_fn(a, bb, (a, b) => a / b);
}

/**
 * 把一个文本对象转换成一个IRecord结构对象，即 javascript的对象，类似于javabean
 * @param {*} kvs 键值序列的文本
 * @returns IRecor的数据对象
 */
function REC(kvs) {
    let record = {};
    if (!kvs) {
        return record;
    } else {
        if (typeof kvs == "string") {
            const kk = kvs.split(/[,:;\t\n]+/ig).filter(e => !/^\s*$/ig.exec(e));
            const n = kk.length;
            for (var i = 0; i < n; i += 2) {
                const k = kk[i].trim();
                record[k] = kk[i + 1];
            }// 
        } else { //if
            record = kvs;
        }

        return record;
    }
}

/**
 * 模板填充(fill template)
 * 
 * 位置占位符为 ${keyname}
 * 
 * @param {*} template 文件模板,结构
 * @param {*} context 模板上下文，占位符的符号定义
 * @param {*} keys tpl 中的键名集合
 */
function FT(template, context, keys) {

    let _temlate = template;
    const _keys = !keys ? Object.keys(context) : keys;

    _keys.forEach(k => {
        const v = context[k]; // 提取占位符内容
        const pattern_text = `\\$\\{${k}}`; // 关键词的模式文本 
        const pattern = new RegExp(pattern_text, "g"); // 全局替换掉占位符的规则模式
        _temlate = _temlate.replace(pattern, v); // 全局替换
    });

    return _temlate;
}