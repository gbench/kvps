package gbench.sandbox.weihai.devops;

import static gbench.util.lisp.IRecord.REC;

import java.time.LocalDate;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import gbench.util.lisp.IRecord;
import gbench.whccb.client.DevOpsClient;

/**
 * 
 * @author xuqinghua
 *
 */
public class ReqJunit extends DevOpsClient {
    /**
     * 项目创建
     * 
     * @param params
     * @return
     */
    public IRecord create(final IRecord params) {
        final String api = apiOf("/auth/oa/businessSheet/insert");
        return this.post_json(api, params);
    }

    @Test
    public void foo_create() {
        final LocalDate startDate = LocalDate.now();
        final IRecord daily_params = REC( //
                "approvalNodes", this.approvalNodes(null) //
                , "assistSystemOidList", Arrays.asList(oid(), oid(), oid()) // 协办系统
                , "endDate", startDate.plusDays(10) //
                , "fileOaUrl", "" //
                , "hostSystemOid", oid() // 主办系统
//                ,"hostSystemProjectOid",oid() // 日常需求
                , "ifNew", true //
                , "oid", oid() // 需求id
                , "startDate", startDate //
                , "title", "日常需求" //
        );

        final IRecord proj_params = REC( //
                "approvalNodes", this.approvalNodes(null) //
//                , "assistSystemOidList", Arrays.asList(oid(), oid(), oid()) // 协办系统
//                , "endDate", startDate.plusDays(10) //
                , "fileOaUrl", "" //
//                , "hostSystemOid", oid() // 主办系统
                , "hostSystemProjectOid", oid() // 日常需求
//                , "ifNew", true //
//                , "oid", oid() // 需求id
//                , "startDate", startDate //
//                , "title", "日常需求" //
        );

        this.create(daily_params);
        this.create(proj_params);
    }
}
