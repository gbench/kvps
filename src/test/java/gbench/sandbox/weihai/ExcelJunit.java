package gbench.sandbox.weihai;

import static gbench.util.io.Output.println;

import java.sql.Driver;
import java.util.Arrays;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import gbench.util.data.xls.SimpleExcel;
import gbench.util.data.xls.StrMatrix;
import gbench.util.lisp.IRecord;
import gbench.whccb.kvps.model.DBModel;

public class ExcelJunit {

    @Test
    public void foo() {
        final SimpleExcel excel = SimpleExcel.of("C:/Users/xuqinghua/Desktop/项目配置.xlsx");
        excel.sheets().forEach(e -> {
            println(excel.autoDetect(e));
        });
        final StrMatrix mx = excel.autoDetect(0);
        println(mx.keys());
        println(mx.rows().get(0));
        println(mx);
        println(mx.transpose());
        println(mx.colS(e -> e.map2(Arrays::asList)).collect(IRecord.recclc()));
        excel.close();
    }
    
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

 
        
    
    /**
     * 
     */
    @Test
    public void bar() {
        
        final IRecord rec = IRecord.REC("url", "jdbc:h2:mem:kvps;MODE=MYSQL;DB_CLOSE_DELAY=-1", "driver",
                "org.h2.Driver", "user", "sa", "password", "");
        final DataSource ds = ds(rec);
        @SuppressWarnings("unused")
        final DBModel dbModel = new DBModel(ds,"E:/slicee/ws/gitws/kvps/src/test/java/gbench/sandbox/weihai/data/devops_data.xlsx");
    }

}
