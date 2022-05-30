package gbench.whccb.kvps.model;

import static gbench.util.io.Output.println;
import static gbench.util.lisp.IRecord.FT;
import static gbench.util.lisp.IRecord.REC;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.stereotype.Component;

import gbench.util.data.DataApp;
import gbench.util.lisp.IRecord;

@Component
public class DBModel {

    /**
     * 数据库模型
     * 
     * @param ds 数据源
     */
    public DBModel(final DataSource ds) {
        dataMain = new DataApp(ds);
        this.initalize();
    }

    /**
     * 基础数据的准备
     */
    public void initalize() {
        dataMain.withTransaction(sess -> {
            // 创建用户表
            final IRecord user_proto = REC("id", 1, "name", "zhangsan", "sex", true);
            final String create_table_user = table_of.apply("T_USER", user_proto);
            sess.sql2execute(create_table_user); // 创建用户表
            for (int i = 0; i < 100; i++) {
                final String t = "insert into T_USER values ($0,'$1',$2)";
                final String insert_sql_user = FT(t, i, "zhang" + i, i % 2 == 0);
                sess.sql2execute(insert_sql_user);
                println(sess.sql2x("select * from T_USER"));
            } // for
        }); // withTransaction
    }

    /**
     * 查看项目的人员的名单
     * 
     * @param proj_key 项目主键
     * @return 人员名单,[{id,name}]
     */
    public List<Map<String, Object>> teamgroup(final String proj_key) {
        return this.dataMain.sql2dframe("select * from T_USER") //
                .rowS().map(e -> e.toMap()).collect(Collectors.toList());
    }

    final static IRecord java2sql = REC( //
            "Integer", "INT" //
            , "Double", "DOUBLE" //
            , "String", "VARCHAR(512)" //
            , "Boolean", "BOOLEAN" //
            , "LocalDateTime", "TIMESTAMP" //
            , "LocalDate", "TIMESTAMP" //
            , "LocalTIME", "TIMESTAMP" //
    ); // java 的类型转 sql的类型
    final static Function<IRecord, IRecord> flds_of = p -> p.tupleS()
            .map(e -> e.map2(o -> o.getClass().getSimpleName()))
            .collect(IRecord.recclc(e -> e.map2(s -> java2sql.strOpt(s).orElse("VARCHAR(512)")))); // 提取字段类型信息
    final static BiFunction<String, IRecord, String> table_of = (name, defs) -> FT("create table $0($1)", name,
            flds_of.apply(defs).tupleS().map(e -> e._1 + " " + e._2).collect(Collectors.joining(","))); // 创建sql

    final DataApp dataMain;
}
