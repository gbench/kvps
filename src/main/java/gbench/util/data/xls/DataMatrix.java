package gbench.util.data.xls;

import java.lang.reflect.Array;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import gbench.util.lisp.Tuple2;

/**
 * 
 * @author xuqinghua
 *
 */
public class DataMatrix<T> {

    /**
     * 数据矩阵
     * 
     * @param cells 数据
     */
    public DataMatrix(final T[][] cells) {
        this(cells, null);
    }

    /**
     * 数据矩阵
     * 
     * @param cells 数据
     * @param hh    表头定义，null 则首行为表头
     */
    public DataMatrix(final T[][] cells, final List<String> hh) {
        this.initialize(cells, hh);
    }

    /**
     * 数据矩阵:使用cells的数据初始化一个数据矩阵,注意当hh为null的时候采用cells的第一行作为数据标题。
     * 
     * @param cells 数据:
     * @param hh    表头定义，null 则首行为表头,对于hh短与width的时候, 使用编号的excel名称,名称用“_”作为开头。
     * @return DataMatrix<T> 对象自身
     */
    public DataMatrix<T> initialize(final T[][] cells, final List<String> hh) {
        this.cells = cells;
        final List<String> final_hh = new LinkedList<>();
        if (hh == null) {// 采用数据的第一行作为表头
            final_hh.addAll(Arrays.stream(cells[0]).map(e -> e + "").collect(Collectors.toList()));
            this.cells = removeFirstLine(cells);
        } else {
            final_hh.addAll(hh);
        }

        final int n = this.width();// 矩阵的列宽,每行的数据长度
        final ListIterator<String> itr = final_hh.listIterator();
        for (int i = 0; i < n; i++) {// 诸列检查
            if (!itr.hasNext()) {// 使用excelname 来进行补充列表的补填。
                itr.add("_" + to_excel_name(i));// 使用默认的excel名称加入一个下划线前缀
            } else {
                itr.next();// 步进到下一个位置
            } // if !itr.hasNext()
        } // for i

        // 设置键名列表
        this.setKeys(final_hh);// 设置表头，表头与键名列表同义

        return this;
    }

    /**
     * 初始化一个空矩阵（元素内容均为null)
     * 
     * @param m         行数
     * @param n         列数
     * @param cellClass 元素类型：null 默认为Object.class
     * @param hh        列名序列,null 使用excelname 进行默认构造
     * @return DataMatrix<T> 对象自身
     */
    @SuppressWarnings("unchecked")
    public DataMatrix<T> initialize(final int m, final int n, Class<T> cellClass, final List<String> hh) {
        final Class<T> finalCellClass = cellClass != null ? cellClass : (Class<T>) Object.class;// 元素类型默认为Object类型
        this.cells = (T[][]) Array.newInstance(finalCellClass, m, n);
        final List<String> final_hh = hh == null
                ? Stream.iterate(0, i -> i + 1).map(DataMatrix::to_excel_name).limit(n).collect(Collectors.toList())
                : hh;

        this.setKeys(final_hh);// 设置表头

        return this;
    }

    /**
     * 矩阵的宽度 ，列数
     * 
     * @return 矩阵的宽度 ，列数
     */
    public int width() {
        return shape(this.cells)._2;
    }

    /**
     * 矩阵高度：行数
     * 
     * @return 行数
     */
    public int height() {
        return shape(this.cells)._1;
    }

    /**
     * 表头，列名字段序列:
     * 
     * @return 返回 数据 的表头，列名序列
     */
    public Stream<String> keyS() {
        return this.keymetas.entrySet().stream().sorted(Comparator.comparingInt(Entry::getValue)).map(Entry::getKey);
    }

    /**
     * 表头，列名字段序列:
     * 
     * @return 返回 数据 的表头，列名序列
     */
    public List<String> keys() {
        return this.keyS().collect(Collectors.toList());
    }

    /**
     * 获取并设置健名（列名）索引
     * 
     * @param keys 列名字段序列:
     * @return Map<String,Integer>
     */
    public Map<String, Integer> setKeys(final List<?> keys) {
        final Map<String, Integer> keysMap = new HashMap<>();
        if (keys == null) {
            final List<String> hh = this.keys();
            for (int i = 0; i < hh.size(); i++) {
                keysMap.put(hh.get(i), i);
            } // for
        } else {
            int i = 0;
            for (Object key : keys) {
                keysMap.put(key + "", i++);
            } // for
            this.setKeymetas(keysMap);
        } // if
        return keysMap;
    }

    /**
     * 行列表
     * 
     * @param mapper 行元素变换器: [t]->{}
     * @return 行列表 集合
     */
    public <U> List<U> rows(final Function<T[], U> mapper) {
        return Arrays.stream(this.cells).map(mapper).collect(Collectors.toList());
    }

    /**
     * 行列表
     * 
     * @return 行列表 集合
     */
    public List<List<T>> rows() {
        return rows(Arrays::asList);
    }

    /**
     * 按照行进行映射
     * 
     * @param <U>    目标行记录{(String,T)} 所变换成的结果类型
     * @param mapper 行数据映射 LinkedHashMap<String,T>的结构, key->value
     * @return U 类型的流
     */
    public <U> Stream<U> rowS(final Function<LinkedHashMap<String, T>, U> mapper) {
        final String[] hh = this.keys().toArray(new String[0]);
        final int hn = hh.length;// 表头长度
        @SuppressWarnings("unchecked")
        Function<LinkedHashMap<String, T>, U> final_mapper = mapper == null ? e -> (U) e : mapper;
        return this.rows().stream().map(row -> {
            int n = row.size();
            LinkedHashMap<String, T> mm = new LinkedHashMap<>();
            for (int i = 0; i < n; i++)
                mm.put(hh[i % hn], row.get(i));
            return final_mapper.apply(mm);
        });// lrows
    }

    /**
     * 按照行进行映射
     * 
     * @param <U>    结果
     * @param mapper 列变换函数 (k,tt)->u
     * @return U 类型的流
     */
    public <U> Stream<U> colS(final Function<Tuple2<String, T[]>, U> mapper) {
        final T[][] tt = DataMatrix.transpose(this.data());
        return this.keyS().map(Tuple2.snb(0)).map(e -> Tuple2.of(e._2, tt[e._1])).map(mapper);
    }

    /**
     * 按照行进行映射
     * 
     * @return U 类型的流
     */
    public Stream<Tuple2<String, T[]>> colS() {
        return this.colS(e -> e);
    }

    /**
     * 按照行进行映射
     * 
     * @return U 类型的流
     */
    public List<Tuple2<String, T[]>> cols() {
        return this.colS().collect(Collectors.toList());
    }

    /**
     * 按照行进行映射:注意区分 mapRows 变换成另外一个 DataMatrix<U>
     * 
     * @param <U>    目标行记录{(String,T)} 所变换成的结果类型
     * @param mapper 行数据映射 LinkedHashMap<String,T>的结构, key->value
     * @return U 类型的流
     */
    public <U> Stream<U> mapByRow(final Function<LinkedHashMap<String, T>, U> mapper) {
        return rowS(mapper);
    }

    /**
     * 设置矩阵的表头字段序列:
     * 
     * @param keymetas {(name,id)}的字段序列
     * @return 设置成功的 表头字段序列：{(name,id)}的字段序列
     */
    public Map<String, Integer> setKeymetas(final Map<String, Integer> keymetas) {
        this.keymetas = keymetas;
        return keymetas;
    }

    /**
     * 格式化输出<br>
     * <br>
     * 垂直方向:第一维度 i 增长方向 从上到下 <br>
     * 水平方向:第二维度 j 增长方向 从左到右 <br>
     * 
     * @return 格式化输出
     */
    public String toString() {
        return toString("\t", "\n", null);
    }

    /**
     * 矩阵转置
     * 
     * @return 矩阵转置
     */
    public DataMatrix<T> transpose() {
        final T[][] dd = DataMatrix.transpose(this.data());
        final Integer n = shape(dd)._2();
        final List<String> hh = st(n).map(DataMatrix::to_excel_name).collect(Collectors.toList());
        return new DataMatrix<T>(dd, hh);
    }

    /**
     * 格式化输出<br>
     * <br>
     * 垂直方向:第一维度 i 增长方向 从上到下 <br>
     * 水平方向:第二维度 j 增长方向 从左到右 <br>
     * 
     * @return 格式化输出
     */
    public String toString(Function<Object, String> cell_formatter) {
        return toString("\t", "\n", cell_formatter);
    }

    /**
     * 矩阵格式化 <br>
     * <br>
     * 垂直方向:第一维度 i 增长方向 从上到下 <br>
     * 水平方向:第二维度 j 增长方向 从左到右 <br>
     * 
     * @param ident          行内间隔
     * @param ln             行间间隔
     * @param cell_formatter 元素格式化
     * @return 格式化输出的字符串
     */
    public String toString(final String ident, final String ln, final Function<Object, String> cell_formatter) {

        final Function<Object, String> final_cell_formatter = cell_formatter == null ? e -> {
            String line = "{0}";// 数据格式字符串
            if (e instanceof Number)
                line = (e instanceof Integer || e instanceof Long) ? "{0,number,#}" : "{0,number,0.##}";
            else if (e instanceof Date) {
                line = "{0,Date,yyy-MM-dd HH:mm:ss}";
            }
            return MessageFormat.format(line, e);
        }// 默认的格式化
                : cell_formatter;

        if (cells == null || cells.length < 1 || cells[0] == null || cells[0].length < 1)
            return "";
        final StringBuilder buffer = new StringBuilder();
        final String headline = String.join(ident, this.keys());
        if (!headline.matches("\\s*"))
            buffer.append(headline).append(ln);

        // 按照维度自然顺序（从小打到):i->j给与展开格式化
        for (T[] cell : cells) { // 第一维度
            final int n = (cell != null && cell.length > 0) ? cell.length : 0;
            for (int j = 0; j < n; j++) { // 第二维度
                buffer.append(final_cell_formatter.apply(cell[j])).append(ident);
            } // for j 水平方向
            buffer.append(ln);
        } // for i 垂直方向

        return buffer.toString(); // 返回格式化数据
    }

    /**
     * 类型装换
     * 
     * @param <U>     目标结果的类型
     * @param corecer 类型变换函数
     * @return 类型变换
     */
    @SuppressWarnings("unchecked")
    public <U> DataMatrix<U> corece(final Function<T, U> corecer) {
        final T[][] cc = this.cells;
        try {
            final int m = this.height();
            final int n = this.width();
            final List<U> ulist = Arrays.stream(cc).flatMap(Arrays::stream).map(corecer).collect(Collectors.toList());// 找到一个非空的数据类型
            final Optional<Class<U>> opt = ulist.stream().filter(Objects::nonNull).findFirst()
                    .map(e -> (Class<U>) e.getClass()); // 提取费控的类型
            final Class<U> cls = opt.orElseGet(() -> (Class<U>) (Object) Object.class);
            final U[][] uu = (U[][]) Array.newInstance(cls, m, n);
            final Iterator<U> itr = ulist.iterator();
            for (int i = 0; i < m; i++)
                for (int j = 0; j < n; j++)
                    uu[i][j] = itr.hasNext() ? itr.next() : null;

            return new DataMatrix<>(uu, this.keys());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } // try
    }

    /**
     * 增加数据预处理函数，只改变数据内容并改变数据形状shape:比如 无效非法值，缺失值，数字格式化等功能。
     * 
     * @param handler 行数据映射 LinkedHashMap<String,T>的结构, key->value
     */
    public DataMatrix<T> preProcess(final Consumer<T[]> handler) {
        for (int i = 0; i < this.height(); i++)
            handler.accept(cells[i]);
        return this;
    }

    /**
     * 矩阵转置
     * 
     * @param <U>   元素类型
     * @param cells 数据矩阵
     * @return U[][]
     */
    public static <U> U[][] transpose(final U[][] cells) {
        return transpose(cells, 1);
    }

    /**
     * 矩阵转置
     * 
     * @param <U>   元素类型
     * @param cells 数据矩阵
     * @param mode  转置模式:0 for,1 stream
     * @return U[][] 转置后的矩阵
     */
    @SuppressWarnings("unchecked")
    public static <U> U[][] transpose(final U[][] cells, long mode) {
        Class<U> u = getGenericClass(cells);
        if (u == null)
            u = (Class<U>) Object.class;
        final Class<U> cellClazz = u;
        if (mode == 0) {// 0 for
            if (cells == null || cells[0] == null)
                return null;
            final int m = cells.length;
            final int n = cells[0].length;
            final U[][] cc = (U[][]) Array.newInstance(cellClazz, n, m);
            for (int i = 0; i < cells.length; i++)
                for (int j = 0; j < cells[0].length; j++)
                    cc[j][i] = cells[i][j];
            return cc;
        } else {// mode 1 stream
            final Class<U[]> rowClazz = Stream.of(cells).filter(Objects::nonNull).map(e -> ((Class<U[]>) e.getClass()))
                    .findFirst().orElse((Class<U[]>) (Object) Object.class);
            final Tuple2<Integer, Integer> shape = shape(cells);// 获取矩阵类型
            return st(shape._2)
                    .map(i -> st(shape._1).map(j -> cells[j][i]).toArray(n -> (U[]) Array.newInstance(cellClazz, n)))// 行数据数组
                    .toArray(m -> (U[][]) Array.newInstance(rowClazz, m));// 返回数组元素
        } // if
    }

    /**
     * 生成一个洞0开始的无限流
     * 
     * @return 生成一个洞0开始的无限流
     */
    public static Stream<Integer> st() {
        return Stream.iterate(0, i -> i + 1);
    }

    /**
     * 生成一个洞0开始的长度为n的流
     * 
     * @return 生成一个洞0开始的长度为n的流
     */
    public static Stream<Integer> st(int n) {
        return st().limit(n);
    }

    /**
     * 删除数组的第一行
     * 
     * @param cells 数据矩阵
     * @return U类型的数据矩阵
     */
    @SuppressWarnings("unchecked")
    public static <U> U[][] removeFirstLine(final U[][] cells) {
        final int m = cells.length;
        final int n = cells[0].length;
        if (m < 1) {
            return null;
        }

        final U[][] cc = (U[][]) Array.newInstance(getGenericClass(cells), m - 1, n);
        System.arraycopy(cells, 1, cc, 0, (m - 1));

        return cc;
    }

    /**
     * 把A转换成0,b转换成1,aa 转换成26 如果line 本身就是一个数字则直接返回 "A1:B2" -> x0=0, y0=0, x1=1, y1=1
     * 
     * @param line 字符串: 字符类别,A:转换成0,B 转换成1,AAA 转换成 702 数字类别 ,1:转换哼0,2 转换成 1;
     * @return 数据解析
     */
    public static Integer excel_name_to_index(final String line) {
        if (line == null)
            return null;
        final String final_line = line.toUpperCase().trim();// 转换成大写形式。
        Matcher matcher = Pattern.compile("\\d+").matcher(final_line);
        if (matcher.matches()) {// 数字格式
            return Integer.parseInt(final_line) - 1;
        }

        matcher = Pattern.compile("[A-Z]+").matcher(final_line);
        if (!matcher.matches())
            return null;
        final int N_SIZE = 26;// 进制
        final int len = final_line.length();
        int num = 0;// 数量内容
        for (int i = 0; i < len; i++) {
            final int n = (final_line.charAt(i) - 'A') + (i == len - 1 ? 0 : 1);// A在line尾端为0，否则A对应为1
            num = num * N_SIZE + n;
        } // for

        return num;
    }

    /**
     * 获得cells数组中的元素的数据类型。 如果cells中的所有元素都是null,返回Object.class;
     * 
     * @param <U>          cells数组中的元素的数据类型。
     * @param cells        数据矩阵
     * @param defaultClass 当cells 全为空返回的默认类型。
     * @return cells 元素类型
     */
    @SuppressWarnings("unchecked")
    public static <U> Class<U> getGenericClass(final U[][] cells, final Class<U> defaultClass) {
        Class<U> uclass = (Class<U>) defaultClass;
        if (cells == null)
            return uclass;
        List<Class<?>> ll = Arrays.stream(cells).flatMap(Arrays::stream).filter(Objects::nonNull)
                .map((Function<U, ? extends Class<?>>) U::getClass).distinct().collect(Collectors.toList());
        if (ll.size() == 1) {
            uclass = (Class<U>) ll.get(0);
        } else {
            uclass = (Class<U>) Object.class;
        }

        return uclass;
    }

    /**
     * 获得cells数组中的元素的数据类型。 如果cells中的所有元素都是null,返回Object.class;
     * 
     * @param <U>   cells数组中的元素的数据类型。
     * @param cells 数据矩阵<br>
     *              defaultClass 当cells 全为空返回的默认类型:Object.class
     * @return cells 元素类型
     */
    @SuppressWarnings("unchecked")
    public static <U> Class<U> getGenericClass(final U[][] cells) {
        return getGenericClass(cells, (Class<U>) Object.class);
    }

    /**
     * 创建子矩阵
     * 
     * @param cells 对象二维数组
     * @param i0    起点行索引 从0开始 行坐标
     * @param j0    起点行索引 从0开始 列坐标
     * @param i1    终点行索引 包含 对于超过最大范文的边界节点采用 循环取模的办法给与填充
     * @param j1    终点行索引 包含 对于超过最大范文的边界节点采用 循环取模的办法给与填充
     * @return 子矩阵
     */
    public static <U> U[][] submatrix(final U[][] cells, final int i0, final int j0, final int i1, final int j1) {
        final int h = i1 - i0 + 1;
        final int w = j1 - j0 + 1;
        final Tuple2<Integer, Integer> shape = DataMatrix.shape(cells);
        @SuppressWarnings("unchecked")
        final U[][] cc = (U[][]) Array.newInstance(getGenericClass(cells), h, w);
        for (int i = i0; i <= i1; i++) {
            for (int j = j0; j <= j1; j++) {
                cc[i - i0][j - j0] = cells[i % shape._1][j % shape._2];
            } // for j
        } // for i
        return cc;
    }

    /**
     * 返回矩阵的高度与宽度即行数与列数
     * 
     * @param <U> aa的元素类型
     * @param aa  待检测的矩阵：过矩阵为null返回一个(0,0)的二元组。
     * @return (height:行数,width:列数)
     */
    public static <U> Tuple2<Integer, Integer> shape(final U[][] aa) {
        if (aa == null || aa.length < 1)
            return new Tuple2<>(0, 0);
        final int height = aa.length;
        int width = 0;
        for (U[] us : aa) {
            if (us != null) {
                width = us.length;
                break;
            } // if
        } // for
        return new Tuple2<>(height, width);
    }

    /**
     * 把 一个数字 n转换成一个字母表中的数值(术语） 在alphabetics中:ABCDEFGHIJKLMNOPQRSTUVWXYZ
     * 比如:0->A,1-B,25-Z,26-AA 等等
     * 
     * @param n      数字
     * @param alphas 字母表
     * @return 生成exel式样的名称
     */
    public static String nomenclature(final Integer n, final String[] alphas) {
        final int model = alphas.length;// 字母表尺寸
        final List<Integer> dd = new LinkedList<>();
        Integer num = n;
        do {
            dd.add(num % model);
            num /= model;// 进入下回一个轮回
        } while (num-- > 0); // num-- 使得每次都是从A开始，即Z的下一个是AA而不是BA
        // 就是这个简答但算法我想了一夜,我就是不知道如何让10位每次都从0开始。
        Collections.reverse(dd);
        return dd.stream().map(e -> alphas[e]).collect(Collectors.joining(""));
    }

    /**
     * 列名称： 从0开始 0->A,1->B;2->C;....,25->Z,26->AA
     * 
     * @param n 数字 从0开始映射
     * @return 类似于EXCEL的列名称
     */
    public static String to_excel_name(final int n) {
        // 字母表
        String[] alphabetics = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".split("");
        return nomenclature(n, alphabetics);
    }

    /**
     * 矩阵数据的二维数组
     * 
     * @return 矩阵数据的二维数组
     */
    public T[][] data() {
        return this.cells;
    }

    private T[][] cells; // 单元格数据
    private Map<String, Integer> keymetas = new HashMap<>();// 表头名-->列id索引 的 Map,列id索引从0开始

}
