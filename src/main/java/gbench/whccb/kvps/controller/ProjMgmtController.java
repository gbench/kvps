package gbench.whccb.kvps.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gbench.util.lisp.IRecord;
import gbench.whccb.kvps.model.DBModel;

@RequestMapping("pm")
@RestController
public class ProjMgmtController {

    /**
     * 获取项目团队成员信息
     * 
     * http://localhost:8089/kvps/pm/teamGroup
     * 
     * @return 项目团队
     */
    @RequestMapping("teamGroup")
    public Map<String, Object> teamGroup(final String proj_key) {
        final IRecord rec = IRecord.REC("code", 0);
        rec.add("users", dbModel.teamGroup(proj_key));
        return rec.toMap2();
    }

    /**
     * 获取项目团队成员信息
     * 
     * http://localhost:8089/kvps/pm/send
     * 
     * @param message 消息对象 {name,key1,key2,....}
     * @return 项目团队
     */
    @RequestMapping(value = "send", consumes = "application/json")
    public Map<String, Object> send(final @RequestBody String message) {
        final IRecord rec = IRecord.REC("code", 0);
        final IRecord msg = IRecord.REC(message);
        rec.add("message", msg);
        final String name = msg.strOpt("name").orElse("-");
        final Object[] params = msg.llS("keys").toArray();
        switch (name) {
        case "reqdec": { //
            // 需求分析
            final Object proj = params[0]; // 项目信息 的 json, IRecord 可以理解为一个 JAVA 实现 的 JS的Object 模型。
            final Object reqdoc = params[1]; // 需求文档 的 json
            rec.add("handler", IRecord.REC("event_handler", "handle_reqdec(proj,reqdoc)", "desc", "需求分解", "name",
                    "reqdec", "proj", proj, "reqdoc", reqdoc));
            // handle_reqdec(proj,reqdoc); // 需求分解的具体逻辑
            break;
        }
        case "uatents": { //
            // 验收申请
            final Object proj = params[0]; // 项目信息 的 json, IRecord 可以理解为一个 JAVA 实现 的 JS的Object 模型。
            final Object reqents = params[1]; // 验收条目的需求条目
            rec.add("handler", IRecord.REC("event_handler", "handle_uatents(proj,reqents)", "desc", "验收测试", "name",
                    "uatents", "proj", proj, "reqents", reqents));
            // handle_uatents(params);
            break;
        }
        default: {
            rec.add("handler", IRecord.REC("event_handler", "handle_unknown(proj,reqents)", "desc", "未知消息", "name",
                    "unknown", "params", params));
            // I don't know
            // so,
            // do nothing
        }
        }
        return rec.toMap2();
    }

    @Autowired
    private DBModel dbModel;
}
