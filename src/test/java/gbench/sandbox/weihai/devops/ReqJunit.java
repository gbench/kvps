package gbench.sandbox.weihai.devops;

import static gbench.util.lisp.IRecord.REC;

import java.time.LocalDate;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import gbench.util.lisp.IRecord;
import gbench.whccb.client.DevOpsClient;

/**
 * 项目需求管理 的前提准备：
 * REQ:3.3
 * 1)用户可发起日常需求申请流程，审批通过后需要将需求推送到DevOps平台，
 * 2)DevOps对需求进行拆解排期开发。
 * 3)DevOps拆分子需求，需要将子需求回传给ITSM平台。(项目需求在DevOps拆分后也需要同步给ITSM)
 *
 * @author xuqinghua
 */
public class ReqJunit extends DevOpsClient {
    /**
     * 需求分解（请求）
     *
     * @param params
     * @return
     */
    public IRecord create(final IRecord params) {
        final String api = apiOf("/auth/oa/businessSheet/insert");
        return this.post_json(api, params);
    }

    /**
     * 流程：需求发起人，业务申请部门负责人，
     */
    @Test
    public void foo_daily_create() {
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

        this.create(daily_params);

    }

    @Test
    public void foo_proj_create() {
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
//                , "title", "项目需求" //
        );
        this.create(proj_params);
    }
}
