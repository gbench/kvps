package gbench.sandbox.weihai.devops;

import static gbench.util.lisp.IRecord.FT;
import static gbench.util.lisp.IRecord.REC;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import gbench.util.lisp.IRecord;
import gbench.whccb.client.DevOpsClient;

/**
 *
 * 3.2.14 UAT 测试。
 * 3.2.14.10.2流程说明 （ 测试中心负责人发起 ）
 * 字段列表：3.2.14.10.4.3
 *  需求条目：关联需求。
 *
 * @author xuqinghua
 *
 */
public class UATTest extends DevOpsClient {

    /**
     * 
     * @param params
     * @return
     */
    IRecord uat_result(final IRecord params) {
        final String api = apiOf(FT("/auth/oa/schedule/approval/$0", params.str("scheduleTestRecordId")));
        return this.post_json(api, params);
    }

    /**
     * uat 结果回传
     */
    public void foo_uat_result() {
        final List<Map<String, Object>> testEnts = Stream.of(1, 2, 3, 4, 5, 6) //
                .map(i -> IRecord.REC("reqEnt", oid(), "testResult", 0)) //
                .map(e -> e.toMap2())//
                .collect(Collectors.toList());
        final IRecord dto = REC( //
                "approvalNodes", this.approvalNodes(null) //
                , "approvalStatus", 2// 0=审批中，1=审批失败，2=审批通过，3=测试失败，4= 投产失败，5=投产成功
                , "recordId", oid(), //
                "testEnts", testEnts // 测试结果
        );
        final IRecord params = REC("projCode", oid(), "dto", dto);
        this.uat_result(params);
    }
}
