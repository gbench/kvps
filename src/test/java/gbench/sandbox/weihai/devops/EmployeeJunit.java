package gbench.sandbox.weihai.devops;

import java.util.Map;
import java.util.List;

import static gbench.util.io.Output.println;
import static gbench.util.lisp.IRecord.FT;
import static gbench.util.lisp.IRecord.REC;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import gbench.util.lisp.DFrame;
import gbench.util.lisp.IRecord;
import gbench.util.lisp.Tuple2;
import gbench.whccb.client.DevOpsClient;

/**
 * 外包人员入场：3.11
 * 外包人员信息：3.11.4.3
 * 事件：外包条线分管副总 审批通过。 3.11.2
 */
public class EmployeeJunit extends DevOpsClient{
    /**
     * 项目创建
     * 
     * @param params
     * @return
     */
    public IRecord create(final IRecord params) {
        final String api = apiOf("/auth/user/sys/bulkinsert");
        return this.post_json(api, params);
    }

    /**
     * 事件:
     */
    @Test
    public void foo_create() {
        final List<Map<String, Object>> userListVO = Arrays.stream("zhangsan,lisi,wangwu,zhaoliu".split("[,]+"))
                .map(Tuple2.snb(0)).map(e -> REC( //
                        "email", FT("$0@gazellio.com", e._2), //
                        "realName", e._2, //
                        "telephone", "123598712" + e._1, //
                        "username", "cs_" + e._2, //
                        "role", "outsourceDeveloper"))
                .map(e -> e.toMap2()).collect(Collectors.toList());
        final IRecord params = REC("userListVO",userListVO,"serviceProvider","上海速邦信息科技有限公司","projectOid",oid());
        println(params.llS2("userListVO", IRecord::REC).collect(DFrame.dfmclc));
        this.create(params);
    }
}
