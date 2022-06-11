const MyProject = {

    template: `模板参见templates/myproject`,

    data() {
        return {
            // do nothing
        }; // return 
    },

    /**
     * 页面装载
     */
    mounted() {
        // do nothing
    },

    /**
     * 页面创建完毕
     */
    created() {
        //
    },

    computed: {

        /**
         * 成果列表
         */
        deliverables() {

            const deliverables = this.deliverables_of({}); // 项目的成果列表
            return deliverables.map(this.flat_deliverable);
        },

        /**
         * State 数据 
         */
        ...Vuex.mapState({ // 
            // mapState 状态数据梗概 不会 同步页面数据，需要使用 mapGetters 才可以。 所以这里不用mapState来映射projects
            state_projects: (state) => state.MyProjModule.projects
        }),

        /**
         * Getters 数据
         */
        ...Vuex.mapGetters("MyProjModule", ["projects", "current_proj_index", "deliverables_of",]),

    },

    methods: {

        /**
         * Actions 方法
         */
        ...Vuex.mapActions("MyProjModule", ['set_current_proj_index', "add_deliverable",
            "add_project", "update_project", "update_projects"]),

    }

}