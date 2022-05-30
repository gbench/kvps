package gbench.sandbox.weihai;

import static gbench.util.io.Output.println;

import java.sql.Driver;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.*;
import org.junit.jupiter.api.Test;

import gbench.util.data.DataApp;
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
        dataSource.setPassword(rec.str("passowrd"));
        return dataSource;
    }

    @Test
    public void foo() {
        final IRecord rec = IRecord.REC("url", "jdbc:h2:mem:activitidb;MODE=MYSQL;DB_CLOSE_DELAY=-1", "driver",
                "org.h2.Driver", "user", "sa", "password", "");
        final DataSource ds = ds(rec);
        final DataApp dataMain = new DataApp(ds);
        final boolean b = dataMain.tblExists("t_registry");
        dataMain.withTransaction(sess -> {
            sess.sql2execute("create table t_registry(id int,name varchar(256))");
            for (int i = 0; i < 100; i++) {
                final String sql = IRecord.FT("insert into t_registry values ($0,'$1') ", i, "zhang" + i);
                sess.sql2execute(sql);
            }
            println(sess.sql2x("select * from t_registry"));
        });
        println(b);
    }

}
