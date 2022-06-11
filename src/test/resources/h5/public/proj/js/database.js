let host = "localhost:8089" // 本地的webapck的
const jdbc_url = `http://${host}/widget/api/baas/jasminedata/jdbc`; // yichem365 上的 imtdata 的数据库连接url
const jdbc_config = { dbhost: "localhost", dbport: "3306", database: "db1", user: "root", password: "123456" }; // 本地的webapck的
const jasminedata_file_path = `http://${host}/widget/api/baas/jasminedata/file/read?path`; // 在线图片文件读取接口
const jasminedata_api = `http://${host}/widget/api/`; // jasminedata_api 接口根路径
const mallim_api_url = `/mall/api`; // 在线图片文件读取接口

/**
* 数据库请求 
* @param {*} sql sql 语句
* @param {*} url 数据库连接url
* @param {*} dbconfig 数据库连接配置,null 表示使用默认数据库配置
*/
function sqlquery(sql, url, dbconfig) {
    //console.log(sql);
    return new Promise((reslove, reject) => {
        $.ajax({
            url: url,
            data: { sql: sql },
            headers: !dbconfig ? jdbc_config : dbconfig,// 在请求头中传递
            method: "post",
            success: reslove,
            failure: reject
        })
    });
}// sqlquery

/**
* @param {*} sql sql 语句
* @param {*} dbconfig 数据库的配置，比如：{ dbhost: "localhost", dbport: "3306", database: "db1", user: "root", password: "123456" }
*/
function imt_query(sql, dbconfig) {
    return sqlquery(sql, jdbc_url, dbconfig);
}

/**
 * 生成一个jdbc对象
 * @param {*} url 数据库连接url
 * @param {*} dbconfig 数据库的配置，比如：{ dbhost: "localhost", dbport: "3306", database: "db1", user: "root", password: "123456" }
 */
function jdbc(url, dbconfig) {
    return sql => sqlquery(sql, url, dbconfig);
}

/**
 * 生成一个jdbc对象
 * @param {*} dbconfig 数据库的配置，比如：{ dbhost: "localhost", dbport: "3306", database: "db1", user: "root", password: "123456" }
 */
function jdbc(dbconfig) {
    return sql => sqlquery(sql, jdbc_url, dbconfig);
}

/**
 * 生成一个Monad Promise 数据,monad 就像一颗子弹，而生成moand的函数则负责对monad进行上膛,开动扳机。
 * Monad 就像一个炸弹，f 式把多个炸弹串联的方式。需要一个外界事件，如 detonate 就可以把monad个引爆了 
 * @param {*} e 状态数据，装入炸弹的火药。
 */
const Monad = e => {

    const m = { // 一定要写成let m  确保m 的作用阈限定于  lambda 表达式范围，否则就会 定义成全局，后果不堪设想
        bind: f => { m.ff.push(f); return m; },// 绑定一个函数到函数集合，这就像穿衣服一样，把m一层层的包裹起来
        ff: [],// 绑定函数的记录器,挂载到 m上的函数集合。第一元素最先调用，
        stateData: () => e //monad 中的状态数据值,对于异步函数，这个状态数据无效，他是在异步响应结果里获取结果状态数据的。
    }// m

    return m;
};

/**
 * 引爆m0的回调函数,开启链式反应,这有点像 Array的reduce
 * @param {*} r 引爆m0说使用的引物
 * @param {*} m0  待引爆的的monad
 */
const detonate = r => m0 => {
    const f = m0.ff[0];// 提取第一个绑定函数
    const rff = m0.ff.slice(1);// 提取剩余的绑定函数,ff 代表f的数组.rff 表示right ff 或是 resting ff,数组右侧部分的意思。
    const isfunc = fn => Object.prototype.toString.call(fn) === '[object Function]';// 判断fn是否是一个函数对象

    if (f && isfunc(f)) {// 仅当f是函数的时候给予 进行函数回调
        // 创建新的绑定函数,这里为绑定函数传递参数。非常重要，是理解Monad实现的关键。用过回调f来触发下一个Monda而不是由bind直接触发。
        // 需要注意的是 f 触发的是 m0 即对m0中的参数进行回调，而不是对m1,m1的回调触发需要绑定在m1上的函数来给触发. 第一次回调
        const primer = !r ? m0.stateData() : r;  // 制作引物
        const m1 = f(primer);// m1,表示m的升级版的意思.,注意 这里忽略m 的状态数据，因为这个一个sqlexecute异步函数,所以状态数据是异步传递的。
        //把剩余的函数依次绑定给新创建的函数m1
        if (rff && rff.length > 0 && m1 && m1.bind) rff.forEach(_f => m1.bind(_f));// 绑定剩余,或者叫做绑定剩余的传递

        // 仅当m1中存在状态数据的的时候,对于sqlquery 这样的本身就是回调函数的接口，进行自我回调的。
        // 此处的自我回调用以保证m1中的函数f可以在非promise，这样的异步函数的自我回调的情况下,依旧可以持续触发下一次的徽标
        // 因为异步函数是CPS (Continuation passing style), 是参数 调用 函数的形式。这里  m1.ff[0](m1.stateData() 也是
        // 手动模拟这种机制。 CPS 就是把 参数调用函数，与一般的函数调用参数 刚好反过来
        const loop = m => { // 采用loop 模拟 for 循环，for  循环会导致对象的属性 赋值失败的情况。
            // monad的链式回调传递的时机/条件：即 当且仅当m.stateData() 有效才给予进行CPS回调
            if (m && m.stateData && m.stateData() && m.ff[0] && isfunc(m.ff[0])) { // stateData 无标表明这是需要异步函数给予的徽标，不需要loop
                let _m = m.ff[0](m.stateData());// 用m.stateData来作为参数数据反调使用m.ff[0],因为此为同步函数，注意同步函数与异步函数在反调用的区别
                if (!_m) return; // 结果无效给予终止递归循环
                m.ff.slice(1).forEach(_f => _m.bind(_f)); // 加载剩余的绑定函数
                loop(_m); // 递归调用
            }//if 同步monadCPS，返回掉
        };// loop

        // 回调传递 ：同步时机 stateData 有效，异步时机：stateData 无效。
        loop(m1); // 接连引爆的递归循环,同步函数的参数调用函数(m1:上的绑定集合函数)
    }//if(f) f 合法

}

/**
 * 生成一个jdbc对象: 把 monad  这颗子弹上膛(创建一个空monad对象),开动扳机,既异步响应,所谓异步响应就是调用monad上的绑定函数ff[0],
 * 并开启链式反应.ff函数会以除掉第一个函数(第一个函数用于本次回调:参数调用函数)后,顺次在后续的新生成的monad在再依次循环的 向后传递，直至完毕
 * 对于 异常结果 回调函数 传递一个 空数组[],并 带有一个 _error 名称的异常信息值的属性(其)
 * 
 * @param {*} handler 从jdbc中提取结果的函数。 参数回填为 sqlexecute的响应返回结果
 * @param {*} jdbc_url url,jdbc 的请求结果
 * @param {*} sql 待执行的sql 语句 
 */
const jdbc2monad = handler => jdbc_url => sql => {
    const sqlexecute = jdbc(jdbc_url)// jdbc的计算函数
    // monad 的初始值,其实这里把Monad作为一个function 容器ff 来记录bind到的函数,并通过sqlexecute的回调机制来依次触发循环调用
    const m0 = Monad();// 子弹上膛,需要注意异步调用的Monad一定要空值(null)构造，以保证stateData无效，这样异步的回调函数就会识别它并执行/CPS monad上的函数绑定集合

    // 需要注意任何一个jdbc2monad 都是从一个异步回调,参数调用函数(即CPS结构), 而开始的.没有这个异步回调的触发，
    // monad 就不会开启链式反应，它就只是一个函数集合而已。根本不会做任何的数据计算，也不会返回任何有意义的数据。这就是jdbc2monad的奥秘
    // monad 的异步回调的传递机制 是 执行异步函数，即在数据返回时，为m中ff的中的各个回调函数f装填实参,开启回调序列。 
    // 即通过参数调用函数的CPS机制的异步实现。
    sqlexecute(sql).then(e => {// e 异步函数sqlexecute 的响应结果 
        if (!e.error) {// 没有错误发生
            const r = handler(e);// 计算结果,从异步函数中提取返回结果
            detonate(r)(m0);// 用r 引爆m0,触发m0 回调
        } else { // sqlexecute的在执行中存在错误
            console.log("sql执行中出现异常", e)
            const primer = []; // 异常的引物为空数组
            primer._error = e.error; // 空数组带有一个错误标记函数
            detonate(primer)(m0);// 使用空列表 引爆m0,触发m0 回调
        }//if (!e.error)
    });// sqlexecute

    return m0; // 制作一个炸弹，等着回调函数过来引爆,炸弹的威力在于 可以通过m0.bind(()=>m1)其他炸弹，形成链式炸弹。威力无穷呀
}

/**
 * 生成一个jdbc对象:回调函数只返回一项
 * @param {&} url url
 * @param {*} sql sql 语句 
 */
const m_jdbc2one = url => sql => {
    return jdbc2monad(e => e.result && e.result.length > 0 ? e.result[0] : null)(url)(sql);// 只提取一条记录
}

/**
 * 生成一个jdbc对象:回调函数只返回一项
 * @param {*} url url
 * @param {*} sql sql 语句 
 */
const m_jdbc2many = url => sql => {
    return jdbc2monad(e => e.result)(url)(sql);// 返回多个记录
}

/**
 * 生成一个jdbc对象:回调函数只返回一项
 * @param {*} sql sql 语句 
 */
const m_jdbc2one2 = sql => {
    return m_jdbc2one(null)(sql);
}

/**
 * 生成一个jdbc对象:回调函数只返回一项
 * @param {*} sql sql 语句 
 */
const m_jdbc2many2 = sql => {
    return m_jdbc2many(null)(sql);
}