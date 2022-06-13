package gbench.sandbox.weihai.devops;

import static gbench.util.io.Output.println;
import static gbench.util.lisp.IRecord.REC;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import gbench.util.io.Output;
import gbench.util.lisp.DFrame;
import gbench.util.lisp.IRecord;
import gbench.whccb.client.DevOpsClient;

public class SystemJunit extends DevOpsClient {

    /**
     * 
     * @param params
     * @return
     */
    public IRecord create(IRecord params) {
        final String api = apiOf("/auth/oa/system/insert");
        return this.post_json(api, params);
    }

    /**
     * 查看系统列表
     * 
     * @return
     */
    public IRecord list() {
        final String api = apiOf("/auth/oa/system/list/map");
        return this.get(api, IRecord.REC());
    }

    /**
     * 
     * @param sysName 系统名 或 缩写
     * @return
     */
    public String sysIdOf(final String sysName) {
        return optOf(sysName).map(e -> e.str("id")).orElse(null);
    }

    /**
     * 
     * @param sysName 系统名 或 缩写
     * @return
     */
    public Optional<IRecord> optOf(final String sysName) {
        final IRecord resp = this.list();
        final DFrame dfm = resp.llS("result").map(IRecord::REC).collect(DFrame.dfmclc);
        return dfm.rowS().filter(e -> sysName.equals(e.str("label")) || sysName.equals(e.str("value").trim()))
                .findFirst();
    }

    @Test
    public void foo() {

        // 创建系统
        final IRecord sys_create_param = REC( //
                "majorUserName", "zhangsan"// 主负责人账号 ？
                , "oid", "ITSM" // 32位的纯数字 ：202111291751105649078
                , "providerName", "上海速邦信息科技有限公司" //
                , "systemCode", "ITSM", //
                "systemName", "IT服务管理平台"); // systemName 必须唯一
        Output.println(this.create(sys_create_param));

        final IRecord resp = this.list();
        final DFrame dfm = resp.llS("result").map(IRecord::REC).collect(DFrame.dfmclc);
        println(dfm);
        // println(sysIdOf("ITSM"));

    }
    
    /**
     * 系统创建
     */
    @Test
    public void foo_create() {

        // 创建系统
        final IRecord sys_create_param = REC( //
                "majorUserName", "zhangsan"// 主负责人账号 ？
                , "oid", "ITSM" // 32位的纯数字 ：202111291751105649078
                , "providerName", "上海速邦信息科技有限公司" //
                , "systemCode", "ITSM", //
                "systemName", "IT服务管理平台"); // systemName 必须唯一
        Output.println(this.create(sys_create_param));

        final IRecord resp = this.list();
        final DFrame dfm = resp.llS("result").map(IRecord::REC).collect(DFrame.dfmclc);
        println(dfm);
        // println(sysIdOf("ITSM"));

    }
    
    /**
     * 查询系统列表
     */
    @Test
    public void foo_list() {
        final IRecord resp = this.list();
        final DFrame dfm = resp.llS("result").map(IRecord::REC).collect(DFrame.dfmclc);
        println(dfm);
    }

}
