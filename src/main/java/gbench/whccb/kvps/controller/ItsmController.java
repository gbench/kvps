package gbench.whccb.kvps.controller;

import static gbench.util.io.Output.println;
import static gbench.util.lisp.IRecord.REC;

import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gbench.util.lisp.DFrame;
import gbench.util.lisp.IRecord;

@RequestMapping("itsm")
@RestController
public class ItsmController {

    /**
     * 需求分解-结果回传（需求分解之DEVOPS->ITSM）需求条目.
     * 
     * 需求条目，分批次传送。
     * 
     * @return {code,message,result}
     */
    @RequestMapping(value = "devops/reqents", consumes = "application/json")
    public Map<String, Object> reqents(final @RequestBody String message) {
        final IRecord ret = IRecord.REC("code", 0);

        final IRecord req = IRecord.REC(message);
        final String projCode = req.str("projCode");
        final String reqRootCode = req.str("reqRootCode"); // 需求根ID
        final DFrame reqEnts = req.llS("reqEnts").map(IRecord::REC).collect(DFrame.dfmclc); // 需求根ID

        println("projCode", projCode);
        println("reqRootCode", reqRootCode);
        println("reqEnts");
        println(reqEnts);

        // 返回值 回填
        ret.add("message", "需求分解-结果回传（需求分解之DEVOPS->ITSM）需求条目.", "result", REC("path", "devops/uattest_apply"));

        return ret.toMap2();
    }

    /**
     * UAT提测申请 ( DEVOPS->ITSM）
     * 
     * @return {code,message,result}
     */
    @RequestMapping(value = "devops/uattest_apply", consumes = "application/json")
    public Map<String, Object> uattest_apply(final @RequestBody String message) {
        final IRecord ret = IRecord.REC("code", 0);

        final IRecord req = IRecord.REC(message);
        final String projCode = req.str("projCode");
        final String reqRootCode = req.str("reqRootCode"); // 需求根ID
        final String title = req.str("title"); // 提测标题
        final DFrame reqEnts = req.llS("reqEnts").map(IRecord::REC).collect(DFrame.dfmclc); // 需求条目列表
        final String attachment = req.str("attachment"); // 提测条目地址
        final String schedId = req.str("schedId"); // 排期Id，DEVOPS 是按照 排期ID来组织任务的。
        final String scheduleTestRecordId = req.str("scheduleTestRecordId");

        println("projCode", projCode);
        println("reqRootCode", reqRootCode);
        println("title", title);
        println("attachment", attachment);
        println("schedId", schedId);
        println("scheduleTestRecordId", scheduleTestRecordId);
        println("reqEnts", reqEnts);

        // 返回值 回填
        ret.add("message", "UAT 提测申请", "result", REC("path", "devops/uattest_apply"));

        return ret.toMap2();
    }

}
