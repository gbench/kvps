package gbench.util.lisp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 数据框
 * 
 * @author xuqinghua
 *
 */
public class DFrame implements Iterable<IRecord> {

    /**
     * 构造函数
     * 
     * @param data 源数据
     */
    public DFrame(final List<IRecord> data) {
        this.rowsData = data.stream().toArray(IRecord[]::new);
    }

    /**
     * 
     * @return
     */
    public List<String> keys() {
        return new ArrayList<>(this.cols().keySet());
    }

    /**
     * 
     * @return
     */
    public String keyOf(final int idx) {

        final List<String> kk = this.keys();
        return idx < kk.size() ? kk.get(idx) : null;
    }

    /**
     * 指定列名的记录
     * 
     * @param name 列名
     * @return 列数据
     */
    public Optional<List<Object>> colOpt(final String name) {
        return Optional.ofNullable(this.cols().get(name));
    }

    /**
     * 指定列名的记录
     * 
     * @param name 列名
     * @return 列数据
     */
    public List<Object> col(final String name) {
        return this.colOpt(name).orElse(null);
    }

    /**
     * 指定列索引的记录
     * 
     * @param idx 列名索引从0开始
     * @return 列数据
     */
    public Optional<List<Object>> colOpt(final int idx) {
        return this.colOpt(this.keyOf(idx));
    }

    /**
     * 列数据
     * 
     * @param idx 列名索引从0开始
     * @return 列数据
     */
    public List<Object> col(final int idx) {
        return this.colOpt(idx).orElse(null);
    }

    /**
     * 转换成列数据数组
     * 
     * @return
     */
    public LinkedHashMap<String, ArrayList<Object>> initialize() {

        final LinkedHashMap<String, ArrayList<Object>> map = new LinkedHashMap<String, ArrayList<Object>>();
        Arrays.stream(this.rowsData).forEach(e -> {
            e.keys().forEach(k -> {
                map.compute(k, (_key, _value) -> _value == null ? new ArrayList<Object>() : _value).add(e.get(k));
            });
        });

        return map;

    }

    /**
     * 列数据
     * 
     * @return 行数据流
     */
    public synchronized LinkedHashMap<String, ArrayList<Object>> cols() {
        return this.colsData == null ? this.colsData = this.initialize() : this.colsData;
    }

    /**
     * 列数据
     * 
     * @param <T>    原数据类型
     * @param <U>    目标数据类型
     * @param name   列名
     * @param mapper 列元素的数据变换 t->u
     * @return 列数据的流
     */
    @SuppressWarnings("unchecked")
    public <T, U> Stream<U> colS(final String name, final Function<T, U> mapper) {
        return this.col(name).stream().map(e -> (T) e).map(mapper);
    }

    /**
     * 行数据流
     * 
     * @return 行数据流
     */
    public List<IRecord> rows() {
        return Arrays.asList(this.rowsData);
    }

    /**
     * 行数据流
     * 
     * @return 行数据流
     */
    public Stream<IRecord> rowS() {
        return Arrays.stream(this.rowsData);
    }

    /**
     * 指定行索引的记录
     * 
     * @param idx 行名索引从0开始
     * @return 行数据流
     */
    public Optional<IRecord> rowOpt(final int idx) {
        return Optional.of(this.rowsData[idx]);
    }

    /**
     * 指定行索引的记录
     * 
     * @param idx 行名索引从0开始
     * @return 行数据流
     */
    public IRecord row(final int idx) {
        return this.rowOpt(idx).orElse(null);
    }

    /**
     * 获取最大值（各个列的最大值，或是 最大行记录, 最大值的规则 需要 使用 biop 来指定 )
     * 
     * @param biop IRecord的二元运算法 把 两个IRecord较大的求取出来 (record0,record1)->record <br>
     *             record 可以是 record0,record1 之一, 也可以是 重新生成的一个IRecord，具体的要根据业务规则来实现。
     * @return 获取最大值（各个列的最大值，或是 最大行记录, 最大值的规则 需要 使用 biop 来指定 )
     */
    public Optional<IRecord> maxOpt(final BinaryOperator<IRecord> biop) {
        return this.rowS().reduce(biop);
    }

    /**
     * 获取最大值（各个列的最大值，或是 最大行记录, 最大值的规则 需要 使用 biop 来指定 )
     * 
     * @param biop IRecord的二元运算法 把 两个IRecord较大的求取出来 (record0,record1)->record <br>
     *             record 可以是 record0,record1 之一, 也可以是 重新生成的一个IRecord，具体的要根据业务规则来实现。
     * @return 获取最大值（各个列的最大值，或是 最大行记录, 最大值的规则 需要 使用 biop 来指定 )
     */
    public IRecord max(final BinaryOperator<IRecord> biop) {
        return this.maxOpt(biop).orElse(null);
    }

    /**
     * 获取最小值（各个列的最小值，或是 最大行记录, 最小值的规则 需要 使用 biop 来指定 )
     * 
     * @param biop IRecord的二元运算法 把 两个IRecord较小的求取出来 (record0,record1)->record <br>
     *             record 可以是 record0,record1 之一, 也可以是 重新生成的一个IRecord，具体的要根据业务规则来实现。
     * @return 获取最大值（各个列的最小值，或是 最大行记录, 最小值的规则 需要 使用 biop 来指定 )
     */
    public Optional<IRecord> minOpt(final BinaryOperator<IRecord> biop) {
        return this.rowS().reduce(biop);
    }

    /**
     * 获取最小值（各个列的最小值，或是 最大行记录, 最小值的规则 需要 使用 biop 来指定 )
     * 
     * @param biop IRecord的二元运算法 把 两个IRecord较小的求取出来 (record0,record1)->record <br>
     *             record 可以是 record0,record1 之一, 也可以是 重新生成的一个IRecord，具体的要根据业务规则来实现。
     * @return 获取最大值（各个列的最小值，或是 最大行记录, 最小值的规则 需要 使用 biop 来指定 )
     */
    public IRecord min(final BinaryOperator<IRecord> biop) {
        return this.minOpt(biop).orElse(null);
    }

    /**
     * 
     */
    @Override
    public Iterator<IRecord> iterator() {
        return this.rows().iterator();
    }

    /**
     * 格式化
     */
    @Override
    public String toString() {
        return fmt(Arrays.asList(this.rowsData));
    }

    /**
     * 创建一个 DFrame 对象
     * 
     * @param data
     * @return
     */
    public static DFrame of(List<IRecord> data) {
        return new DFrame(data);
    }

    /**
     * DFrame 數據的格式化
     * 
     * @param records
     * @return
     */
    public static String fmt(List<IRecord> records) {
        if (records.size() > 0) {
            final StringBuffer buffer = new StringBuffer();
            final IRecord first = records.get(0);
            final List<String> keys = first.keys();
            final String header = keys.stream().collect(Collectors.joining("\t"));
            final String body = records.stream() //
                    .map(e -> keys.stream().map(e::str).collect(Collectors.joining("\t"))) // 行數據格式化
                    .collect(Collectors.joining("\n"));
            buffer.append(header + "\n");
            buffer.append(body);
            return buffer.toString();
        } else {
            return "DFrame(empty)";
        }

    }

    /**
     * DFrame 归集器
     * 
     * @param <T>    归集的元素类型
     * @param mapper t->rec
     * @return [o]->dfm
     */
    public static <T> Collector<T, ?, DFrame> dfmclc(final Function<T, IRecord> mapper) {

        return Collector.of((Supplier<List<IRecord>>) ArrayList::new, (acc, t) -> acc.add(mapper.apply(t)),
                (left, right) -> {
                    left.addAll(right);
                    return left;
                }, e -> {
                    return new DFrame(e);
                });
    }

    /**
     * DFrame 归集器
     */
    public static Collector<IRecord, ?, DFrame> dfmclc = Collector.of((Supplier<List<IRecord>>) ArrayList::new,
            List::add, (left, right) -> {
                left.addAll(right);
                return left;
            }, e -> {
                return new DFrame(e);
            });

    /**
     * DFrame 归集器
     */
    public static Collector<Map<?, ?>, ?, DFrame> dfmclc2 = Collector.of((Supplier<List<IRecord>>) ArrayList::new,
            (aa, a) -> aa.add(new MyRecord(a)), (left, right) -> {
                left.addAll(right);
                return left;
            }, e -> {
                return new DFrame(e);
            });

    protected transient LinkedHashMap<String, ArrayList<Object>> colsData = null; // 列数据
    protected final IRecord[] rowsData; // 行数据

}
