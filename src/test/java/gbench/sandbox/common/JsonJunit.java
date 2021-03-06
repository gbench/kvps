package gbench.sandbox.common;

import org.junit.jupiter.api.Test;

import static gbench.util.io.Output.println;
import static gbench.util.lisp.IRecord.REC;
import static java.util.Arrays.asList;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Date;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import gbench.util.json.MyJson;
import gbench.util.lisp.DFrame;
import gbench.util.lisp.IRecord;
import gbench.util.lisp.MyRecord;

/**
 * 简单的JSON的测试
 *
 * @author xuqinghua
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
                                "school", REC("name", "gongfu school", "master", "tortoise")), REC("name", "b", "sex", "woman", //
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
        println(IRecord.REC("http://localhost:8089/kvps/junit/sendData2?name=zhangsan&keys=1,2,3")); // url 结构
        println(IRecord.REC("E:/slicee/temp/kvps/data/devops/project.json")); // 文件结构

        println(MyRecord.send("http://localhost:8089/kvps/junit/sendData", REC("$method", "post", "name", "reqdec", "keys", //
                asList("a,b,c".split(",")))));
        println(MyRecord.send("http://localhost:8089/kvps/junit/sendData2", REC("$method", "post", "$content_type", "form", "name", "reqdec", "keys", //
                asList("a,b,c,9".split(",")))));
        // 失败的请求
        println(MyRecord.send("http://localhost:8089/kvps/junit/sendData3", REC("$method", "post", "$content_type", "multipart", "name", "key", "keys", "1,2,3".split(","), "file", new File("E:/slicee/temp/kvps/data/devops/project.json"))));

    }

    /**
     * 扩展名
     */
    @Test
    public void qux() {
        final Function<String, String> filename_of = line -> {
            final Matcher matcher = Pattern.compile("([^/\\\\]+)$").matcher(line);
            final String name = matcher.find() ? matcher.group(1) : line;
            return name;
        };
        final Function<String, String> extension_of = line -> {
            final int i = line.lastIndexOf(".");
            return i >= 0 ? line.substring(i + 1) : "";
        };
        println(filename_of.apply("a/b/c/d.jpg"));
        println(extension_of.apply("d.jpg"));
        println(IRecord.FT("'$0'", extension_of.apply("abc")));
    }

    /**
     * slidingS 的测试
     */
    @Test
    public void qux2() {
        IRecord.slidingS(Arrays.asList(1, 2, 3, 4, 5, 6), 3, 1, false).forEach(e -> {
            println(e);
        });
        println("--------------------");
        IRecord.slidingS(Arrays.asList(1, 2, 3, 4, 5, 6), 3, 1, true).forEach(e -> {
            println(e);
        });

        IRecord.iterate(0, i -> i < 10, i -> i + 1).forEach(e -> {
            println(e);
        });
    }

}
