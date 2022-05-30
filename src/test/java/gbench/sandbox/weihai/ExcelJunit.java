package gbench.sandbox.weihai;

import static gbench.util.io.Output.println;

import org.junit.jupiter.api.Test;

import gbench.util.data.xls.SimpleExcel;
import gbench.util.data.xls.StrMatrix;

public class ExcelJunit {

    @Test
    public void foo() {
        final SimpleExcel excel = SimpleExcel.of("C:\\Users\\xuqinghua\\Desktop\\项目配置.xlsx");
        final StrMatrix mm = excel.autoDetect(0);
        println(mm.header());
        println(mm.rows().get(0));
        println(mm);
        println(mm.transpose());
        excel.close();
    }

}
