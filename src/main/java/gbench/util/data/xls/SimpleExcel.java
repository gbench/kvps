package gbench.util.data.xls;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import gbench.util.io.FileSystem;
import gbench.util.lisp.Tuple2;

/**
 * 
 * @author xuqinghua
 *
 */
public class SimpleExcel implements AutoCloseable {

    /**
     * 简单的EXCEL, try to load a excel file
     * 
     * @param path 文件路径
     * @return SimpleExcel 对象本身 以实现链式编程
     */
    public SimpleExcel(String path) {
        this.load(path);
    }

    /**
     * 简单的EXCEL, try to load a excel file
     * 
     * @param excelfile excel文件对象
     * @return SimpleExcel 对象本身 以实现链式编程
     */
    public SimpleExcel(final File excelfile) {
        this.load(excelfile, true);
    }

    /**
     * 简单的EXCEL, try to load a excel file
     * 
     * @param inputStream 数据流
     * @param extension   文件扩展名 xls,xlsx,或者 xlsm等等。
     * @return SimpleExcel 对象本身 以实现链式编程
     */
    public SimpleExcel(final InputStream inputStream, final String extension) {
        if (extension == null || !extension.trim().toLowerCase().startsWith("xls")) {
            println("扩展名不正确，为null 或是 没有以xls开头");
        }
        this.loadWithExtension(inputStream, extension);
    }

    /**
     * 加载一个EXCEL的文件
     * 
     * @param path excel文件路径
     * @return SimpleExcel 对象本身 以实现链式编程
     */
    public SimpleExcel load(final String path) {
        return load(path, true);
    }

    /**
     * 加载一个EXCEL的文件
     * 
     * @param path     excel文件的绝对路径
     * @param readonly 是否以只读模式加载
     * @return SimpleExcel 对象本身 以实现链式编程
     */
    public SimpleExcel load(final String path, final boolean readonly) {
        final File file = new File(path);
        return this.load(file, readonly);
    }

    /**
     * 不支持读写的并存
     * 
     * @param excelfile excel文件对象
     * @param readonly  是否只读
     * @return SimpleExcel 对象本身 以实现链式编程
     */
    public SimpleExcel load(final File excelfile, final boolean readonly) {
        try {
            if (this.workbook != null) {
                this.workbook.close();
            }

            final String fullpath = excelfile.getAbsolutePath();
            final String ext = FileSystem.extensionpicker(fullpath).toLowerCase();

            try {
                if (!ext.equals("xlsx") && !ext.equals("xls"))
                    throw new Exception("数据格式错误,文件需要xls 或者 xlsx");
            } catch (Exception e) {
                e.printStackTrace();
                return this;
            } // try

            if (excelfile.exists() && readonly) {// 文件只读
                workbook = ext.equals("xlsx") ? new XSSFWorkbook(fullpath)
                        : new HSSFWorkbook(new FileInputStream(excelfile));
            } else {
                workbook = ext.equals("xlsx") ? new XSSFWorkbook() : new HSSFWorkbook();
            } // if

            evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            this.xlsfile = excelfile;
        } catch (IOException e) {
            e.printStackTrace();
        } // try

        return this;
    }

    /**
     * 带扩展名的 资源加载
     * 
     * @param inputStream 文件对象
     * @param ext         扩展名,excel文件类型扩展名,如果为null，默认为xlsx
     * @param readonly    是否只读
     * @return SimpleExcel 对象本身 以实现链式编程
     */
    public SimpleExcel loadWithExtension(InputStream inputStream, String ext, boolean readonly) {
        try {
            final String final_ext = ext == null ? "xlsx" : ext;
            if (this.workbook != null)
                this.workbook.close();

            if (readonly) {// 文件只读
                workbook = final_ext.equals("xlsx") ? new XSSFWorkbook(inputStream) : new HSSFWorkbook(inputStream);
            } else {
                workbook = final_ext.equals("xlsx") ? new XSSFWorkbook() : new HSSFWorkbook();
            }
            evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            this.xlsfile = File.createTempFile("xlsfile-" + System.nanoTime(), ext);
        } catch (IOException e) {
            e.printStackTrace();
        } // try
        return this;
    }

    /**
     * 带扩展名的 资源加载:只读加载
     * 
     * @param inputStream 文件对象
     * @param ext         扩展名,excel文件类型扩展名,如果为null，默认为xlsx
     * @return SimpleExcel 对象本身 以实现链式编程
     */
    public SimpleExcel loadWithExtension(final InputStream inputStream, final String ext) {
        return this.loadWithExtension(inputStream, ext, true);
    }

    /**
     * 采用 区域遍历法在 [firstRowIndex,maxSize)的范围内遍历获得出有包含有有效数据的单元格，其地址索引为最小（最上，最左） 左上
     * 单元的位置，可能为空.left top .<br>
     * 
     * @param sht           表单名称
     * @param firstRowIndex 首行索引；从0开始，包含
     * @param maxSize       遍历行索引的上限。不包含
     * @return 左上顶点的为cell单元格地址
     */
    public Tuple2<Integer, Integer> lt(final Sheet sht, final Integer firstRowIndex, final Integer maxSize) {
        if (sht == null)
            return null;
        int c1 = Integer.MAX_VALUE;
        int c2 = Integer.MAX_VALUE;

        for (int i = firstRowIndex; i < maxSize; i++) {
            final Row row = sht.getRow(i);
            if (row == null)
                continue;
            if (row.getPhysicalNumberOfCells() < 1)
                continue;
            if (c1 == Integer.MAX_VALUE)
                c1 = i;
            if (row.getFirstCellNum() < c2)
                c2 = row.getFirstCellNum();
        }
        if (c1 == Integer.MAX_VALUE || c2 == Integer.MAX_VALUE)
            return null;
        // println("lt:"+c1+","+c2);
        return new Tuple2<>(c1, c2);
    }

    /**
     * 采用 区域遍历法在 [firstRowIndex,maxSize)的范围内遍历获得出有包含有有效数据的单元格，其地址索引为最大（最上，最左）<br>
     * 右下 right botttom<br>
     * 
     * @param sht           excel的表单 sheet对象
     * @param firstRowIndex 首行索引；从0开始，包含
     * @param maxSize       遍历行索引的上限。不包含
     * @return 右下顶点的为cell单元格地址
     */
    public Tuple2<Integer, Integer> rb(final Sheet sht, final Integer firstRowIndex, final Integer maxSize) {
        int c1 = Integer.MIN_VALUE;
        int c2 = Integer.MIN_VALUE;
        if (sht == null)
            return null;

        for (int i = firstRowIndex; i < maxSize; i++) {
            final Row row = sht.getRow(i);
            if (row == null)
                continue;
            if (row.getPhysicalNumberOfCells() < 1)
                continue;
            c1 = i;
            if (row.getLastCellNum() > c2)
                c2 = row.getLastCellNum() - 1;
        } // for

        if (c1 == Integer.MIN_VALUE)
            return null;
        return new Tuple2<>(c1, c2);
    }

    /**
     * 自动定位数据位置 <br>
     * 读取指定sht 的最大可用状态
     * 
     * @param sht           表单对象
     * @param firstRowIndex 首行索引从0开始
     * @param maxSize       检索范围边界
     * @return 数据矩阵
     */
    public StrMatrix autoDetect(final Sheet sht, final Integer firstRowIndex, final Integer maxSize) {
        final Tuple2<Integer, Integer> lt = this.lt(sht, firstRowIndex, maxSize);
        final Tuple2<Integer, Integer> rb = this.rb(sht, firstRowIndex, maxSize);
        final String rangeName = ltrb2rngname(lt, rb);// 转换成rangename
        // println(rangeName);
        return this.range(sht, parse(rangeName));
    }

    /**
     * 自动定位数据位置<br>
     * 读取指定sht 的最大可用状态
     * 
     * @param sht 表单对象
     * @return 数据矩阵
     */
    public StrMatrix autoDetect(final Sheet sht) {
        return this.autoDetect(sht, 0, MAX_SIZE);
    }

    /**
     * 读取指定sht 的最大可用状态 <br>
     * 默认数据首行为标题：
     * 
     * @param shtid   sheetid 编号从0开始
     * @param maxSize 检索范围边界
     * @return 可用的 sheetid 页面的数据区域
     */
    public StrMatrix autoDetect(int shtid, Integer firstRowIndex, Integer maxSize) {
        if (shtid >= this.sheets().size())
            return null;
        return autoDetect(this.sheet(shtid), firstRowIndex, maxSize);
    }

    /**
     * 读取指定sht 的最大可用状态<br>
     * 默认数据首行为标题：<br>
     * 
     * @param sheetname     表单sheet名称
     * @param firstRowIndex 首行索引号 从0开始
     * @param maxSize       检索范围边界
     * @return 可用的 sheetname 页面的数据区域
     */
    public StrMatrix autoDetect(final String sheetname, final Integer firstRowIndex, final Integer maxSize) {
        final int shtid = this.sheetname2shtid(sheetname);
        if (shtid < 0) {
            println(sheetname + " sheet,不存在！");
            return null;
        } // if
        return autoDetect(this.sheet(shtid), firstRowIndex, maxSize);
    }

    /**
     * 读取指定sht 的最大可用状态 <br>
     * 默认数据首行为标题：<br>
     * 
     * @param sheetname 表单sheet名称
     * @param maxSize   检索范围边界
     * @return 可用的 sheetname 页面的数据区域
     */
    public StrMatrix autoDetect(final String sheetname, final Integer maxSize) {
        return this.autoDetect(sheetname, 0, maxSize);
    }

    /**
     * 自动检测sheetname中的数据区域<br>
     * 
     * @param sheetname 表单sheet名称
     * @return 可用的 sheetname 页面的数据区域
     */
    public StrMatrix autoDetect(final String sheetname) {
        return this.autoDetect(sheetname, 0, MAX_SIZE);
    }

    /**
     * 自动检测sheetname中的数据区域<br>
     * 默认数据首行为标题：<br>
     * 
     * @param shtid   表单id 编号从0开始
     * @param maxSize 检索范围边界
     * @return 可用的 sheetid 页面的数据区域
     */
    public StrMatrix autoDetect(final int shtid, final Integer maxSize) {
        return this.autoDetect(shtid, 0, maxSize);
    }

    /**
     * 自动检测sheetname中的数据区域<br>
     * 默认数据首行为标题：<br>
     * 
     * @param shtid 表单sheet编号从0开始
     * @return 可用的 sheetid 页面的数据区域
     */
    public StrMatrix autoDetect(final int shtid) {
        return this.autoDetect(shtid, 0, MAX_SIZE);
    }

    /**
     * 从excel中读取数据矩阵<br>
     * 
     * @param <U>    目标矩阵的元素类型
     * @param shtid  表单sheet编号从0开始
     * @param mapper 值变换函数
     * @return DataMatrix &lt;U&gt;
     */
    public <U> DataMatrix<U> autoDetect(final int shtid, final Function<String, U> mapper) {
        return this.autoDetect(shtid, 0, MAX_SIZE).corece(mapper);
    }

    /**
     * 从excel中读取数据矩阵<br>
     * 
     * @param <U>     目标矩阵的元素类型
     * @param shtname sheet的名称
     * @param mapper  值变换函数
     * @return DataMatrix &lt;U&gt;
     */
    public <U> DataMatrix<U> autoDetect(final String shtname, final Function<String, U> mapper) {
        return this.autoDetect(shtname, 0, MAX_SIZE).corece(mapper);
    }

    /**
     * 按照行进行数据变换 <br>
     * 自动定位数据位置<br>
     * 读取指定sht 的最大可用状态
     * 
     * @param <U>    目标类型
     * @param sht    表单对象
     * @param mapper 值变换函数 row->u
     * @return U类型的数据流
     */
    public <U> Stream<U> autoDetectS(final Sheet sht, final Function<LinkedHashMap<String, String>, U> mapper) {
        return this.autoDetect(sht).mapByRow(mapper);
    }

    /**
     * 按照行进行数据变换 <br>
     * 自动定位数据位置<br>
     * 读取指定sht 的最大可用状态
     * 
     * @param <U>    目标类型
     * @param shtid  表单引号,从0开始
     * @param mapper 值变换函数 row->u
     * @returnU类型的数据流
     */
    public <U> Stream<U> autoDetectS(final int shtid, final Function<LinkedHashMap<String, String>, U> mapper) {
        return this.autoDetect(shtid).mapByRow(mapper);
    }

    /**
     * 按照行进行数据变换 <br>
     * 自动定位数据位置<br>
     * 读取指定sht 的最大可用状态
     * 
     * @param <U>     目标类型
     * @param shtname 表单名称
     * @param mapper  值变换函数 row->u
     * @return U类型的数据流
     */
    public <U> Stream<U> autoDetectS(final String shtname, final Function<LinkedHashMap<String, String>, U> mapper) {
        return this.autoDetect(shtname).mapByRow(mapper);
    }

    /**
     * 默认表头 读取指定区域的数据内容
     * 
     * @param rangedef 数据区域
     * @param sht      表单对象
     * @return 数据区域的数据内容
     */
    public StrMatrix range(final Sheet sht, final RangeDef rangedef) {
        return range(sht, rangedef, null);
    }

    /**
     * 读取指定区域的数据内容
     * 
     * @param sht      表单对象
     * @param rangedef 数据区域
     * @param hh       表头列表,null表示数据中包含表头,第一行数据作为表头列表。
     * @return 数据区域的数据内容
     */
    public StrMatrix range(Sheet sht, RangeDef rangedef, List<String> hh) {
        if (rangedef == null) {
            println("无法获得rangedef数据,rangedef 为空数据");
            return null;
        }

        final DataMatrix<String> cc = this.evaluate(sht, rangedef.x0(), rangedef.y0(), rangedef.x1(), rangedef.y1(), hh,
                e -> { // 数字类型 的值转换
                    if (e instanceof Number) { // 数值类型
                        final Double dbl = ((Number) e).doubleValue();
                        final Long lng = ((Number) e).longValue();
                        if (Math.abs(dbl - lng) < SimpleExcel.EPSILON) // 浮点数与整形误差小于 误差容忍限度 EPSILON ，采用 整数表述
                            return lng + ""; // 长整形
                        else
                            return dbl + ""; // 浮点数
                    } else { // 其他类型
                        return e + "";
                    } // if
                });
        if (cc == null)
            return null;
        final String cells[][] = cc.data();
        final List<String> headers = cc.keys();
        return new StrMatrix(cells, headers);
    }

    /**
     * 名称转id
     * 
     * @param name sheet 名称 默认数据首行为标题：
     * @return 把 sheet名称转换成sheetdi
     */
    public Integer sheetname2shtid(String name) {
        return this.sheets().stream().map(Sheet::getSheetName).collect(Collectors.toList()).indexOf(name);
    }

    /**
     * 选择哪些列数据 <br>
     * 这有点类似于 select mapper(hh) from name 这样的数据操作。 <br>
     * 
     * 对于有 name 进行标识的excel中的区域给予计算求求职 <br>
     * 由于name标识的区域是一个 数据框，所以可以通过 在 hh 中指定列名的形式对于每个元素a[i,j]应用mapper 函数，进而得到一个新的矩阵
     * <br>
     * c1 c2 c3 :列名 ----> ci cj ck [i,j,k是 一个c1,c2,c3...的子集] <br>
     * a11 a12 a13 :行数据 ----> b1i b1j b1k <br>
     * a21 a22 a23 :行数据 ----> b2i b2j b2k <br>
     * ... ... ... ----> ... ... ... <br>
     * 
     * @param sht       sheet 名
     * @param rangeName 区域名称
     * @param hh        表头名称
     * @param mapper    数据变换操作: o->t 完成 aij->bij的变换
     * @return 新数据矩阵
     */
    public <T> DataMatrix<T> evaluate(final Sheet sht, final String rangeName, final List<String> hh,
            final Function<Object, T> mapper) {
        return this.evaluate(sht, parse(rangeName), hh, mapper);

    }

    /**
     * 选择哪些列数据 <br>
     * 这有点类似于 select mapper(hh) from name 这样的数据操作。 <br>
     * 
     * 对于有 name 进行标识的excel中的区域给予计算求求职 <br>
     * 由于name标识的区域是一个 数据框，所以可以通过 在 hh 中指定列名的形式对于每个元素a[i,j]应用mapper 函数，进而得到一个新的矩阵
     * <br>
     * c1 c2 c3 :列名 ----> ci cj ck [i,j,k是 一个c1,c2,c3...的子集] <br>
     * a11 a12 a13 :行数据 ----> b1i b1j b1k <br>
     * a21 a22 a23 :行数据 ----> b2i b2j b2k <br>
     * ... ... ... ----> ... ... ... <br>
     * 
     * @param sht       表单名
     * @param rangeName 区域名称
     * @param mapper    数据变换操作: o->t 完成 aij->bij的变换
     * @return 新数据矩阵
     */
    public <T> DataMatrix<T> evaluate(final Sheet sht, final String rangeName, final Function<Object, T> mapper) {
        return this.evaluate(sht, parse(rangeName), null, mapper);
    }

    /**
     * 选择哪些列数据 <br>
     * 这有点类似于 select mapper(hh) from name 这样的数据操作。 <br>
     * 
     * 对于有 name 进行标识的excel中的区域给予计算求求职 <br>
     * 由于name标识的区域是一个 数据框，所以可以通过 在 hh 中指定列名的形式对于每个元素a[i,j]应用mapper 函数，进而得到一个新的矩阵
     * <br>
     * c1 c2 c3 :列名 ----> ci cj ck [i,j,k是 一个c1,c2,c3...的子集] <br>
     * a11 a12 a13 :行数据 ----> b1i b1j b1k <br>
     * a21 a22 a23 :行数据 ----> b2i b2j b2k <br>
     * ... ... ... ----> ... ... ... <br>
     * 
     * @param name   区域全名称比如sheet2!A1:B100
     * @param hh     列名序列,即选择那些列数据 这就点类似于 select mapper(hh) from name 这样的数据操作。
     * @param mapper 数据变换操作: o->t 完成 aij->bij的变换
     * @return 重新计算后的新的数据矩阵
     */
    public <T> DataMatrix<T> evaluate(final String name, final List<String> hh, final Function<Object, T> mapper) {
        String names[] = name.split("[!]+");// 多个！视为一个
        Sheet sht = this.sheet(0);
        String rangeName = name;
        // 默认为第一个sheet的区域名
        if (names.length >= 2) {
            sht = this.sheet(this.shtid(names[0]));// 获取sheetid
            rangeName = names[1];// 选区第二项目作为区域名称
        } // 选区第二项目作为区域名称
        return this.evaluate(sht, parse(rangeName), hh, mapper);
    }

    /**
     * 选择哪些列数据 <br>
     * 这有点类似于 select mapper(hh) from name 这样的数据操作。 <br>
     * 
     * 对于有 name 进行标识的excel中的区域给予计算求求职 <br>
     * 由于name标识的区域是一个 数据框，所以可以通过 在 hh 中指定列名的形式对于每个元素a[i,j]应用mapper 函数，进而得到一个新的矩阵
     * <br>
     * c1 c2 c3 :列名 ----> ci cj ck [i,j,k是 一个c1,c2,c3...的子集] <br>
     * a11 a12 a13 :行数据 ----> b1i b1j b1k <br>
     * a21 a22 a23 :行数据 ----> b2i b2j b2k <br>
     * ... ... ... ----> ... ... ... <br>
     * 
     * @param name   区域全名称比如sheet2!A1:B100
     * @param mapper 数据变换操作: o->t 完成 aij->bij的变换
     * @return 重新计算后的新的数据矩阵
     */
    public <T> DataMatrix<T> evaluate(final String name, final Function<Object, T> mapper) {
        return this.evaluate(name, null, mapper);
    }

    /**
     * 选择哪些列数据 <br>
     * 这有点类似于 select mapper(hh) from name 这样的数据操作。 <br>
     * 
     * 对于有 name 进行标识的excel中的区域给予计算求求职 <br>
     * 由于name标识的区域是一个 数据框，所以可以通过 在 hh 中指定列名的形式对于每个元素a[i,j]应用mapper 函数，进而得到一个新的矩阵
     * <br>
     * c1 c2 c3 :列名 ----> ci cj ck [i,j,k是 一个c1,c2,c3...的子集] <br>
     * a11 a12 a13 :行数据 ----> b1i b1j b1k <br>
     * a21 a22 a23 :行数据 ----> b2i b2j b2k <br>
     * ... ... ... ----> ... ... ... <br>
     * 
     * @param sht    表单数据
     * @param mapper 数据变换操作: o->t 完成 aij->bij的变换
     * @param hh     null,表示数据中包含表头,第一行就是表头
     * @return 新数据矩阵
     */
    public <T> DataMatrix<T> evaluate(final Sheet sht, final RangeDef rangedef, final List<String> hh,
            final Function<Object, T> mapper) {
        return this.evaluate(sht, rangedef.x0(), rangedef.y0(), rangedef.x1(), rangedef.y1(), hh, mapper);

    }

    /**
     * 选择哪些列数据 <br>
     * 这有点类似于 select mapper(hh) from name 这样的数据操作。 <br>
     * 
     * 对于有 name 进行标识的excel中的区域给予计算求求职 <br>
     * 由于name标识的区域是一个 数据框，所以可以通过 在 hh 中指定列名的形式对于每个元素a[i,j]应用mapper 函数，进而得到一个新的矩阵
     * <br>
     * c1 c2 c3 :列名 ----> ci cj ck [i,j,k是 一个c1,c2,c3...的子集] <br>
     * a11 a12 a13 :行数据 ----> b1i b1j b1k <br>
     * a21 a22 a23 :行数据 ----> b2i b2j b2k <br>
     * ... ... ... ----> ... ... ... <br>
     * 
     * 行范围 从 _i0 开始,包含 _i1结束 <br>
     * 行范围 从 _j0 开始,包含 _j1结束 <br>
     * 
     * @param <T>     数据矩阵的的元素类型
     * @param sht     sht 表单数据
     * @param _i0     行范围 开始位置 ,从 0开始,包含
     * @param _j0     行范围 开始位置,从 0开始,包括
     * @param _i1     列范围 结束位置,包含
     * @param _j1     列范围 结束位置,包含
     * @param _mapper 元素变换函数 即生成T类型的数元素。
     * @return excel 数据矩阵
     */
    @SuppressWarnings("unchecked")
    public <T> DataMatrix<T> evaluate(final Sheet sht, final int _i0, final int _j0, final int _i1, final int _j1,
            final List<String> _hh, final Function<Object, T> _mapper) {

        if (sht == null) {
            println("指定的sheet为空,无法获得表单数据");
            return null;
        }

        List<String> hh = _hh; // 表头对象
        final int i0 = Math.min(_i0, _i1);
        final int i1 = Math.max(_i0, _i1);
        final int j0 = Math.min(_j0, _j1);
        final int j1 = Math.max(_j0, _j1);
        final Function<Object, T> mapper = _mapper == null ? e -> (T) e : _mapper;
        final int offset = hh == null ? 1 : 0;// 数据从哪一行开始
        final Object[][] mm = new Object[(i1 - i0 + 1 - offset)][(j1 - j0 + 1)];

        if (mm.length <= 0 || mm[0] == null || mm[0].length <= 0) {
            println("数据矩阵的行数为空，没有数据！");
            return null;
        }

        final List<String> firstrow = new ArrayList<>(mm[0].length);
        final Set<Class<?>> classes = new HashSet<>();
        for (int i = i0; i <= i1 - offset; i++) { // 数据尾行需要去掉offset部分因为这些跳过了
            for (int j = j0; j <= j1; j++) {// 当hh==null的时候数据要偏移一行
                if (i == i0)
                    firstrow.add(this.strval(sht, i, j));// 记录首行
                Cell c = null;// 单元格
                try {
                    final Row row = sht.getRow(i + offset); // 数据行对象
                    c = (row == null) ? null : row.getCell(j);// 获取产品单元格,注意含有行偏移值：这里是 i+offset
                    final T value = mapper.apply(this.evaluate(c));// 跳过offset行;
                    mm[i - i0][j - j0] = value;// 计算矩阵数值
                    if (value != null)
                        classes.add(value.getClass());
                } catch (Exception e) {
                    e.printStackTrace();
                    println("error on (" + i + "," + j + ")," + (sht.getRow(i) == null ? "行对象:'" + sht.getRow(i) + "为空"
                            : "行对象或者cell单元格异常,请指定有效的EXCEL数据范围（或是EXCEL自行判断的数据范围有错误）！"));
                } // try
            } // for j
        } // for i

        if (hh == null || hh.size() < 1) {// 使用第一行作为表头
            final AtomicInteger ai = new AtomicInteger(0);
            hh = firstrow.stream().map(e -> {
                if (e == null || "null".equals(e) || e.matches("\\s*")) { // 自动生成表头
                    return "_" + DataMatrix.to_excel_name(ai.get()); // 生成动态表头
                }
                ai.incrementAndGet();
                return e;
            }).collect(Collectors.toList());// 默认的第一行作表头
        } else {// 使用指定表头
            final String[] nn = hh.toArray(new String[0]);
            final int size = hh.size();
            hh = Stream.iterate(1, i -> i + 1).limit(mm[0].length)
                    .map(e -> size >= e ? nn[e - 1] : ("_" + DataMatrix.to_excel_name(e))).collect(Collectors.toList());
        }

        Class<?> cls = null;// 获取矩阵的数据分类
        if (classes.size() > 0) {
            cls = classes.iterator().next();
            if (classes.size() > 1) {
                println("warnnings:矩阵中出现不同类别:" + classes + ",取用类别:" + classes.iterator().next());
            } // if
        } // if

        final int m = mm.length; // 行数
        final int n = mm[0].length;// 表列宽度，列数
        final T[][] tt = (T[][]) Array.newInstance((Class<T>) cls, mm.length, mm[0].length); // 数据矩阵

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                try {
                    tt[i][j] = (T) mm[i][j];
                } catch (Exception e) {
                    e.printStackTrace();
                } // try
            } // for j
        } // for i

        return new DataMatrix<>(tt, hh);
    }

    /**
     * 格式化输出结构
     * 
     * @param cell excel表单sheet单元格对象
     * @return 字符串输出
     */
    public Object evaluate(final Cell cell) {
        Object value = null;
        if (cell == null) {
            return null;
        }

        final Function<Cell, String> strcell = c -> { // 元素值的字符串
            final CellStyle style = c.getCellStyle();
            final short count = style.getIndention(); // 缩进数量
            return "\t".repeat(count) + c.getStringCellValue(); // 数据格式化
        };

        final CellType cellType = cell.getCellType();

        if (cellType == CellType.STRING) { // 字符串类型
            value = strcell.apply(cell);
        } else if (cellType == CellType.NUMERIC) { // 数值类型
            if (DateUtil.isCellDateFormatted(cell)) {
                final Date date = cell.getDateCellValue();
                value = sdf.format(date);
            } else { //
                final Double dbl = cell.getNumericCellValue();
                value = dbl;
            } // if
        } else if (cellType == CellType.FORMULA) { // 公式处理
            CellValue vv = null; // 公式的值

            try {
                vv = evaluator.evaluate(cell);// 计算单元格的数值
            } catch (Exception e) {
                // e.printStackTrace();
                value = String.valueOf(cell.getRichStringCellValue());
                return value;
            } // try

            try {
                if (DateUtil.isCellDateFormatted(cell)) {// 日期值处理
                    final Date date = cell.getDateCellValue();
                    value = sdf.format(date);
                } else if (vv.getCellType() == CellType.NUMERIC) { // 数值类型
                    value = vv.getNumberValue();
                } else if (vv.getCellType() == CellType.BOOLEAN) { // BOOL 类型
                } else if (vv.getCellType() == CellType.ERROR) { // 错误类型
                    value = vv.getErrorValue();
                } else { // default 默认类型
                    value = vv.getStringValue();
                } // 值类型的处理

            } catch (IllegalStateException e) { // 默认采用时间格式
                value = strcell.apply(cell);
            } // try

        } else if (cellType == CellType.NUMERIC) {// 数值得处理
            if (DateUtil.isCellDateFormatted(cell)) {
                value = sdf.format(cell.getDateCellValue());
            } else {
                value = cell.getNumericCellValue();
            }
        } else if (cellType == CellType.BOOLEAN) {// 布尔值得处理
            value = cell.getBooleanCellValue();
        } else { // 默认值类型
            // do nothin so value == null
        }

        // println(value);
        return value;
    }

    /**
     * 根据sheet 的名称确定sheet的编号
     * 
     * @param name 表单sheet的名称
     * @return name 对应的编号
     */
    public Integer shtid(final String name) {
        int shtid = -1;

        final List<Sheet> shts = sheets();// 获取表单列表
        for (int i = 0; i < sheets().size(); i++) {
            if (shts.get(i) == null) {
                continue;
            }

            String shtname = shts.get(i).getSheetName();
            if (shtname != null && shtname.equals(name)) {
                shtid = i;
                break;
            } // if
        } // forEach

        return shtid;
    }

    /**
     * 书写单元格
     * 
     * @param i 从0开始
     * @param j 从0开始
     */
    public String strval(final int i, final int j) {
        return strval(activesht, i, j);
    }

    /**
     * 书写单元格
     * 
     * @param i   从0开始
     * @param j   从0开始
     * @param sht 当前的表单
     */
    public String strval(final Sheet sht, final int i, final int j) {
        if (i < 0 || j < 0) {
            return null;
        }

        if (sht == null) {
            println("未指定表单,sht==null");
            return null;
        }

        Row row = sht.getRow(i);
        if (row == null) {
            return "";
        }

        final Cell cell = row.getCell(j);
        if (cell == null) {
            return "";
        }

        return format(cell);
    }

    /**
     * 格式化输出结构
     * 
     * @param cell excel的输出结构
     * @return 字符串输出
     */
    public String format(Cell cell) {
        return this.evaluate(cell) + "";
    }

    /**
     * 所有的Sheet 列表
     */
    public List<Sheet> sheets() {
        // 遍历workbook获取sheet 列表
        return this.sheetS().collect(Collectors.toList());
    }

    /**
     * 所有的Sheet 的流
     */
    public Stream<Sheet> sheetS() {
        // 遍历workbook获取sheet 列表
        final Spliterator<Sheet> splitr = Spliterators.spliteratorUnknownSize(workbook.sheetIterator(),
                Spliterator.ORDERED);
        return StreamSupport.stream(splitr, false);
    }

    /**
     * 数据保存
     * 
     * @param filename 保存的文件名
     * @return SimpleExcel 对象本身 以实现链式编程
     */
    public SimpleExcel saveAs(String filename) {
        return this.saveAs(new File(filename));
    }

    /**
     * 保存成文件
     * 
     * @param file excel文件对象,如果file业已存在则给与添加
     * @return SimpleExcel 对象本身 以实现链式编程
     */
    public SimpleExcel saveAs(File file) {
        try (final FileOutputStream fos = new FileOutputStream(file, file.exists())) {
            this.workbook.write(fos);
        } catch (Exception e) {
            e.printStackTrace();
        } // try
        return this;
    }

    /**
     * excel 文件保存
     * 
     * @return SimpleExcel 对象本身 以实现链式编程
     */
    public SimpleExcel save() {
        return this.saveAs(this.xlsfile);
    }

    /**
     * 获取表单数据
     * 
     * @param i 表单id 从0开始
     * @return 表单对象
     */
    public Sheet sheet(final int i) {
        Sheet sht = null;
        try {
            sht = this.workbook.getSheetAt(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sht;
    }

    /**
     * 文件关闭
     */
    @Override
    public void close() {
        try {
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        } // try
    }

    /**
     * 把excel 名称转换成位置坐标
     * 
     * @param rangeName A1:B13这样的字符串
     * @return 名称转rangedef
     */
    public static RangeDef parse(final String rangeName) {
        final String pattern = "(([A-Z]+)\\s*(\\d+))(\\s*:\\s*(([A-Z]+)\\s*(\\d+)))?";
        if (rangeName == null) {
            return null;
        }

        final String name = rangeName.toUpperCase(); // 转换成大写形式
        final Matcher matcher = Pattern.compile(pattern).matcher(name);

        RangeDef rangedef = null;// 数值区域
        if (matcher.find()) {
            final String y0 = matcher.group(2).replaceAll("\\s*", "");
            final String x0 = matcher.group(3).replaceAll("\\s*", "");
            final String y1 = matcher.group(6).replaceAll("\\s*", "");
            final String x1 = matcher.group(7).replaceAll("\\s*", "");
            final Integer ix0 = DataMatrix.excel_name_to_index(x0);
            final Integer iy0 = DataMatrix.excel_name_to_index(y0);
            final Integer ix1 = DataMatrix.excel_name_to_index(x1);
            final Integer iy1 = DataMatrix.excel_name_to_index(y1);

            rangedef = new RangeDef(ix0, iy0, ix1, iy1);
        } // if

        return rangedef; // 数据区域内容
    }

    /**
     * 把坐标索引(左上与右下)转换成 EXCEL 区域名称，比如 (0,0),(1,1) 转换成 A1:B2
     * 
     * @param lt 左上角单元的坐标索引,从0开始:(0,0)表示第一个单元格
     * @param rb 右下角的坐标索引，从0开始:(0,0)表示第一个单元格
     * @return range 的名称
     */
    public static String ltrb2rngname(final Tuple2<Integer, Integer> lt, final Tuple2<Integer, Integer> rb) {
        if (lt == null || rb == null) {
            return null;
        }

        final Tuple2<Integer, Integer> _lt = lt.map1(e -> e + 1);
        final Tuple2<Integer, Integer> _rb = rb.map1(e -> e + 1);
        final String ltaddr = tuple2address(_lt);
        final String rbaddr = tuple2address(_rb);
        return MessageFormat.format("{0}:{1}", ltaddr, rbaddr);
    }

    /**
     * 把一个点索引转换成cell地址名
     * 
     * @param tuple 顶坐标 x(水平),y （垂直）
     * @return cell位置的字符串字符串
     */
    public static String tuple2address(Tuple2<Integer, Integer> tuple) {
        return DataMatrix.to_excel_name(tuple._2) + "" + tuple._1;
    }

    /**
     * SimpleExcel 生成器
     * 
     * @param file excel 文件对象
     * @return SimpleExcel
     */
    public static SimpleExcel of(final File file) {
        return new SimpleExcel(file);
    }

    /**
     * SimpleExcel 生成器
     * 
     * @param path excel 文件路径的绝对路径
     * @return SimpleExcel
     */
    public static SimpleExcel of(final String path) {
        return SimpleExcel.of(new File(path));
    }

    /**
     * 格式化输出
     * 
     * @param objs 数据列表
     * @return 格式化输出
     */
    public static String println(final Object... objs) {
        final String line = Stream.of(objs).map(e -> e + "").collect(Collectors.joining("\n"));
        System.out.println(line);
        return line;
    }

    public static final Integer MAX_SIZE = 1000000;// 最大处理行数
    public static Double EPSILON = 1e-20;// 最小的数字精度
    private Workbook workbook = new XSSFWorkbook();
    private Sheet activesht = null;// 当前的对象
    private final SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
    private static FormulaEvaluator evaluator;
    private File xlsfile = null;

}
