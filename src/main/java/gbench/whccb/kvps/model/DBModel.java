package gbench.whccb.kvps.model;

import static gbench.util.io.Output.println;
import static gbench.util.lisp.IRecord.FT;
import static gbench.util.lisp.IRecord.REC;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gbench.util.data.DataApp;
import gbench.util.data.xls.SimpleExcel;
import gbench.util.lisp.DFrame;
import gbench.util.lisp.IRecord;
import gbench.util.lisp.Tuple2;

@Component
public class DBModel {

    /**
     * 数据库模型
     *
     * @param ds 数据源
     */
    public DBModel(final DataSource ds,
                   @Value("${excel.data.devops.file:E:/slicee/ws/gitws/kvps/src/test/java/gbench/sandbox/weihai/data/devops_data.xlsx}") String excel_data_devops_file) {
        dataMain = new DataApp(ds);
        this.initalize(excel_data_devops_file);
    }

    /**
     * 基础数据的准备
     */
    public void initalize(final String excel_data_devops_file) {
        dataMain.withTransaction(sess -> {
            // 创建用户表
            final IRecord proj_proto = REC("id", 1, "name", "项目1");
            final String create_table_proj = table_of.apply("T_PROJ", proj_proto);
            sess.sql2execute(create_table_proj); // 创建用户表
            for (int i = 0; i < 10; i++) {
                final String t = "insert into T_PROJ(name) values ('$0')";
                final String insert_sql_proj = FT(t, "PROJ00" + i);
                sess.sql2executeS(insert_sql_proj).collect(DFrame.dfmclc(IRecord::REC));
            }

            final String[] roles = "项目经理,技术经理,开发,测试".split("[,]+");
            for (final DataApp.IRecord r : sess.sql2x("select * from T_PROJ")) {
                if (!sess.isTablePresent("T_USER")) {
                    // 创建用户表
                    final IRecord user_proto = REC("id", 1, "name", "zhangsan", "sex", true, "role", "项目经理", "proj_id",
                            r.str("ID"));
                    final String create_table_user = table_of.apply("T_USER", user_proto);
                    sess.sql2execute(create_table_user); // 创建用户表
                }
                for (int i = 0; i < 100; i++) {
                    final String t = "insert into T_USER(name,sex,role,proj_id) values ('$0',$1,'$2',$3)";
                    final String insert_sql_user = FT(t, r.str("NAME") + "-zhang" + i, i % 2 == 0,
                            roles[i % roles.length], r.str("ID"));
                    sess.sql2execute(insert_sql_user);
                } // for
            }

            println(sess.sql2x("select * from T_USER"));
        }); // withTransaction

        // 加载devops数据
        if (excel_data_devops_file != null && new File(excel_data_devops_file).exists()) {
            this.loadXlsx(excel_data_devops_file);
        } else {
            println(IRecord.FT("文件:'$0' 不存在!",excel_data_devops_file));
        }
    }

    /**
     * 查看项目的人员的名单
     *
     * @param proj_key 项目主键
     * @return 人员名单, [{id,name}]
     */
    public List<Map<String, Object>> teamGroup(final String proj_key) {
        return this.dataMain.sql2dframe("select * from T_USER") //
                .rowS().map(e -> e.toMap()).collect(Collectors.toList());
    }

    /**
     * @param path
     */
    public void loadXlsx(final String path) {
        final SimpleExcel excel = SimpleExcel.of(path);
        excel.sheetS().forEach(sht -> {
            final DFrame dfm = excel.autoDetect(sht).mapByRow(IRecord::REC).collect(DFrame.dfmclc);
            final String tblname = sht.getSheetName();
            final IRecord proto = dfm.row(0);
            final String create_table_sql = table_of.apply(tblname, proto);
            final BiFunction<String, IRecord, String> insert_into_of = (tbl, rec) -> {
                final StringBuilder builder = new StringBuilder();
                builder.append(IRecord.FT("insert into $0 ($1)", tblname,
                        proto.keyS().map(e -> e).collect(Collectors.joining(","))));
                builder.append(IRecord.FT(" values ( $0) ",
                        proto.valueS().map(e -> "'" + e + "'").collect(Collectors.joining(","))));
                return builder.toString();
            };

            dataMain.withTransaction(sess -> {
                println(create_table_sql);
                println(insert_into_of.apply(tblname, proto));
                sess.sql2execute(create_table_sql);
                for (IRecord r : dfm) {
                    sess.sql2execute(insert_into_of.apply(tblname, r));
                }
                println(sess.sql2x(IRecord.FT("select * from $0", tblname)));
            });

        });
        excel.close();
    }

    final static IRecord java2sql = REC( //
            "*Integer", "INT PRIMARY KEY AUTO_INCREMENT" //
            , "Integer", "INT" //
            , "Double", "DOUBLE" //
            , "String", "VARCHAR(512)" //
            , "Boolean", "BOOLEAN" //
            , "LocalDateTime", "TIMESTAMP" //
            , "LocalDate", "TIMESTAMP" //
            , "LocalTIME", "TIMESTAMP" //
    ); // java 的类型转 sql的类型
    final static Function<IRecord, IRecord> flds_of = rec -> rec.tupleS().map(Tuple2.snb(0))
            .map(tup -> tup._2.map2(o -> (tup._1 == 0 ? "*" : "") + o.getClass().getSimpleName())) // 主键类型的判断
            .collect(IRecord.recclc(e -> e.map2(s -> java2sql.strOpt(s).orElse("VARCHAR(512)")))); // 提取字段类型信息
    final static BiFunction<String, IRecord, String> table_of = (name, defs) -> FT("create table $0($1)", name,
            flds_of.apply(defs).tupleS().map(e -> e._1 + " " + e._2).collect(Collectors.joining(","))); // 创建sql

    final DataApp dataMain;
}
