package gbench.sandbox.weihai.devops;

import static gbench.util.io.Output.println;
import static gbench.util.lisp.IRecord.REC;
import static java.util.Arrays.asList;

import org.junit.jupiter.api.Test;

import gbench.util.io.Output;
import gbench.util.lisp.IRecord;
import gbench.whccb.client.DevOpsClient;

public class ProjectJunit extends DevOpsClient {

    /**
     * 项目创建
     * 
     * @param params
     * @return
     */
    public IRecord create(final IRecord params) {
        final String api = apiOf("/auth/oa/project/insert");
        return this.post_json(api, params);
    }
    
    /**
     * 项目更新
     * 
     * @param params
     * @return
     */
    public IRecord update(final IRecord params) {
        final String api = apiOf("/auth/oa/project/update");
        return this.post_json(api, params);
    }

    @Test
    public void foo_create() {
        SystemJunit system = new SystemJunit();
        Output.println(system.optOf("ITSM"));
        final IRecord proj_param = REC( //
                "title", "ITSM-PROJ001", //
                "projectDevType", "0", //
                "hostSystemOid", "ITSM",  // 需要与 系统Id相一致。
                "projectUserName", "zhangsan", // 需要与系统负责人需要与项目负责人。
                "describe", "ITSM-PROJ001", //
                "oid", "202206071429314753290", //
                "processId", "202206071429314753290", //
                // "projectStatus", "0", //
                "approvalNodes", asList(REC("activityName", "项目经理", "createDate", "2022-05-16 15:08:24.182",
                        "createUserName", "项目负责人", "actionName", "项目经理", "msg", "项目负责人") //
                ) //
                , "startDate", "2022-06-07", "endDate", "2022-06-31"//
        );
        final IRecord resp = this.create(proj_param);
        Output.println(resp);
    }
    
    @Test
    public void foo_update() {
        SystemJunit system = new SystemJunit();
        Output.println(system.optOf("ITSM"));
        final IRecord proj_param = REC( //
                "title", "ITSM-PROJ001", //
                "projectDevType", "0", //
                "hostSystemOid", "ITSM",  // 需要与 系统Id相一致。
                "projectUserName", "zhangsan", // 需要与系统负责人需要与项目负责人。
                "describe", "ITSM-PROJ001", //
                "oid", "202206071429314753290", //
                "processId", "202206071429314753290", //
                // "projectStatus", "0", //
                "approvalNodes", this.approvalNodes(null) // 项目审批节点
                , "startDate", "2022-06-07", "endDate", "2022-06-31"//
        );
        final IRecord resp = this.create(proj_param);
        println(resp);
    }
    
    @Test
    public void qux() {
        println(oid());
        println(this.approvalNodes(null));
    }
}
