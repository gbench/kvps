package gbench.sandbox.weihai.devops;

import static gbench.util.lisp.IRecord.REC;
import static java.util.Arrays.asList;

import org.junit.jupiter.api.Test;

import gbench.util.io.Output;
import gbench.util.lisp.IRecord;

public class ProjectJunit extends DevOpsClient {

    /**
     * 
     * @param params
     * @return
     */
    public IRecord create(IRecord params) {
        final String api = apiOf("/auth/oa/project/insert");
        return this.post_json(api, params);
    }

    @Test
    public void foo() {
        SystemJunit system = new SystemJunit();
        Output.println(system.systemOf("ITSM"));
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
}
