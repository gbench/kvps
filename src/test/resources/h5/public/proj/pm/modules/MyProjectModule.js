/**
 * 我的项目的数据模块
 */
const MyProjModule = {

    namespaced: true,

    state: () => {
        return { // 我的项目的页面数据-网页存储
            storage: { // 数据存储
                milestones: [ // 项目id,阶段id,事项id,成果信息
                    {  }
                ] // 成果文档
            },
        } // return
    }, // state

    /**
    * 数据操作的公布方法：actions 只有通过 mutations 才能够修改 store的本地缓存的state 数据。
    */
    mutations: {// DAO

        /**
         * 添加项目信息
         * 
         * @param {*} state 
         * @param {*} projects 载荷
         */
        update_projects(state, projects) {
            state.projects = projects;
        },

    },


    /**
    * 数据操作的异步方法
    */
    actions: {// Service
        /**
         * 添加项目信息
         * 
         * @param {*} { commit } 
         * @param {*} projects 载荷
         */
        update_projects({ commit }, projects) {
            commit('update_projects', projects);
        },

    },// actions

    /**
     * getters
     */
    getters: {

        /**
         * 提取 proj_id 的函数
         * 
         * @param {*} deliverable_proto 成果原型
         * @returns  满足与 deliverable_proto 的成果列表
         */
        deliverables_of: state => deliverable_proto => {

            const predicate = _.cond([
                [_.matches(deliverable_proto), _.constant(true)], // 匹配结果分支
                [_.stubTrue, _.constant(false)] // 默认结果分支
            ]); // 过滤函数

            return state.storage.deliverables.filter(predicate);
        }
    }
}