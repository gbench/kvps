package gbench.sandbox.weihai.devops;

import static gbench.util.lisp.IRecord.FT;
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
    public IRecord new_proj(IRecord params) {
        final String api = FT("$0$1", host, "/auth/oa/project/insert");
        return this.post_json(api, params);
    }

    @Test
    public void foo() {
        final IRecord proj_param = REC( //
                "title", "ABC", "projectDevType", "0", //
                "hostSystemOid", "12345671234567123456712345671231", "projectUserName", "0001", //
                "describe", "ABC", //
                "oid", "202205241429314753290", //
                "processId", "202205241429314762625", //
                // "projectStatus", "0", //
                "approvalNodes", asList(REC("activityName", "项目经理", "createDate", "2022-05-16 15:08:24.182",
                        "createUserName", "项目负责人", "actionName", "项目经理", "msg", "项目负责人") //
                ) //
                , "startDate", "2022-05-31", "endDate", "2022-06-31"//
        );
        final IRecord resp = this.new_proj(proj_param);
        Output.println(resp);

    }
}
