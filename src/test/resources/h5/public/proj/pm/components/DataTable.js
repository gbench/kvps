const DataTable = {
    template: `
    <table :class="tblclass">
            <tr>
                <th :class="thclass"
                    v-for="h in heads(data)">{{label(h)}}</th>
            </tr>
            <tr v-for="line,i in data">
                <td :class="tdclass"
                v-for="h in heads(data)">
                    {{line[h]}}
                </td>
            </tr>
    </table>
    `,

    props: {
        data: Array,
        dics: { type: Object, default: () => { } },
        tblclass: { type: String, default: "tblclass" },
        thclass: { type: String, default: "thclass" },
        tdclass: { type: String, default: "tdclass" },
    },

    computed: {

    },

    methods: {

        label(key) {
            let lbl = key;
            if (this.dics) {
                const v = this.dics[key];
                if (v) {
                    lbl = v;
                }
            }

            return lbl;
        },

        /**
         * 提取表头表头数据
         * 
         * @param {*} dd 列表数据
         * @returns 表头数据
         */
        heads(dd) {

            if (!dd || dd.length < 1) {
                return [];
            } else { // 遍历所有的 数据 元素 获取 完整的 键名列表
                return _.reduce(dd.map(e => Object.keys(e)), (acc, cur) => { // 累计元素,当前元素
                    const diffs = _.difference(cur, acc); // 提取差异部分，acc 中所没有的部分
                    return _.concat(acc, diffs);
                });
            }
        },

    }

};