package gbench.sandbox.weihai;

import static gbench.util.io.Output.println;
import static gbench.util.lisp.DFrame.dfmclc;
import static gbench.util.lisp.IRecord.FT;
import static gbench.util.lisp.IRecord.REC;

import java.sql.Driver;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;
import org.springframework.jdbc.datasource.*;
import org.junit.jupiter.api.Test;

import gbench.util.data.DataApp;
import gbench.util.lisp.DFrame;
import gbench.util.lisp.IRecord;

/**
 * 
 * @author xuqinghua
 *
 */
public class DataJunit {

    /*
     * 配置数据源
     * 
     * @param rec {url,driver,user,password}
     * 
     * @return DataSource
     */
    @SuppressWarnings("unchecked")
    public DataSource ds(final IRecord rec) {
        final SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setUrl(rec.str("url"));
        try {
            Class<Driver> driverClass = (Class<Driver>) Class.forName(rec.str("driver"));
            dataSource.setDriverClass(driverClass);
        } catch (Exception e) {
            e.printStackTrace();
        }
        dataSource.setUsername(rec.str("user"));
        dataSource.setPassword(rec.str("password"));
        return dataSource;
    }

    @Test
    public void foo() {
        final IRecord rec = IRecord.REC("url", "jdbc:h2:mem:kvps;MODE=MYSQL;DB_CLOSE_DELAY=-1", "driver",
                "org.h2.Driver", "user", "sa", "password", "");
        final DataSource ds = ds(rec);
        final DataApp dataMain = new DataApp(ds);
        final IRecord java2sql = REC( //
                "Integer", "INT" //
                , "Double", "DOUBLE" //
                , "String", "VARCHAR(512)" //
                , "Boolean", "BOOLEAN" //
                , "LocalDateTime", "TIMESTAMP" //
                , "LocalDate", "TIMESTAMP" //
                , "LocalTIME", "TIMESTAMP" //
        ); // java 的类型转 sql的类型
        final Function<IRecord, IRecord> flds_of = p -> p.tupleS().map(e -> e.map2(o -> o.getClass().getSimpleName()))
                .collect(IRecord.recclc(e -> e.map2(s -> java2sql.strOpt(s).orElse("VARCHAR(512)")))); // 提取字段类型信息
        final BiFunction<String, IRecord, String> table_of = (name, defs) -> FT("create table $0($1)", name,
                flds_of.apply(defs).tupleS().map(e -> e._1 + " " + e._2).collect(Collectors.joining(","))); // 创建sql

        // 数据使用演示
        dataMain.withTransaction(sess -> {
            sess.sql2execute(table_of.apply("t_user", REC("id", 1, "name", "zhangsan", "sex", true)));
            for (int i = 0; i < 100; i++) {
                final String sql = FT("insert into t_user values ($0,'$1',$2) ", i, "zhang" + i, i % 2 == 0);
                sess.sql2execute(sql);
            }
            println(sess.sql2x("select * from t_user where sex=false"));
        });

        //
        Stream.of("t_user", "T_USER").forEach(e -> {
            println(e, dataMain.tblExists(e));
        });

    }

    @Test
    public void bar() {
        final DataSource ds = ds(REC( //
                "url", "jdbc:mysql://localhost:3306/activitidb", //
                "driver", "com.mysql.cj.jdbc.Driver", //
                "user", "root", "password", "123456"));
        final DataApp dataMain = new DataApp(ds);
        dataMain.withTransaction(sess -> {
            final DFrame dfm = sess.sql2recordS("show tables").collect(DFrame.dfmclc(IRecord::REC));
            println(dfm);
        });
    }

    @Test
    public void qux() {
        final DataSource ds = ds(REC( //
                "url", "jdbc:mysql://localhost:3306/itsm_whbank", //
                "driver", "com.mysql.cj.jdbc.Driver", //
                "user", "root", "password", "123456"));
        final DataApp dataMain = new DataApp(ds);
        dataMain.withTransaction(sess -> {
            final String sql = "select * from itsm_sys_user";
            final DFrame dfm = sess.sql2recordS(sql).collect(dfmclc(IRecord::REC));
            println(dfm);
        });
    }

}
