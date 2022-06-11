/**
 * 数据存储：数据库的本地缓存。
 */
const store = new Vuex.Store({// 数据持久化层

    /**
     * 数据模块
     */
    modules: {
        MyProjModule,
    },

    state: {// 本地缓存数据
        // nothing
    }, // state

    /**
    * 数据操作的公布方法：actions 只有通过 mutations 才能够修改 store的本地缓存的state 数据。
    */
    mutations: {// DAO
        // nothing
    },

    /**
    * 数据操作的异步方法
    */
    actions: {// Service
        // nothing
    },// actions

    getters: {// 数据的共享读取方法
        // nothing
    }
});