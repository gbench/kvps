package gbench.sandbox.common;

import static gbench.util.io.Output.println;
import static gbench.util.lisp.IRecord.REC;
import static java.util.Arrays.asList;

import org.junit.jupiter.api.Test;

import gbench.util.json.MyJson;
import gbench.util.lisp.DFrame;
import gbench.util.lisp.IRecord;

/**
 * 
 * @author xuqinghua
 *
 */
public class JsonJunit {

    /**
     * 格式化输出
     */
    @Test
    public void foo() {
        final IRecord rec = REC("name", "zhangsan", "address", REC("city", "shanghai", "street", "fahuazhen"), "items",
                asList(REC("name", "a", "sex", "boy", "school", REC("name", "gongfu school", "master", "tortoise")),
                        REC("name", "b", "sex", "girl")));
        final String json = MyJson.pretty(rec);
        println(json);
        final IRecord r = MyJson.fromJson(json);
        println(r);
        final DFrame dfm = r.llS("items").collect(DFrame.dfmclc(IRecord::REC));
        println(dfm);
    }

}
