package gbench.sandbox.weihai.flds;

import static gbench.util.io.Output.println;
import static gbench.util.lisp.IRecord.REC;
import static java.util.Arrays.asList;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import gbench.util.lisp.IRecord;

public class ITSM_SUPPLIES_Junit {

    /**
     * 转换成 json 格式
     * 
     * @param obj
     * @return
     */
    public static String toJson(final Object obj) {
        final ObjectMapper objM = new ObjectMapper();
        objM.registerModule(new JavaTimeModule());
        String json = null;
        try {
            json = objM.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * 
     * 事件：项目立项流程审批结束后进行推送 <br>
     * 项目信息/项目基本信息
     */
    public IRecord itsm_proj() {

        println("项目信息/项目基本信息");
        //
        final IRecord proj = REC(//
                "sys_id", REC("label", "系统ID", "value", "") //
                , "label", REC("label", "项目名称", "value", "") //
                , "executive", REC("label", "项目负责人", "value", "") //
                , "start_date", REC("label", "项目开始日期", "value", "") //
                , "due_date", REC("label", "项目结束", "value", "") //
                , "description", REC("label", "项目描述", "value", "") //
                , "proj_id", REC("label", "项目编号", "value", "") //
                , "type", REC("label", "项目研发类型", "value", "") //
                , "flow_id", REC("label", "流程id", "value", "") //
                , "status", REC("label", "项目状态", "value", "") //
        );

        System.out.println(toJson(proj.toMap2()));

        return proj;

    }

    /**
     * 
     * 事件：提交入场申请审批通过后进行推送 <br>
     * 
     * 外包人员入场/外包入场申请
     */
    public IRecord itsm_outsrc_workers() {

        println("外包人员入场/外包入场申请");
        //
        final IRecord outsrc_workers = REC( //
                "proj_id", REC("label", "所属项目编号", "value", "") //
                , "company", REC("label", "供应商名称", "value", "") //
                , "workers", asList(REC( //
                        "label", REC("label", "姓名", "value", "") //
                        , "phone", REC("label", "电话", "value", "") //
                        , "role", REC("label", "用户角色", "value", "") //
                )));

        System.out.println(toJson(outsrc_workers.toMap2()));

        return outsrc_workers;
    }

    /**
     * 事件：需求分析说明书上传后进行推送 <br>
     * 项目需求分解 请求 <br>
     * 需求管理/项目需求
     */
    public IRecord itsm_proj_req() {

        println("需求管理/项目需求");
        //
        final IRecord proj_req = REC( //
                "proj_id", REC("label", "所属项目编号", "value", ""), //
                "req_doc_url", REC("label", "需求分析说明书", "value", "") //
        );

        System.out.println(toJson(proj_req.toMap2()));

        return proj_req;
    }

    /**
     * 事件：请求流程审批通过后进行推送 <br>
     * 日常需求分解 请求 <br>
     * 需求管理/日常需求
     */
    public IRecord itsm_daily_req() {

        println("需求管理/日常需求");
        //
        final IRecord proj_req = REC( //
                "proj_id", REC("label", "所属项目编号", "value", "") //
                , "req_id", REC("label", "需求ID", "value", "") //
                , "title", REC("label", "业务单标题", "value", "") //
                , "title", REC("label", "业务需求主办系统", "value", "") //
                , "title", REC("label", "业务需求主办项目", "value", "") //
                , "title", REC("label", "业务需求协办系统列表", "value", "") //
                , "title", REC("label", "业务需求开始日期", "value", "") //
                , "title", REC("label", "业务需求截止日期", "value", "") //
                , "title", REC("label", "需求分析书名书路径", "value", "") //
        );

        System.out.println(toJson(proj_req.toMap2()));

        return proj_req;
    }

    /**
     * 
     * 事件：响应 UAT测试流程 请求<br>
     * UAT提测结果/UAT测试流程
     */
    public IRecord itsm_uat_res() {

        println("UAT提测结果/UAT测试流程");
        //
        final IRecord uat_res = REC( //
                "proj_id", REC("label", "所属项目编号", "value", "") //
                , "sched_id", REC("label", "排期ID", "value", "") //
                , "req-ents", asList( // 需求条目列表
                        REC("ent_id", REC("label", "需求条目id", "value", ""), "label",
                                REC("label", "需求条目名称", "value", ""))),
                "test_ents", asList( // 需求条目以及对应的测试结果
                        REC( //
                                "ent_id", REC("label", "需求条目id", "value", ""), //
                                "test-result", REC("label", "测试结果", "value", "")) //
                ), //
                "result", REC("label", "审批结果 ", "value", ""));

        System.out.println(toJson(uat_res.toMap2()));

        return uat_res;
    }

    /**
     * 事件：响应 投产上线流程发起<br>
     * 投产申请结果/投产上线流程发起
     */
    public IRecord itsm_launch_res() {

        println("投产申请结果/投产上线流程发起");
        //
        final IRecord launch_res = REC( //
                "proj_id", REC("label", "所属项目编号", "value", "") //
                , "sched_id", REC("label", "排期ID", "value", "") //
                , "msg_deploy", REC("label", "投产审批通过后部署通知", "value", "") //
                , "msg_launch", REC("label", "投产结果通知", "value", "") //
        );

        System.out.println(toJson(launch_res.toMap2()));

        return launch_res;
    }

    /**
     * 事件：响应里程碑同步 <br>
     * 里程碑信息/里程碑同步
     */
    public IRecord itsm_milestone_res() {

        println("里程碑信息/里程碑同步");
        //
        final IRecord milestones = REC("proj_id", REC("label", "所属项目编号", "value", "") //
                , "milestones", asList( // 里程碑节点
                        REC("name", REC("label", "里程碑节点", "value", ""), "deliverable",
                                REC("label", "里程碑交付物", "value", ""), "deliverable_url",
                                REC("label", "里程碑交付物路径", "value", ""), "due_date",
                                REC("label", "里程碑节点预计截止日期", "value", ""))));

        System.out.println(toJson(milestones.toMap2()));

        return milestones;
    }

    /**
     * 模拟数据展示
     */
    @Test
    public void foo() {
        this.itsm_proj();
        this.itsm_outsrc_workers();
        this.itsm_proj_req();
        this.itsm_daily_req();
        this.itsm_uat_res();
        this.itsm_launch_res();
        this.itsm_milestone_res();
    }

    /**
     * 需求管理/项目需求
     * 
     * 信息时点: 项目立项流程审批结束后进行推送
     */
    @RequestMapping(value = "/itsm/proj", consumes = "application/json")
    public Map<String, Object> post_itsm_proj_req(@RequestBody String json) {
//        //
//        final IRecord proj_req = REC(
//                "proj_id",REC("label","所属项目编号","value",""), //
//                "req_doc_url",REC("label","需求分析说明书","value","") //
//        );
//        
//        System.out.println(toJson(proj_req.toMap2()));

        return REC().toMap();

    }

}
