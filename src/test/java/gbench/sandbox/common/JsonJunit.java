package gbench.sandbox.common;

import static gbench.util.io.Output.println;
import static gbench.util.lisp.IRecord.REC;
import static java.util.Arrays.asList;

import org.junit.jupiter.api.Test;

import gbench.util.json.MyJson;
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
                asList(REC("name", "a"), REC("name", "b")));
        final String json = MyJson.pretty(rec);
        println(json);
        println(MyJson.fromJson(json));
    }

}
