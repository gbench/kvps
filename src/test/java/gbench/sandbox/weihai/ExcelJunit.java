package gbench.sandbox.weihai;

import static gbench.util.io.Output.println;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import gbench.util.data.xls.SimpleExcel;
import gbench.util.data.xls.StrMatrix;
import gbench.util.lisp.IRecord;

public class ExcelJunit {

    @Test
    public void foo() {
        final SimpleExcel excel = SimpleExcel.of("C:\\Users\\xuqinghua\\Desktop\\项目配置.xlsx");
        excel.sheets().forEach(e->{
            println(excel.autoDetect(e));
        });
        final StrMatrix mx = excel.autoDetect(0);
        println(mx.keys());
        println(mx.rows().get(0));
        println(mx);
        println(mx.transpose());
        println(mx.colS(e->e.map2(Arrays::asList)).collect(IRecord.recclc()));
        excel.close();
    }

}
