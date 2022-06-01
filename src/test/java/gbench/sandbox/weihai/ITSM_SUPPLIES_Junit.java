package gbench.sandbox.weihai;

import static gbench.util.lisp.IRecord.REC;
import static java.util.Arrays.asList;

import org.junit.jupiter.api.Test;

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
     * 项目信息/项目基本信息
     */
    @Test
    public void itsm_proj() {
        //
        final IRecord proj = REC(
                "sys_id",REC("name","系统ID","value",""), //
                "name", REC("name","项目名称","value",""), //
                "executive", REC("name","项目负责人","value",""), //
                "start_date", REC("name","项目开始日期","value",""), //
                "due_date", REC("name","项目结束","value",""), //
                "description", REC("name","项目描述","value",""), //
                "proj_id", REC("name","项目编号","value",""), //
                "type", REC("name","项目研发类型","value",""), //
                "flow", REC("name","流程id","value",""), //
                "status", REC("name","项目状态","value","") //
                );
        
        System.out.println(toJson(proj.toMap2()));
        
    }
    
    /** 
     * 外包人员入场/外包入场申请
     */
    @Test
    public void itsm_outsrc_workers() {
        //
        final IRecord outsrc_workers = REC(
                "proj_id",REC("name","所属项目编号","value",""), //
                "company",REC("name","供应商名称","value",""), //
                 "workers",asList(
                         REC( // 
                                 "name",REC("name","姓名","value","") //
                                 ,"phone",REC("name","电话","value","") //
                                 ,"role",REC("name","用户角色","value","") //
                           ),
                         
                         REC( // 
                                 "name",REC("name","姓名","value","") //
                                 ,"phone",REC("name","电话","value","") //
                                 ,"role",REC("name","用户角色","value","") //
                           )
                       
                   )
                );
        
        System.out.println(toJson(outsrc_workers.toMap2()));
        
    }
    
    
    /**
     * 需求管理/项目需求
     */
    @Test
    public void itsm_proj_req() {
        //
        final IRecord proj_req = REC(
                "proj_id",REC("name","所属项目编号","value",""), //
                "req_doc_url",REC("name","需求分析说明书","value","") //
        );
        
        System.out.println(toJson(proj_req.toMap2()));
        
    }
    
    /**
     * 需求管理/项目需求
     */
    @Test
    public void itsm_daily_req() {
        //
        final IRecord proj_req = REC(
                "proj_id",REC("name","所属项目编号","value",""), //
                "req_doc_url",REC("name","需求分析说明书","value","") //
        );
        
        System.out.println(toJson(proj_req.toMap2()));
        
    }

}
