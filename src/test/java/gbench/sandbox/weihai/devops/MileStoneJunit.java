package gbench.sandbox.weihai.devops;

import static gbench.util.lisp.IRecord.REC;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import gbench.util.lisp.IRecord;
import gbench.util.lisp.Tuple2;

public class MileStoneJunit extends DevOpsClient {
    
    /**
     * 项目创建
     * 
     * @param params
     * @return
     */
    public IRecord create(final IRecord params) {
        final String api = apiOf("/project/milepost/instance/insert");
        return this.post_json(api, params);
    }
    
    
    @Test
    public void foo_create() {
        final List<Map<String, Object>> milestones = Arrays.stream("a,b,c,d,e,f".split("[,]+")) //
                .map(Tuple2.snb(1)) // 生成序列号
                .map(e -> { // 生成里程碑计划
                    final String phaseId = oid();
                    final String name  = e._2;
                    final IRecord rec = REC( //
                            "phaseId", phaseId // 里程碑阶段Id
                            ,"name", name // 阶段名称
                            ,"deliverable", "deliverable_" + name //
                            ,"delivarableUrl", "http://localhost:8089/kvps/media/file/download?key=" + phaseId //
                            ,"dueDate",LocalDate.now().plusDays(e._1)
                    ); // 
                    return rec;
                }).map(e -> e.toMap2()).collect(Collectors.toList());
        final IRecord params = IRecord.REC("projCode",oid(),milestones);
        this.create(params);
    }

}
