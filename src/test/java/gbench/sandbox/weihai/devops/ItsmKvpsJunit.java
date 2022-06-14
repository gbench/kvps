package gbench.sandbox.weihai.devops;

import static gbench.util.lisp.IRecord.REC;
import static gbench.util.io.Output.println;

import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import gbench.util.lisp.IRecord;
import gbench.whccb.client.ItsmKvpsClient;

/**
 * ITSM的 接口模拟
 * @author xuqinghua
 *
 */
public class ItsmKvpsJunit extends ItsmKvpsClient {

    /**
     * 项目创建
     * 
     * @param params
     * @return
     */
    public IRecord reqents(final IRecord params) {
        final String api = apiOf("/itsm/devops/reqents");
        return this.post_json(api, params);
    }

    /**
     * 项目创建
     * 
     * @param params
     * @return
     */
    public IRecord uattest_apply(final IRecord params) {
        final String api = apiOf("/itsm/devops/uattest_apply");
        return this.post_json(api, params);
    }

    /**
     * 项目需求管理 的前提准备：
     * REQ:3.3
     * 1)用户可发起日常需求申请流程，审批通过后需要将需求推送到DevOps平台，
     * 2)DevOps对需求进行拆解排期开发。
     * 3)DevOps拆分子需求，需要将子需求回传给ITSM平台。(项目需求在DevOps拆分后也需要同步给ITSM)
     *
     */
    @Test
    public void foo_reqents() {
        List<Map<String, Object>> reqEnts = Arrays.stream("a,b,c,d".split("[,]+"))
                .map(e -> REC("title", e, "description", "_" + e)) // 模拟需求条目的标题与说明
                .map(e -> e.add(REC("reqCode", oid(), "status", "1", "schedId", oid()))).map(e -> e.toMap2())
                .collect(Collectors.toList());
        final IRecord params = REC("projCode", oid(), "reqRootCode", oid(), "reqEnts", reqEnts); // 接口参数

        println(this.reqents(params));
    }

    /**
     * 3.2.14 UAT 测试。
     * 3.2.14.10.2流程说明 （ 测试中心负责人发起 ）
     * 字段列表：3.2.14.10.4.3
     * 需求条目：关联需求。
     *
     * 提测请求
     *
     */
    @Test
    public void foo_uattest_apply() {
        List<Map<String, Object>> reqEnts = Arrays.stream("a,b,c,d".split("[,]+"))
                .map(e -> REC("title", e, "description", "_" + e))
                .map(e -> e.add(REC("reqCode", oid(), "status", 1, "schedId", oid()))).map(e -> e.toMap2())
                .collect(Collectors.toList()); // 需求条目

        final IRecord params = REC( //
                "projCode", oid(), // 项目编码
                "reqRootCode", oid(), // 根需求
                "title", "foo_uattest_apply", // 提测Id 
                "reqEnts", reqEnts, // 需求条目
                "attachment", "attachment_xxx", //
                "schedId", oid(), // 排期id，DEVOPS开发工作的实际ID
                "scheduleTestRecordId", oid() // 排期提测ID，后续进行 结果通知的时候需要使用。
        );
        println(this.uattest_apply(params));
    }

}
