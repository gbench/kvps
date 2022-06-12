package gbench.util.lisp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import gbench.util.json.MyJson;

/**
 * 数据记录对象的实现
 * 
 * @author gbench
 *
 */
public class MyRecord implements IRecord, Serializable {

    /**
     * 序列号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 构造数据记录对象
     * 
     * @param data IRecord数据
     */
    public MyRecord(final Map<?, ?> data) {
        data.forEach((k, v) -> {
            this.data.put(k instanceof String ? (String) k : k + "", v);
        }); // forEach
    }

    /**
     * 默认构造函数 <br>
     * 构造空数据记录 {}
     * 
     */
    public MyRecord() {
        this(new LinkedHashMap<String, Object>());
    }

    @Override
    public IRecord set(final String key, final Object value) {
        this.data.put(key, value);
        return this;
    }

    @Override
    public IRecord remove(String key) {
        this.data.remove(key);
        return this;
    }

    @Override
    public IRecord build(final Object... kvs) {
        return MyRecord.REC(kvs);
    }

    @Override
    public Map<String, Object> toMap() {
        return this.data;
    }

    @Override
    public List<String> keys() {
        return new ArrayList<>(this.data.keySet());
    }

    /**
     * 
     */
    @Override
    public Object get(final String key) {
        return this.data.get(key);
    }

    @Override
    public IRecord duplicate() {
        final MyRecord _rec = new MyRecord();
        _rec.data.putAll(this.data);
        return _rec;
    }

    @Override
    public String json() {
        return MyRecord.toJson(this.toMap2());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.toMap());
    }

    @Override
    public String toString() {
        return this.data.toString();
    }

    /**
     * 转换成 json 格式
     * 
     * @param json json字符串
     * @return IRecord
     */
    public static IRecord fromJson(final String json) {
        return MyJson.fromJson(json);
    }

    /**
     * 转换成 json 格式
     * 
     * @param json json字符串
     * @throws Exception
     */
    public static IRecord fromJson2(final String json) throws Exception {
        return MyJson.fromJson2(json);
    }

    /**
     * 转换成 json 格式
     * 
     * @param obj 值对象
     * @return json 字符串
     */
    public static String toJson(final Object obj) {
        return MyJson.toJson(obj);
    }

    /**
     * 转换成 json 格式,带有异常抛出
     * 
     * @param obj 值对象
     * @return json 字符串
     * @throws Exception
     */
    public static String toJson2(final Object obj) throws Exception {
        return MyJson.toJson2(obj);
    }

    /**
     * Iterable 转 List
     * 
     * @param <T>      元素类型
     * @param iterable 可迭代类型
     * @param maxSize  最大元素长度
     * @return 元素类型
     */
    public static <T> List<T> iterable2list(final Iterable<T> iterable, final long maxSize) {
        final ArrayList<T> aa = new ArrayList<>();
        StreamSupport.stream(iterable.spliterator(), false).limit(maxSize).forEach(aa::add);

        return aa;
    }

    /**
     * 标准版的记录生成器, map 生成的是LinkedRecord
     * 
     * @param <T> 类型占位符
     * @param kvs 键,值序列:key0,value0,key1,value1,.... <br>
     *            Map结构（IRecord也是Map结构） 或是 键名,键值 序列。即 build(map) 或是
     *            build(key0,value0,key1,vlaue1,...) 的 形式， 特别注意 build(map) 时候，当且仅当
     *            kvs 的只有一个元素，即 build(map0,map1) 会被视为 键值序列
     * @return IRecord对象
     */
    @SafeVarargs
    public static <T> IRecord REC(final T... kvs) {
        final int n = kvs.length;
        final LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>();
        final Consumer<Tuple2<String, ?>> put_tuple = tup -> { // 元组处理
            data.put(tup._1, tup._2);
        };
        final Consumer<Stream<Object>> put_stream = stream -> { // 数据流的处理
            stream.map(Tuple2.snb(0)).map(tuple -> { // 编号分组
                return Optional.ofNullable(tuple._2).map(e -> {
                    if (e instanceof Tuple2) { // Tuple2 元组类型
                        return (Tuple2<?, ?>) e;
                    } else if (e instanceof Map.Entry) { // Map.Entry 类型
                        final Map.Entry<?, ?> me = (Map.Entry<?, ?>) tuple._2;
                        return Tuple2.of(me.getKey(), me.getValue());
                    } else { // default 默认
                        return tuple;
                    } // if
                }).map(e -> e.map1(Object::toString)).orElse(null); // key 专为字符串类型 && 默认值为null
            }).filter(Objects::nonNull).forEach(put_tuple); //
        }; // put_stream
        final Consumer<Iterable<Object>> put_iterable = iterable -> { // iterable 数据处理
            put_stream.accept(StreamSupport.stream(iterable.spliterator(), false));
        }; // put_iterable

        if (n == 1) { // 单一参数情况
            final T single = kvs[0]; // 提取单值
            if (single instanceof Map) { // Map情况的数据处理
                ((Map<?, ?>) single).forEach((k, v) -> { // 键值的处理
                    final String key = k instanceof String ? (String) k : k + "";
                    data.put(key, v);
                }); // forEach
            } else if (single instanceof IRecord) {// IRecord 对象类型 复制对象数据
                data.putAll(((IRecord) single).toMap());
            } else if (single instanceof Collection) {// Collection 对象类型 复制对象数据
                @SuppressWarnings("unchecked")
                final Collection<Object> coll = (Collection<Object>) single;
                put_iterable.accept(coll);
            } else if (single instanceof Iterable) {// Iterable 对象类型 复制对象数据
                @SuppressWarnings("unchecked")
                final Iterable<Object> iterable = (Iterable<Object>) single;
                put_iterable.accept(iterable);
            } else if (single instanceof Iterator) {// Iterable 对象类型 复制对象数据
                @SuppressWarnings("unchecked")
                final Iterable<Object> iterable = () -> (Iterator<Object>) single;
                put_iterable.accept(iterable);
            } else if (single instanceof Stream) {// stream 对象类型 复制对象数据
                @SuppressWarnings("unchecked")
                final Stream<Object> stream = (Stream<Object>) single;
                put_stream.accept(stream);
            } else if (single instanceof String) { // 字符串类型的单个参数
                final String line = single.toString();
                final IRecord rec = Optional.ofNullable(line) //
                        .map(String::trim)
                        .map(ln -> { //
                            IRecord r = null;

                            try { // 乐观锁 假设用户输入的是合法的json
                                r = MyRecord.fromJson2(ln);
                            } catch (Exception e) { // 非合法的json
                                if (!(ln.startsWith("{") && ln.endsWith("}"))) { // 省略的 最外层的大括号
                                    final String _ln = IRecord.FT("{$0}", ln); // 补充外层括号
                                    r = MyRecord.fromJson(_ln);
                                } // if
                            } // try

                            return r;
                        }) //
                        .orElse(IRecord.REC()); // 尝试解析json
                if (rec != null) { //
                    final Map<String, Object> _data = rec.toMap();
                    if (_data.size() > 0) { // 非空数据
                        data.putAll(_data);
                    } // if
                } // if
            } else { // if
                // do nothing 省略其他单值情况
            } // if
        } else { // 键名减值序列
            for (int i = 0; i < n - 1; i += 2) {
                data.put(kvs[i].toString(), kvs[i + 1]);
            }
        } // if

        return new MyRecord(data);
    }

    private LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>();
}