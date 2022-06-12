package gbench.sandbox.common;

import org.junit.jupiter.api.Test;

import static gbench.util.io.Output.println;
import static gbench.util.lisp.IRecord.REC;
import static java.util.Arrays.asList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Stream;

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
        final IRecord rec = REC( //
                "id", 1000, //
                "name", "zhangsan", //
                "password", "123456", //
                "flag", false, //
                "weight", 70.6, //
                "birth_dt", new Date(), //
                "birth_ldt", LocalDateTime.now(), //
                "birth_ld", LocalDate.now(), //
                "birth_lt", LocalTime.now(), //
                "address", REC("city", "shanghai", "street", "fahuazhen road 11 nong 11#201 "), //
                "items", asList( //
                        REC("name", "a", "sex", "man", //
                                "school", REC("name", "gongfu school", "master", "tortoise")),
                        REC("name", "b", "sex", "woman", //
                                "school", REC())), //
                "empty_list", asList() // 空列表
        );
        final String json = MyJson.pretty(rec);
        println(json);
        final IRecord r = MyJson.fromJson(json);
        println(r);
        final DFrame dfm = r.llS("items").collect(DFrame.dfmclc(IRecord::REC));
        println(dfm);
    }

    @Test
    public void bar() {
        println(IRecord.REC(Arrays.asList(1, 2, 3, 4, LocalDateTime.now())).json());
        println(IRecord.REC(Arrays.asList(1, 2, 3, 4)));
        println(IRecord.REC(Arrays.asList(1, 2, 3, 4).iterator()));
        println(IRecord.REC(Stream.of(1, 2, 3, 4)));
        println(IRecord.REC(REC("name", "zhangsan", "sex", true).tuples()));
        println(IRecord.REC(REC("name", "zhangsan", "sex", true).toMap().entrySet().iterator()));
        println(IRecord.REC("name:'zhangsan',sex:false,postcode:200052")); // 省略括号的形式
        println(IRecord.REC("name:[1,2,,,]")); // 省略值类型
        println(IRecord.REC("http://localhost:8089/kvps/pm/teamGroup")); // url 结构
        println(IRecord.REC("E:/slicee/temp/kvps/data/registry.json")); // 文件结构
    }

}
