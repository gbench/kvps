package gbench.sandbox.common;

import org.junit.jupiter.api.Test;

import static gbench.util.io.Output.println;
import static gbench.util.lisp.IRecord.REC;
import static java.util.Arrays.asList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

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

}
