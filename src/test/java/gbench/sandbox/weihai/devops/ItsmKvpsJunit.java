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
     * 需求分解
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
     * 提测请求
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
