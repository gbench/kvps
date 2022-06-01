package gbench.sandbox.weihai.flds;

import static gbench.util.io.Output.println;
import static gbench.util.lisp.IRecord.REC;
import static java.util.Arrays.asList;

import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import gbench.util.lisp.IRecord;

/**
 * 
 * @author xuqinghua
 *
 */
public class DEVOPS_SUPPLIES_Junit {

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
     * 事件：响应需求（项目）分解 <br>
     * 需求分解的结果：<br>
     * 需求管理/项目需求
     */
    public IRecord devops_proj_req() {

        println("需求管理/项目需求");
        //
        final IRecord proj_req = REC( //
                "proj_id", REC("label", "所属项目编号", "value", "") //
                , "req-ents", asList( // 需求条目
                        REC( // 条目属性
                                "req_id", REC("label", "需求ID", "value", "") //
                                , "title", REC("label", "需求条目标题", "value", "") //
                                , "status", REC("label", "需求条目状态", "value", "") //
                        )));

        System.out.println(toJson(proj_req.toMap2()));

        return proj_req;
    }

    /**
     * 事件：响应需求(日常)分解 <br>
     * 需求分解的结果。 <br>
     * 需求管理/日常需求
     */
    public IRecord devops_daily_req() {

        println("需求管理/日常需求");
        //
        final IRecord proj_req = REC( //
                "proj_id", REC("label", "所属项目编号", "value", "") //
                , "req-id", REC("label", "需求父级ID") //
                , "req-ents", asList( // 需求条目
                        REC( // 条目属性
                                "ent_id", REC("label", "需求ID", "value", "") //
                                , "sched_id", REC("label", "排期ID", "value", "") //
                                , "title", REC("label", "需求条目标题", "value", "") //
                                , "status", REC("label", "需求条目状态", "value", "") //
                                , "description", REC("label", "需求描述", "value", "") //
                                , "start_date", REC("label", "需求预计开始时间", "value", "") //
                                , "due_date", REC("label", "需求预计完成时间", "value", "") //
                        )));

        System.out.println(toJson(proj_req.toMap2()));

        return proj_req;
    }

    /**
     * 事件：发起UAT测试 <br>
     * 需求分解的结果。 <br>
     * UAT提测申请/UAT测试
     */
    public IRecord devops_uat_test_req() {

        println("UAT提测申请/UAT测试");
        //
        final IRecord proj_req = REC( //
                "req_id", REC("label", "需求单ID", "value", "") //
                , "sched-id", REC("label", "排期ID", "value", "") //
                , "req-ents", asList( // 需求条目
                        REC( // 条目属性
                                "ent_id", REC("label", "需求ID", "value", "") //
                                , "sched_id", REC("label", "排期ID", "value", "") //
                                , "title", REC("label", "需求条目标题", "value", "") //
                                , "status", REC("label", "需求条目状态", "value", "") //
                                , "description", REC("label", "需求描述", "value", "") //
                                , "start_date", REC("label", "需求预计开始时间", "value", "") //
                                , "due_date", REC("label", "需求预计完成时间", "value", "") //
                        )), "attachment", REC("label", "附件地址", "value", ""));

        System.out.println(toJson(proj_req.toMap2()));
        return proj_req;
    }

    @Test
    public void foo() {
        this.devops_proj_req();
        this.devops_daily_req();
        this.devops_uat_test_req();
    }

}
