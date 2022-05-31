package gbench.util.lisp;

import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 数据记录对象
 * 
 * @author xuqinghua
 *
 */
public interface IRecord extends Comparable<IRecord> {

    /**
     * 根据键名进行取值
     * 
     * @param key 键名
     * @return 键key的值
     */
    Object get(final String key);

    /**
     * 设置键，若 key 与 老的 键 相同则 覆盖 老的值
     * 
     * @param key   新的 键名
     * @param value 键值
     * @return 对象本身
     */
    IRecord set(String key, Object value);

    /**
     * 除掉键 key 的值
     * 
     * @param key 新的 键名
     * @return 对象本身(移除了key)
     */
    IRecord remove(String key);

    /**
     * 转换成json 字符串
     * 
     * @return json 字符串
     */
    String json();

    /**
     * 数据复制
     * 
     * @return 当前对象的拷贝
     */
    IRecord duplicate();

    /**
     * 构建一个键名键值序列 指定的 IRecord
     * 
     * @param kvs Map结构（IRecord也是Map结构） 或是 键名,键值 序列。即 build(map) 或是
     *            build(key0,value0,key1,vlaue1,...) 的 形式， 特别注意 build(map) 时候，当且仅当
     *            kvs 的只有一个元素，即 build(map0,map1) 会被视为 键值序列
     * @return 新生成的IRecord
     */
    IRecord build(final Object... kvs);

    /**
     * 键名序列
     * 
     * @return 键名列表
     */
    List<String> keys();

    /**
     * 返回一个 Map 结构<br>
     * 非递归进行变换
     * 
     * @return 一个 键值 对儿 的 列表 [(key,map)]
     */
    Map<String, Object> toMap();

    /**
     * 返回一个 Map 结构,递归遍历<br>
     * 非递归进行变换
     * 
     * @return 一个 键值 对儿 的 列表 [(key,map)]
     */
    default Map<String, Object> toMap2() {
        final Map<String, Object> data = new LinkedHashMap<String, Object>();
        this.forEach((key, value) -> {
            if (value instanceof Iterable) {
                final List<?> value_ll = IRecord.iterable2list((Iterable<?>) value,
                        e -> e instanceof IRecord ? ((IRecord) e).toMap2() : e);
                data.put(key, value_ll);
            } else if (value instanceof Stream) {
                @SuppressWarnings("unchecked")
                final List<?> value_ll = ((Stream<Object>) value) //
                        .map(e -> e instanceof IRecord ? ((IRecord) e).toMap2() : e) //
                        .collect(Collectors.toList());
                data.put(key, value_ll);
            } else if (value instanceof IRecord) {
                final IRecord rec = (IRecord) value;
                data.put(key, rec.toMap2());
            } else {
                data.put(key, value);
            }
        });
        return data;
    }

    /**
     * 数据元素便利访问
     * 
     * @param action 遍历的回调函数 (key,value)->{}
     * @return this 对象本身。
     */
    public default IRecord forEach(BiConsumer<String, Object> action) {
        this.toMap().forEach(action);
        return this;
    }

    /**
     * 设置键，若 idx 与 老的 键 相同则 覆盖 老的值
     * 
     * @param idx   键名索引，从0开始
     * @param value 数值
     * @return this 对象本身
     */
    default IRecord set(final Integer idx, final Object value) {
        final String key = this.keyOf(idx);
        this.add(key, value);
        return this;
    }

    /**
     * 构造 IRecord <br>
     * 按照构建器的 键名序列表，依次把objs中的元素与其适配以生成 IRecord <br>
     * {key0:objs[0],key1:objs[1],key2:objs[2],...}
     * 
     * @param <T>  元素类型
     * @param objs 值序列, 若 objs 为 null 则返回null, <br>
     *             若 objs 长度不足以匹配 keys 将采用 循环补位的仿制给予填充 <br>
     *             若 objs 长度为0则返回一个空对象{},注意是没有元素且不是null的对象
     * @return IRecord 对象 若 objs 为 null 则返回null
     */
    default public IRecord get(final Object... objs) {
        if (objs == null) { // 空值判断
            return null;
        }

        final int n = objs.length;
        final List<String> keys = this.keys();
        final int size = this.keys().size();
        final LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>();

        for (int i = 0; n > 0 && i < size; i++) {
            final String key = keys.get(i);
            final Object value = objs[i % n];
            data.put(key, value);
        } // for

        return this.build(data);
    }

    /**
     * 提取record类型的结果 <br>
     * 
     * 可以识别的值类型IRecord,Map,Collection,Array其中Collection和Array 的 key为索引序号，从开始
     * 
     * @param <X>          源数据类型
     * @param <Y>          目标数据类型,即 IRecord,Map,Collection,Array 其中之一
     * @param key          键名
     * @param preprocessor 预处理器
     * @return IRecord类型的值
     */
    @SuppressWarnings("unchecked")
    default <X, Y> IRecord rec(final String key, final BiFunction<String, X, Y> preprocessor) {
        final Y value = preprocessor.apply(key, (X) this.get(key));
        final Function<Collection<?>, IRecord> clcn2rec = tt -> {
            int i = 0;
            final IRecord rec = this.build(); // 创建一个空IRecord
            for (final Object t : tt) {
                rec.add("" + (i++), t);
            } // for
            return rec;
        }; // clc2rec

        if (value instanceof IRecord) {
            return (IRecord) value;
        } else if (value instanceof Map) {
            return this.build((Map<?, ?>) value);
        } else if (value instanceof Collection || (value != null && value.getClass().isArray())) {
            final Collection<Object> tt = value instanceof Collection // 值类型判断
                    ? (Collection<Object>) value // 集合类型
                    : (Collection<Object>) Arrays.asList((Object[]) value); // 数组类型
            return clcn2rec.apply(tt);
        } else if (value instanceof Iterable) {
            return clcn2rec.apply(IRecord.itr2list((Iterable<?>) value));
        } else {
            return null;
        }
    }

    /**
     * 根据路径提取数据
     * 
     * @param <X>          元素类型
     * @param <Y>          元素类型，需要为IRecord,Map,Collection,Array其中Collection和Array任一
     * @param <T>          元素类型
     * @param <U>          结果类型
     * @param path         键名路径
     * @param preprocessor 预处理器 x->y
     * @param mapper       值变换函数 t->u
     * @return U类型的值
     */
    @SuppressWarnings("unchecked")
    default <X, Y, T, U> U pathget(final String[] path, final BiFunction<String, X, Y> preprocessor,
            final Function<T, U> mapper) {
        final String key = path[0].strip();
        final Integer len = path.length;

        if (len > 1) {
            final IRecord _rec = this.rec(key, preprocessor);
            final String[] _path = Arrays.copyOfRange(path, 1, len);
            return _rec != null ? _rec.pathget(_path, preprocessor, mapper) : null;
        } else {
            return mapper.apply((T) this.get(key));
        } // if
    }

    /**
     * 根据路径提取数据 <br>
     * 
     * 可以识别的值类型IRecord,Map,Collection,Array其中Collection和Array 的 key为索引序号，从开始
     * 
     * @param <X>          元素类型
     * @param <Y>          元素类型Y 需要为
     *                     IRecord,Map,Collection,Array其中Collection和Array任一
     * @param <T>          元素类型
     * @param <U>          结果类型
     * @param path         键名路径
     * @param preprocessor 预处理器 x->y
     * @param mapper       值变换函数 t->u
     * @return U类型的值
     */
    default <X, Y, T, U> U pathget(final String path, final BiFunction<String, X, Y> preprocessor,
            final Function<T, U> mapper) {
        return this.pathget(path.split("[/,]+"), preprocessor, mapper);
    }

    /**
     * 根据路径提取数据
     * 
     * @param <T>    元素类型
     * @param <U>    结果类型
     * @param path   键名路径 如 a/b/c
     * @param mapper 值变换函数 t->u
     * @return U类型的值
     */
    default <T, U> U pathget(final String path, final Function<T, U> mapper) {
        return this.pathget(path, (k, e) -> e, mapper);
    }

    /**
     * 根据路径提取数据
     * 
     * @param <T>      键值变换函数的源类型
     * @param <U>      键值变换函数的目标类型
     * @param path     键名路径 如 a/b/c
     * @param streamer 元素对象流构建器 t->[u]
     * @return U类型的元素对象流
     */
    default <T, U> Stream<U> pathllS(final String path, final Function<T, Stream<U>> streamer) {
        return this.pathget(path, streamer);
    }

    /**
     * 根据路径提取数据
     * 
     * @param path 键名路径 如 a/b/c
     * @return 元素对象流,路径不存在或是值为null的时候返回null值
     */
    default Stream<Object> pathllS(final String path) {
        return this.pathllS(path, e -> Optional.ofNullable(e).map(IRecord::asList).map(List::stream).orElse(null));
    }

    /**
     * 把idx转key
     * 
     * @param idx 键名索引 从0开始
     * @return 索引转键名
     */
    default String keyOf(final int idx) {

        final List<String> kk = this.keys();
        return idx < kk.size() ? kk.get(idx) : null;
    }

    /**
     * 键名索引
     * 
     * @param key 键名
     * @return 键名索引 从0开始
     */
    default Integer indexOf(final String key) {

        final List<String> kk = this.keys();

        for (int i = 0; i < kk.size(); i++) {
            final String _key = kk.get(i);
            if (key.equals(_key))
                return i;
        }

        return null;
    }

    /**
     * 返回值数据流
     * 
     * @return 值数据流
     * 
     */
    default Stream<Object> valueS() {
        return this.keys().stream().map(k -> this.get(k));
    }

    /**
     * 返回值列表
     * 
     * @return 值列表
     * 
     */
    default List<Object> values() {
        return this.valueS().collect(Collectors.toList());
    }

    /**
     * 带有缺省值计算的值获取函数，<br>
     * defaultEvaluator 计算的结果并不给予更新到this当中。这是与computeIfAbsent不同的。
     * 
     * @param <T>              返回值类型
     * @param key              键名
     * @param defaultEvaluator 缺省值计算函数(key)->obj
     * @return 缺值计算的值
     */
    default <T> T get(final String key, Function<String, T> defaultEvaluator) {
        @SuppressWarnings("unchecked")
        final T t = (T) this.get(key);
        return Optional.ofNullable(t).orElse(defaultEvaluator.apply(key));
    }

    /**
     * 提取键名所标定的值
     * 
     * @param key 键名
     * @return Optional 的键值
     */
    default Optional<Object> opt(final String key) {
        return Optional.ofNullable(this.get(key));
    }

    /**
     * 提取键名索引所标定的值
     * 
     * @param idx 键名索引，从0开始
     * @return Optional 的键值
     */
    default Optional<Object> opt(final Integer idx) {
        return Optional.ofNullable(this.get(idx));
    }

    /**
     * 把key列转换成逻辑值
     * 
     * @param idx 键名索引,从0开始
     * @return 布尔类型
     */
    default Boolean bool(final Integer idx) {
        return this.bool(this.keyOf(idx));
    };

    /**
     * 把key列转换成逻辑值
     * 
     * @param key 键名
     * @return 布尔类型
     */
    default Boolean bool(final String key) {
        return this.get(key, o -> Boolean.parseBoolean(o + ""));
    };

    /**
     * 把key列转换成逻辑值
     * 
     * @param key           键名
     * @param default_value 默认值
     * @return 布尔类型
     */
    default Boolean bool(final String key, final Boolean default_value) {
        final Boolean b = this.bool(key);
        return b == null ? default_value : b;
    };

    /**
     * 返回 建明索引 所对应的 键值, Boolean 类型
     * 
     * @param idx 键名索引 从0开始
     * @return idx 所标定的 值 Optional
     */
    default Optional<Boolean> boolOpt(final int idx) {
        return Optional.ofNullable(this.bool(idx));
    }

    /**
     * 返回 key 所对应的 键值, Boolean 类型
     * 
     * @param key 键名
     * @return key 所标定的 值 Optional
     */
    default Optional<Boolean> boolOpt(final String key) {
        return Optional.ofNullable(this.bool(key));
    }

    /**
     * 返回 key 所对应的 键值, Integer 类型
     * 
     * @param key 键名
     * @return key 所标定的 值
     */
    default Integer i4(final String key) {
        return Optional.ofNullable(this.dbl(key)).map(Number::intValue).orElse(null);
    }

    /**
     * 返回 键名索引 所对应的 键值, Integer 类型
     * 
     * @param idx 键名索引 从0开始 从0开始
     * @return idx 所标定的 值
     */
    default Integer i4(final Integer idx) {
        return this.i4(this.keyOf(idx));
    }

    /**
     * 返回 建明索引 所对应的 键值, Integer 类型
     * 
     * @param idx 键名索引 从0开始
     * @return idx 所标定的 值 Optional
     */
    default Optional<Integer> i4Opt(final int idx) {
        return Optional.ofNullable(this.i4(idx));
    }

    /**
     * 返回 key 所对应的 键值, Integer 类型
     * 
     * @param key 键名
     * @return key 所标定的 值 Optional
     */
    default Optional<Integer> i4Opt(final String key) {
        return Optional.ofNullable(this.i4(key));
    }

    /**
     * 返回 key 所对应的 键值, Integer 类型
     * 
     * @param key 键名
     * @return key 所标定的 值
     */
    default Long lng(final String key) {
        return Optional.ofNullable(this.dbl(key)).map(Number::longValue).orElse(null);
    }

    /**
     * 返回 键名索引 所对应的 键值, Integer 类型
     * 
     * @param idx 键名索引 从0开始 从0开始
     * @return idx 所标定的 值
     */
    default Long lng(final Integer idx) {
        return this.lng(this.keyOf(idx));
    }

    /**
     * 返回 建明索引 所对应的 键值, Long 类型
     * 
     * @param idx 键名索引 从0开始
     * @return idx 所标定的 值 Optional
     */
    default Optional<Long> lngOpt(final int idx) {
        return Optional.ofNullable(this.lng(idx));
    }

    /**
     * 返回 key 所对应的 键值, Long 类型
     * 
     * @param key 键名
     * @return key 所标定的 值 Optional
     */
    default Optional<Long> lngOpt(final String key) {
        return Optional.ofNullable(this.lng(key));
    }

    /**
     * 返回 key 所对应的 键值, Double 类型
     * 
     * @param key 键名
     * @return key 所标定的 值
     */
    default Double dbl(String key) {
        return IRecord.obj2dbl().apply(this.get(key));
    }

    /**
     * 返回 key 所对应的 键值, Double 类型
     * 
     * @param idx 键名索引 从0开始
     * @return idx 所标定的 值
     */
    default Double dbl(int key) {
        return this.dbl(this.keyOf(key));
    }

    /**
     * 返回 建明索引 所对应的 键值, Double 类型
     * 
     * @param idx 键名索引 从0开始
     * @return idx 所标定的 值 Optional
     */
    default Optional<Double> dblOpt(final int idx) {
        return Optional.ofNullable(this.dbl(idx));
    }

    /**
     * 返回 key 所对应的 键值, Double 类型
     * 
     * @param key 键名
     * @return key 所标定的 值 Optional
     */
    default Optional<Double> dblOpt(final String key) {
        return Optional.ofNullable(this.dbl(key));
    }

    /**
     * 返回 key 所对应的 键值, LocalDateTime 类型
     * 
     * @param key 键名
     * @return key 所标定的 值
     */
    default LocalDateTime ldt(final String key) {
        return this.ldt(key, null);
    }

    /**
     * 返回 建明索引 所对应的 键值, LocalDateTime 类型
     * 
     * @param idx 键名索引 从0开始
     * @return idx 所标定的 值
     */
    default LocalDateTime ldt(final int idx) {
        return this.ldt(this.keyOf(idx));
    }

    /**
     * 返回 key 所对应的 键值, LocalDateTime 类型
     * 
     * @param key          键名
     * @param defaultValue 默认值
     * @return key 所标定的 值
     */
    default LocalDateTime ldt(final String key, final LocalDateTime defaultValue) {
        final Object value = this.get(key);

        if (value == null) { // 空结构
            return defaultValue;
        } else { // 非空值
            final LocalDateTime ldt = IRecord.asLocalDateTime(value);
            return Optional.ofNullable(ldt).orElse(defaultValue);
        } // if
    }

    /**
     * 返回 建明索引 所对应的 键值, LocalDateTime 类型
     * 
     * @param idx 键名索引 从0开始
     * @return idx 所标定的 值 Optional
     */
    default Optional<LocalDateTime> ldtOpt(final int idx) {
        return Optional.ofNullable(this.ldt(idx));
    }

    /**
     * 返回 key 所对应的 键值, LocalDateTime 类型
     * 
     * @param key 键名
     * @return key 所标定的 值 Optional
     */
    default Optional<LocalDateTime> ldtOpt(final String key) {
        return Optional.ofNullable(this.ldt(key));
    }

    /**
     * 返回 key 所对应的 键值, LocalDateTime 类型
     * 
     * @param idx          键名索引
     * @param defaultValue 默认值
     * @return key 所标定的 值
     */
    default LocalDateTime ldt(final int idx, final LocalDateTime defaultValue) {
        return this.ldt(this.keyOf(idx), defaultValue);
    }

    /**
     * 返回 建明索引 所对应的 键值, String 类型
     * 
     * @param idx 键名索引 从0开始
     * @return idx 所标定的 值
     */
    default String str(final int idx) {
        return this.str(this.keyOf(idx));
    }

    /**
     * 返回 key 所对应的 键值, String 类型
     * 
     * @param key 键名
     * @return key 所标定的 值
     */
    default String str(final String key) {
        final Object obj = this.get(key);

        if (obj == null) {
            return null;
        }

        return obj instanceof String ? (String) obj : obj + "";
    }

    /**
     * 返回 建明索引 所对应的 键值, String 类型
     * 
     * @param idx 键名索引 从0开始
     * @return idx 所标定的 值 Optional
     */
    default Optional<String> strOpt(final int idx) {
        return this.opt(idx).map(Object::toString);
    }

    /**
     * 返回 key 所对应的 键值, String 类型
     * 
     * @param key 键名
     * @return key 所标定的 值 Optional
     */
    default Optional<String> strOpt(final String key) {
        return this.opt(key).map(Object::toString);
    }

    /**
     * 把 键名元素的值转换为 列表结构 <br>
     * lla 是 LinkedList apply 的含义，取意为 应用方法 获得 链表
     * 
     * @param <T>    源数据类型
     * @param <U>    目标列表元素的数据类型
     * @param key    键名
     * @param lister 列表构建器 t->[u]
     * 
     * @return 列表结构的数据,U 类型的列表 [u]
     */
    @SuppressWarnings("unchecked")
    default <T, U> List<U> lla(final String key, final Function<T, List<U>> lister) {
        final Object value = this.get(key); // 提取数据值

        return lister.apply((T) value);
    }

    /**
     * 把 键名索引 元素的值转换为 列表结构<br>
     * lla 是 LinkedList apply 的含义，取意为 应用方法 获得 链表
     * 
     * @param <T>    源数据类型
     * @param <U>    目标列表元素的数据类型
     * @param idx    键名索引 从0开始
     * @param lister 列表构建器 t->[u]
     * 
     * @return 列表结构的数据,U 类型的列表 [u]
     */
    default <T, U> List<Object> lla(final int idx, final Function<T, List<U>> lister) {
        return this.lla(this.keyOf(idx));
    }

    /**
     * 把 键名元素的值转换为 列表结构 <br>
     * lla 是 LinkedList apply 的含义，取意为 应用方法 获得 链表
     * 
     * @param key 键名
     * 
     * @return 列表结构的数据
     */
    default List<Object> lla(final String key) {
        return this.lla(key, IRecord::asList);
    }

    /**
     * 把 键名索引 元素的值转换为 列表结构
     * 
     * @param idx 键名索引 从0开始
     * 
     * @return 列表结构的数据
     */
    default List<Object> lla(final int idx) {
        return this.lla(this.keyOf(idx));
    }

    /**
     * 把 键名索引 元素的值转换为 元素列表
     * 
     * @param <T>    键值变换函数的源类型
     * @param <U>    键值变换函数的目标类型
     * @param key    键名
     * @param mapper 键值变换函数 t->u
     * @return U类型的 元素列表
     */
    default <T, U> List<U> lla2(final String key, final Function<T, U> mapper) {
        return this.llS2(key, mapper).collect(Collectors.toList());
    }

    /**
     * 把 键名索引 元素的值转换为 元素列表
     * 
     * @param <T>    键值变换函数的源类型
     * @param <U>    键值变换函数的目标类型
     * @param key    键名索引 从0开始
     * @param mapper 键值变换函数 t->u
     * @return U类型的 元素列表
     */
    default <T, U> List<U> lla2(final int idx, final Function<T, U> mapper) {
        return this.lla2(this.keyOf(idx), mapper);
    }

    /**
     * 把 键名元素的值转换为 元素对象流 <br>
     * llS 是 LinkedList Stream 的 缩写 根据 lla 的变体,S 表示这是一个 返回 Stream类型的函数
     * 
     * @param <T>      源数据类型
     * @param <U>      目标列表元素的数据类型
     * @param key      键名
     * @param streamer 元素对象流构建器 t->[u]
     * 
     * @return U 类型的元素对象流 [u]
     */
    @SuppressWarnings("unchecked")
    default <T, U> Stream<U> llS(final String key, final Function<T, Stream<U>> streamer) {
        final Object value = this.get(key); // 提取数据值

        return Optional.ofNullable(streamer.apply((T) value)).orElse(null);
    }

    /**
     * 把 键名索引 元素的值转换为 元素对象流 <br>
     * llS 是 LinkedList Stream 的 缩写 根据 lla 的变体,S 表示这是一个 返回 Stream类型的函数
     * 
     * @param <T>      源数据类型
     * @param <U>      目标列表元素的数据类型
     * @param idx      键名索引 从0开始
     * @param streamer 元素对象流构建器 t->[u]
     * 
     * @return U 类型的元素对象流 [u]
     */
    default <T, U> Stream<U> llS(final int idx, final Function<T, Stream<U>> streamer) {
        return this.llS(this.keyOf(idx), streamer);
    }

    /**
     * 把 键名 元素的值转换为 元素对象流
     * 
     * llS 是 LinkedList Stream 的 缩写 根据 lla 的变体,S 表示这是一个 返回 Stream类型的函数
     * 
     * @param key 键名
     * 
     * @return 元素对象流
     */
    default Stream<Object> llS(final String key) {
        return this.llS(key, e -> Optional.ofNullable(e).map(IRecord::asList).map(List::stream).orElse(null));
    }

    /**
     * 把 键名索引 元素的值转换为 元素对象流
     * 
     * @param idx 键名索引 从0开始
     * 
     * @return 元素对象流
     */
    default Stream<Object> llS(final int idx) {
        return this.llS(this.keyOf(idx));
    }

    /**
     * 把 键名 元素的值转换为 元素对象流
     * 
     * @param <T>    键值变换函数的源类型
     * @param <U>    键值变换函数的目标类型
     * @param key    键名
     * @param mapper 键值变换函数 t->u
     * @return U类型的元素对象流
     */
    @SuppressWarnings("unchecked")
    default <T, U> Stream<U> llS2(final String key, final Function<T, U> mapper) {
        return this.llS(key).map(e -> mapper.apply((T) e));
    }

    /**
     * 把 键名索引 元素的值转换为 元素对象流
     * 
     * @param <T>    键值变换函数的源类型
     * @param <U>    键值变换函数的目标类型
     * @param key    键名索引 从0开始
     * @param mapper 键值变换函数 t->u
     * @return U类型的元素对象流
     */
    default <T, U> Stream<U> llS2(final int idx, final Function<T, U> mapper) {
        return this.llS2(this.keyOf(idx), mapper);
    }

    /**
     * 把 键名 元素的值转换为 元素对象流
     * 
     * @param <T>    键值变换函数的源类型
     * @param <U>    键值变换函数的目标类型
     * @param key    键名
     * @param mapper 键值变换函数 t->u
     * @return U类型的元素对象流 的 Optional
     */
    @SuppressWarnings("unchecked")
    default <T, U> Optional<Stream<U>> llOptS2(final String key, final Function<T, U> mapper) {
        return Objects.nonNull(this.get(key)) //
                ? Optional.ofNullable(this.llS(key).map(e -> mapper.apply((T) e)))
                : Optional.empty();
    }

    /**
     * 把 键名索引 元素的值转换为 元素对象流
     * 
     * @param <T>    键值变换函数的源类型
     * @param <U>    键值变换函数的目标类型
     * @param key    键名索引 从0开始
     * @param mapper 键值变换函数 t->u
     * @return U类型的元素对象流 的 Optional
     */
    default <T, U> Optional<Stream<U>> llOptS2(final int idx, final Function<T, U> mapper) {
        return this.llOptS2(this.keyOf(idx), mapper);
    }

    /**
     * 更新式添加<br>
     * 增加新键，若 key 与 老的 键 相同则 覆盖 老的值
     * 
     * @param key   新的 键名
     * @param value 新的 键值
     * @return 对象本身
     */
    default IRecord add(final String key, final Object value) {
        this.set(key, value);
        return this;
    }

    /**
     * 更新式添加,即改变自身的内容<br>
     * 增加新键，若 key 与 老的 键 相同则 覆盖 老的值
     * 
     * @param tup 待添加的键值对
     * @return 对象本身
     */
    default IRecord add(final Tuple2<String, ?> tup) {
        return this.add(tup._1, tup._2);
    }

    /**
     * 更新式添加<br>
     * 增加新键，若 key 与 老的 键 相同则 覆盖 老的值 <br>
     * 若 kvs 长度为 1 <br>
     * 1) IRecord 或 Map 类型 根据 (键,值) 序列给予 元素添加 <br>
     * 2) Iterable 类型 索引序号（从0开始）为 键名, 元素值 进行 (键,值)序列添加
     * 
     * @param kvs 键名键值序列
     * @return 对象本身
     */
    default IRecord add(final Object... kvs) {
        if (kvs.length == 1) {
            final Object obj = kvs[0];
            if (obj instanceof IRecord) { // 记录类型
                ((IRecord) obj).forEach((k, v) -> { // 可迭代类型
                    this.add(k, v);
                });
            } else if (obj instanceof Map) { // Map类型
                ((Map<?, ?>) obj).forEach((k, v) -> {
                    this.add(k + "", v);
                });
            } else if (obj instanceof Iterable) { // 可迭代类型
                int i = 0; //
                for (Object x : (Iterable<?>) obj) {
                    if (i > 10000) {
                        break;
                    } else {
                        this.add(String.valueOf(i++), x);
                    } // if
                } // for
            } else {
                // do nothing
            }
        } else {
            IRecord.slidingS(kvs, 2, 2, true).forEach(wnd -> { // 窗口遍历
                this.add(wnd.get(0) + "", wnd.get(1));
            });
        }
        return this;
    }

    /**
     * 对键 key 应用函数 mapper
     * 
     * @param <T>    键值类型
     * @param <U>    结果类型
     * @param key    键名
     * @param mapper 键值变换函数 t->u
     * @return 对键 key 应用函数 mapper
     */
    @SuppressWarnings("unchecked")
    default <T, U> U invoke(final String key, Function<T, U> mapper) {
        return mapper.apply((T) this.get(key));
    }

    /**
     * 对键索引 idx 应用函数 mapper
     * 
     * @param <T>    键值类型
     * @param <U>    结果类型
     * @param idx    键名
     * @param mapper 键值变换函数 t->u
     * @return 对键 key 应用函数 mapper
     */
    default <T, U> U invoke(final int idx, Function<T, U> mapper) {
        return this.invoke(this.keyOf(idx), mapper);
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * <p>
     * The {@code equals} method implements an equivalence relation on non-null
     * object references:
     * <ul>
     * <li>It is <i>reflexive</i>: for any non-null reference value {@code x},
     * {@code x.equals(x)} should return {@code true}.
     * <li>It is <i>symmetric</i>: for any non-null reference values {@code x} and
     * {@code y}, {@code x.equals(y)} should return {@code true} if and only if
     * {@code y.equals(x)} returns {@code true}.
     * <li>It is <i>transitive</i>: for any non-null reference values {@code x},
     * {@code y}, and {@code z}, if {@code x.equals(y)} returns {@code true} and
     * {@code y.equals(z)} returns {@code true}, then {@code x.equals(z)} should
     * return {@code true}.
     * <li>It is <i>consistent</i>: for any non-null reference values {@code x} and
     * {@code y}, multiple invocations of {@code x.equals(y)} consistently return
     * {@code true} or consistently return {@code false}, provided no information
     * used in {@code equals} comparisons on the objects is modified.
     * <li>For any non-null reference value {@code x}, {@code x.equals(null)} should
     * return {@code false}.
     * </ul>
     * <p>
     * The {@code equals} method for class {@code Object} implements the most
     * discriminating possible equivalence relation on objects; that is, for any
     * non-null reference values {@code x} and {@code y}, this method returns
     * {@code true} if and only if {@code x} and {@code y} refer to the same object
     * ({@code x == y} has the value {@code true}).
     * <p>
     * Note that it is generally necessary to override the {@code hashCode} method
     * whenever this method is overridden, so as to maintain the general contract
     * for the {@code hashCode} method, which states that equal objects must have
     * equal hash codes.
     *
     * @param rec the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument;
     *         {@code false} otherwise.
     * @see #hashCode()
     * @see java.util.HashMap
     */
    default boolean equals(final IRecord rec) {
        return this == rec || this.compareTo(rec) == 0;
    }

    /**
     * 元素个数
     * 
     * @return 元素个数
     */
    default int size() {
        return this.toMap().size();
    }

    /**
     * IRececord 之间的比较大小,比较的keys选择当前对象的keys,当 null 小于任何值,即null值会排在牵头
     */
    @Override
    default int compareTo(final IRecord o) {

        if (o == null) {
            return 1;
        } else if (this.keys().equals(o.keys())) {
            return IRecord.cmp(this.keys()).compare(this, o);
        } else {
            final Set<String> hashSet = new LinkedHashSet<String>(this.keys());
            hashSet.addAll(o.keys());// 归并
            final String[] kk = hashSet.stream().sorted().toArray(String[]::new);
            System.err.println(
                    "比较的两个个键名序列(" + this.keys() + "," + o.keys() + ")不相同,采用归并键名序列进行比较:" + Arrays.deepToString(kk));
            return IRecord.cmp(kk, true).compare(this, o);
        } // if
    }

    /**
     * 肯定过滤
     * 
     * @param predicate 谓词判断函数 tuple2->boolean
     * @return 新生的IRecord
     */
    default IRecord filter(Predicate<Tuple2<String, Object>> predicate) {
        return this.tupleS().filter(predicate).collect(IRecord.recclc(e -> e));
    }

    /**
     * 肯定过滤
     * 
     * @param keys 保留的键名索引序列，键名索引从0开始
     * @return 新生的IRecord
     */
    default IRecord filter(final Integer... idices) {
        final List<String> kk = this.keys();
        final int n = kk.size();
        final IRecord rec = this.build();

        Arrays.stream(idices).filter(i -> i >= 0 && i < n).map(kk::get).forEach(key -> {
            rec.add(key, this.get(key));
        });

        return rec;
    }

    /**
     * 肯定过滤
     * 
     * @param keys 保留的键名序列，键名之间采用半角逗号分隔
     * @return 新生的IRecord
     */
    default IRecord filter(final String keys) {
        return this.filter(keys.split("[,]+"));
    }

    /**
     * 肯定过滤
     * 
     * @param keys 保留的键名序列
     * @return 新生的IRecord
     */
    default IRecord filter(final String[] keys) {
        return this.filter(Arrays.asList(keys));
    }

    /**
     * 肯定过滤
     * 
     * @param keys 保留的键名序列
     * @return 新生的IRecord
     */
    default IRecord filter(final List<String> keys) {
        final IRecord rec = this.build();

        keys.forEach(key -> {
            final String _key = key.strip();
            final Object value = this.get(_key);
            if (value != null) {
                rec.add(key, value);
            }
        });

        return rec;
    }

    /**
     * 否定过滤
     * 
     * @param idices 剔除的键名索引序列，键名索引从0开始
     * @return 新生的IRecord
     */
    default IRecord filterNot(final Integer... idices) {
        final int n = this.size();
        final List<Integer> ids = Arrays.asList(idices);
        final Integer[] _indices = Stream.iterate(0, i -> i < n, i -> i + 1).filter(i -> !ids.contains(i))
                .toArray(Integer[]::new);

        return this.filter(_indices);
    }

    /**
     * 否定过滤
     * 
     * @param keys 剔除的键名序列，键名之间采用半角逗号分隔
     * @return 新生的IRecord
     */
    default IRecord filterNot(final String keys) {
        return this.filterNot(keys.split("[,]+"));
    }

    /**
     * 否定过滤
     * 
     * @param keys 剔除的键名序列
     * @return 新生的IRecord
     */
    default IRecord filterNot(final String[] keys) {
        return this.filterNot(Arrays.asList(keys));
    }

    /**
     * 否定过滤
     * 
     * @param keys 剔除的键名序列
     * @return 新生的IRecord
     */
    default IRecord filterNot(final List<String> keys) {
        final BiPredicate<String, Object> bipredicate = (key, v) -> !keys.contains(key);
        return this.filter(bipredicate);
    }

    /**
     * 转换成 tuple2 的 流式结构
     * 
     * @return tuple的流结构 [(k,v)]
     */
    default Stream<Tuple2<String, Object>> tupleS() {
        return this.toMap().entrySet().stream().map(e -> Tuple2.of(e.getKey(), e.getValue()));
    }

    /**
     * 键名过滤
     * 
     * @param bipredicate 过滤函数 (key,value)->bool, true 值保留, false 剔除
     * @return 新生的IRecord
     */
    default IRecord filter(final BiPredicate<String, Object> bipredicate) {
        final IRecord rec = this.build(); // 空IRecord
        this.toMap().entrySet().stream().filter(tup -> bipredicate.test(tup.getKey(), tup.getValue()))
                .forEach(e -> rec.add(e.getKey(), e.getValue()));
        return rec;
    }

    /**
     * 把 IRecord 值转换成 Object 一维数组
     * 
     * @return Object 一维数组
     */
    default Object[] toArray() {
        return this.toArray(e -> e);
    }

    /**
     * 把 IRecord 值转换成 数组结构
     * 
     * @param <X>    mapper 参数类型
     * @param <T>    mapper 值类型
     * @param mapper 值的变换函数 x->t
     * @return T类型的一维数组
     */
    @SuppressWarnings("unchecked")
    default <X, T> T[] toArray(final Function<X, T> mapper) {
        final List<Class<?>> classes = this.tupleS().map(x -> mapper.apply((X) x._2)).filter(Objects::nonNull)
                .map(e -> (Class<Object>) e.getClass()).distinct().collect(Collectors.toList());
        final Class<?> clazz = classes.size() > 1 // 类型不唯一
                ? classes.stream().allMatch(e -> Number.class.isAssignableFrom(e)) // 数字类型
                        ? Number.class // 数字类型
                        : Object.class // 节点类型
                : classes.get(0); // 类型唯一
        return this.tupleS().map(x -> mapper.apply((X) x._2)).toArray(n -> (T[]) Array.newInstance(clazz, n));
    }

    /**
     * 智能版的数组转换 <br>
     * 视键值对儿kvp的值为单值类型(非集合类型[比如List,Set,HashMap等]),比如 <br>
     * Integer,Long,Double等，把当前集合中的值集合转换成 一维数组<br>
     * 
     * 使用示例：<br>
     * IRecord.rb("name,birth,marry") // 档案结构 <br>
     * .get("zhangsan,19810713,20011023".split(",")) // 构建张三的数据记录 <br>
     * .arrayOf("birth,marry",IRecord::asLocalDate, // 把 出生日期和结婚日期转换为日期类型 <br>
     * ldts->ldts[0].until(ldts[1]).getYears()); // 计算张三的结婚年龄 <br>
     * 
     * @param <T>    数组的元素类型
     * @param <U>    mapper 目标元素的类型
     * @param mapper [t]->u 数组变换函数
     * @return U类型结果
     */
    @SuppressWarnings("unchecked")
    default <T, U> U arrayOf(final Function<T[], U> mapper) {
        final Object[] oo = this.toArray();
        U u = null;
        try {
            u = mapper.apply((T[]) oo);
        } catch (Exception e) {
            // do nothing
        } // try

        return u;
    }

    /**
     * 非更新式添加,即在一个复制品上添加新的键值数据<br>
     * 添加键值对数据
     * 
     * @param rec 键值对集合
     * @return 添加了新的减值数据的复制品
     */
    default IRecord derive(final IRecord rec) {
        return this.duplicate().add(rec);
    }

    /**
     * 非更新式添加,即在一个复制品上添加新的键值数据<br>
     * 添加键值对数据
     * 
     * @param tups 键值对集合
     * @return 添加了新的减值数据的复制品
     */
    default IRecord derive(final Object... tups) {
        final IRecord rec = this.duplicate(); // 复制品
        IRecord.slidingS(Arrays.asList(tups), 2, 2, true).forEach(aa -> {
            rec.add(aa.get(0).toString(), aa.get(1));
        }); // forEach
        return rec;
    }

    /**
     * 不存在则计算
     * 
     * @param <T>    值类型
     * @param key    健名
     * @param mapper 健名映射 k->t
     * @return T 类型的结果
     */
    @SuppressWarnings("unchecked")
    default <T> T computeIfAbsent(final String key, final Function<String, T> mapper) {
        return this.opt(key).map(e -> (T) e).orElseGet(() -> {
            final T value = mapper.apply(key);
            this.add(key, value);
            return value;
        });
    }

    /**
     * 不存在则计算
     * 
     * @param <T>    值类型
     * @param @param idx 键名索引，从0开始
     * @param mapper 健名映射 k->t
     * @return T 类型的结果
     */
    default <T> T computeIfAbsent(final Integer idx, final Function<String, T> mapper) {
        return this.computeIfAbsent(this.keyOf(idx), mapper);
    }

    /**
     * 不存在则计算
     * 
     * @param <T>
     * @param key    健名
     * @param mapper 健名映射 k->t
     * @return T 类型的结果
     */
    default <T> T computeIfPresent(final String key, final Function<String, T> mapper) {
        return this.opt(key).map(v -> {
            final T t = mapper.apply(key);
            this.add(key, t);
            return t;
        }).orElse(null);
    }

    /**
     * 不存在则计算
     * 
     * @param <T>
     * @param @param idx 键名索引，从0开始
     * @param mapper 健名映射 k->t
     * @return T 类型的结果
     */
    default <T> T computeIfPresent(final Integer idx, final Function<String, T> mapper) {
        return this.computeIfPresent(this.keyOf(idx), mapper);
    }

    /**
     * 生成 构建器
     * 
     * @param n     键数量,正整数
     * @param keyer 键名生成器 (i:从0开始)->key
     * @return IRecord 构造器
     */
    public static Builder rb(final int n, Function<Integer, ?> keyer) {

        final List<?> keys = Stream.iterate(0, i -> i < n, i -> i + 1).map(keyer).collect(Collectors.toList());
        return new Builder(keys);
    }

    /**
     * 生成 构建器
     * 
     * @param K    元素类型
     * @param keys 键名序列
     * @return keys 为格式的 构建器
     */
    @SafeVarargs
    public static <T> Builder rb(final T... keys) {

        final List<String> _keys = Arrays.asList(keys).stream().map(e -> e + "").collect(Collectors.toList());
        return new Builder(_keys);
    }

    /**
     * 生成 构建器
     * 
     * @param keys 键名序列, 用半角逗号 “,” 分隔
     * @return keys 为格式的 构建器
     */
    public static Builder rb(final String keys) {

        return rb(keys.split(","));
    }

    /**
     * 生成 构建器
     * 
     * @param keys 键名序列
     * @return keys 为格式的 构建器
     */
    public static Builder rb(final String[] keys) {

        return rb(Arrays.asList(keys));
    }

    /**
     * 生成 构建器
     * 
     * @param keys 键名序列
     * @return keys 为格式的 构建器
     */
    public static <T> Builder rb(final Iterable<T> keys) {

        return new Builder(keys);
    }

    /**
     * fill_template 的别名<br>
     * Term.FT("$0+$1=$2",1,2,3) 替换后的结果为 1+2=3
     * 
     * @param template 模版字符串，占位符$0,$1,$2,...
     * @param tt       模版参数序列
     * @return template 被模版参数替换后的字符串
     */
    public static String FT(final String template, final Object... tt) {
        final IRecord rec = IRecord.rb(tt.length, i -> "$" + i).get(tt);
        return fill_template(template, rec);
    }

    /**
     * fill_template 的别名<br>
     * 把template中的占位符用rec中对应名称key的值value给予替换,默认 非null值会用 单引号'括起来，<br>
     * 对于使用$开头/结尾的占位符,比如$name或name$等不采用单引号括起来。<br>
     * 还有对于 数值类型的值value不论占位符/key是否以$开头或结尾都不会用单引号括起来 。<br>
     * 例如 : FT("insert into tblname$ (name,sex) values
     * (#name,#sex)",REC("tblname$","user","#name","张三","#sex","男")) <br>
     * 返回: insert into user (name,sex) values ('张三','男') <br>
     * 
     * @param template           模版字符串
     * @param placeholder2values 关键词列表:占位符/key以及与之对应的值value集合
     * @return 把template中的占位符/key用placeholder2values中的值value给予替换
     */
    public static String FT(final String template, final IRecord placeholder2values) {
        return fill_template(template, placeholder2values);
    }

    /**
     * 把template中的占位符用rec中对应名称key的值value给予替换,默认 非null值会用 单引号'括起来，<br>
     * 对于使用$开头/结尾的占位符,比如$name或name$等不采用单引号括起来,<br>
     * 还有对于 数值类型的值value不论占位符/key是否以$开头或结尾都不会用单引号括起来 。<br>
     * 例如 : fill_template("insert into tblname$ (name,sex) values
     * (#name,#sex)",REC("tblname$","user","#name","张三","#sex","男")) <br>
     * 返回: insert into user (name,sex) values ('张三','男') <br>
     * 
     * @param template           模版字符串
     * @param placeholder2values 关键词列表:占位符/key以及与之对应的值value集合
     * @return 把template中的占位符/key用placeholder2values中的值value给予替换
     */
    public static String fill_template(final String template, final IRecord placeholder2values) {

        if (placeholder2values == null) { // 空值判断
            return template;
        }

        final int len = template.length(); // 模版的长度
        final StringBuilder buffer = new StringBuilder();// 工作缓存
        final List<String> keys = placeholder2values.keys().stream().sorted((a, b) -> -(a.length() - b.length())) // 按照从长到短的顺序进行排序。以保证keys的匹配算法采用的是贪婪算法。
                .collect(Collectors.toList());// 键名 也就是template中的占位符号。
        final Map<Object, List<String>> firstCharMap = keys.stream()
                .collect(Collectors.groupingBy(key -> key.charAt(0)));// keys的首字符集合，这是为了加快
        // 读取的步进速度
        int i = 0;// 当前读取的模版字符位置,从0开始。

        while (i < len) {// 从前向后[0,len)的依次读取模板字符串的各个字符
            // 注意:substring(0,0) 或是 substring(x,x) 返回的是一个长度为0的字符串 "",
            // 特别是,当 x大于等于字符串length会抛异常:StringIndexOutOfBoundsException
            final String line = template.substring(0, i);// 业已读过的模版字符串内容[0,i),当前所在位置为i等于line.length
            String placeholder = null;// 占位符号 的内容
            final List<String> kk = firstCharMap.get(template.charAt(i));// 以 i为首字符开头的keys
            if (kk != null) {// 使用firstCharMap加速步进速度读取模版字符串
                for (final String key : kk) {// 寻找 可以被替换的key
                    final int endIndex = line.length() + key.length(); // 终点索引 exclusive
                    final boolean b = endIndex > len // 是否匹配到placeholder
                            ? false // 拼装的组合串(line+key) 超长（超过模板串的长度)
                            : template.substring(line.length(), endIndex).equals(key); // 当前位置之后刚好存在一个key模样的字符串
                    if (b) { // 定位到一个完整的占位符：长度合适（endIndex<=len) && 内容匹配 equals
                        placeholder = key; // 提取key作为占位符
                        break; // 确定了占位符 ,跳出本次i位置的kk循环。
                    } // if b 定位到一个完整的占位符
                } // for key:kk
            } // if kk!=null

            if (placeholder != null) {// 已经发现了占位符号，用rec中的值给予替换
                boolean isnumberic = false;// 默认不是数字格式
                final Object value = placeholder2values.get(placeholder); // 提取占位符内容
                if (placeholder.startsWith("$") || placeholder.endsWith("$"))
                    isnumberic = true; // value 是否是 强制 使用 数字格式，即用$作为前/后缀。
                if (value instanceof Number)
                    isnumberic = true; // 如果值是数字类型就一定会不加引号的。即采用数字格式

                buffer.append((isnumberic || value == null) ? value + "" : "'" + value + "'"); // 为字符串添加引号，数字格式或是null不加引号
                i += placeholder.length();// 步进placeholder的长度，跳着前进
            } else {// 发现了占位符号则附加当前的位置的字符
                buffer.append(template.charAt(i));
                i++;// 后移一步
            } // if placeholder
        } // while

        return buffer.toString(); // 返回替换后的结果值
    }

    /**
     * 构建一个键名键值序列 指定的 IRecord
     * 
     * @param kvs Map结构（IRecord也是Map结构） 或是 键名,键值 序列。即 build(map) 或是
     *            build(key0,value0,key1,vlaue1,...) 的 形式， 特别注意 build(map) 时候，当且仅当
     *            kvs 的只有一个元素，即 build(map0,map1) 会被视为 键值序列
     * @return 新生成的IRecord
     */
    public static IRecord REC(final Object... kvs) {
        return MyRecord.REC(kvs); // 采用 MyRecord 来作为IRecord的默认实现函数
    }

    /**
     * 把 一个 Iterable 对象 转换成 List 结构
     * 
     * @param <T> 元素类型
     * @param itr 可遍历结构
     * @return ArrayList结构的数据
     */
    public static List<Object> iterable2list(final Iterable<?> itr, final Function<Object, Object> mapper) {
        final List<Object> list = new ArrayList<>();
        itr.forEach(e -> {
            if (e instanceof Iterable) {
                Iterable<?> e_itr = (Iterable<?>) e;
                final List<Object> ll = IRecord.iterable2list(e_itr, mapper);
                list.add(ll);
            } else {
                list.add(mapper.apply(e));
            }
        });
        return list;
    }

    /**
     * 把一个数据对象转换为浮点数<br>
     * 对于 非法的数字类型 返回 null
     * 
     * @param <T> 函数的参数类型
     * @return t->dbl
     */
    static <T> Function<T, Double> obj2dbl() {
        return IRecord.obj2dbl(null);
    }

    /**
     * 把一个数据对象转换为浮点数<br>
     * 对于 非法的数字类型 返回 defaultValue <br>
     * 
     * 默认会尝试把时间类型也解释为数字,即 '1970-01-01 08:00:01' <br>
     * 也会被转换成一个 0时区 的 从1970年1月1 即 epoch time 以来的毫秒数<br>
     * 对于 中国 而言 位于+8时区, '1970-01-01 08:00:01' 会被解析为1000
     * 
     * @param <T>          函数的参数类型
     * @param defaultValue 非法的数字类型 返回 的默认值
     * @return t->dbl
     */
    static <T> Function<T, Double> obj2dbl(final Number defaultValue) {
        return obj2dbl(defaultValue, true);
    }

    /**
     * 把一个数据对象转换为浮点数<br>
     * 对于 非法的数字类型 返回 defaultValue
     * 
     * 默认会尝试把时间类型也解释为数字,即 '1970-01-01 08:00:01' <br>
     * 也会被转换成一个 0时区 的 从1970年1月1 即 epoch time 以来的毫秒数<br>
     * 对于 中国 而言 位于+8时区, '1970-01-01 08:00:01' 会被解析为1000
     * 
     * @param <T>          函数的参数类型
     * @param defaultValue 非法的数字类型 返回 的默认值
     * @param timeflag     是否对时间类型数据进行转换, true 表示 开启,'1970-01-01 08:00:01'将会被解析为
     *                     1000,false 不开启 时间类型将会返回defaultValue
     * @return t->dbl
     */
    static <T> Function<T, Double> obj2dbl(final Number defaultValue, final boolean timeflag) {
        return (T obj) -> {
            if (obj instanceof Number) {
                return ((Number) obj).doubleValue();
            }

            Double dbl = defaultValue == null ? null : defaultValue.doubleValue();
            try {
                dbl = Double.parseDouble(obj.toString());
            } catch (Exception e) { //
                if (timeflag) { // 开启了时间解析功能，则尝试 进行事件的数字化转换
                    final LocalDateTime ldt = IRecord.REC("key", obj).ldt("key"); // 尝试把时间转换成数字
                    if (ldt != null) {
                        final ZoneId systemZone = ZoneId.systemDefault(); // 默认时区
                        final ZoneOffset offset = systemZone.getRules().getOffset(ldt); // 时区 offset
                        dbl = ((Number) ldt.toInstant(offset).toEpochMilli()).doubleValue(); // 转换成 epoch time 以来的毫秒数
                    } // if
                }
            } // try

            return dbl;
        };
    }

    /**
     * 把值对象转化成列表结构
     * 
     * @param value 值对象
     * @return 列表结构
     */
    @SuppressWarnings("unchecked")
    public static List<Object> asList(final Object value) {

        if (value instanceof List) {
            return ((List<Object>) value);
        } else if (value instanceof Collection) {
            return new ArrayList<>((Collection<?>) value);
        } else if (value instanceof Iterable) {
            return IRecord.itr2list((Iterable<Object>) value);
        } else if (value instanceof Stream) {
            return ((Stream<Object>) value).collect(Collectors.toList());
        } else if (Objects.nonNull(value) && value.getClass().isArray()) {
            return Arrays.asList((Object[]) value);
        } else {
            final List<Object> aa = new ArrayList<Object>();

            if (value != null) {
                aa.add(value);
            }

            return aa;
        } // if
    }

    /**
     * 把一个值对象转换成LocalDateTime
     * 
     * @param value 值对象
     * @return LocalDateTime
     */
    static LocalDateTime asLocalDateTime(final Object value) {
        final Function<LocalDate, LocalDateTime> ld2ldt = ld -> LocalDateTime.of(ld, LocalTime.of(0, 0));
        final Function<LocalTime, LocalDateTime> lt2ldt = lt -> LocalDateTime.of(LocalDate.of(0, 1, 1), lt);
        final Function<Long, LocalDateTime> lng2ldt = lng -> {
            final Long timestamp = lng;
            final Instant instant = Instant.ofEpochMilli(timestamp);
            final ZoneId zoneId = ZoneId.systemDefault();
            return LocalDateTime.ofInstant(instant, zoneId);
        };

        final Function<Timestamp, LocalDateTime> timestamp2ldt = timestamp -> {
            return lng2ldt.apply(timestamp.getTime());
        };

        final Function<Date, LocalDateTime> dt2ldt = dt -> {
            return lng2ldt.apply(dt.getTime());
        };

        final Function<String, LocalTime> str2lt = line -> {
            LocalTime lt = null;
            for (String format : "HH:mm:ss,HH:mm,HHmmss,HHmm,HH".split("[,]+")) {
                try {
                    lt = LocalTime.parse(line, DateTimeFormatter.ofPattern(format));
                } catch (Exception ex) {
                    // do nothing
                }
                if (lt != null)
                    break;
            }
            return lt;
        };

        final Function<String, LocalDate> str2ld = line -> {
            LocalDate ld = null;
            for (String format : "yyyy-MM-dd,yyyy-M-d,yyyy/MM/dd,yyyy/M/d,yyyyMMdd".split("[,]+")) {
                try {
                    ld = LocalDate.parse(line, DateTimeFormatter.ofPattern(format));
                } catch (Exception ex) {
                    // do nothing
                }
                if (ld != null)
                    break;
            }

            return ld;
        };

        final Function<String, LocalDateTime> str2ldt = line -> {
            LocalDateTime ldt = null;
            final String patterns = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS," //
                    + "yyyy-MM-dd'T'HH:mm:ss.SSSSSS," //
                    + "yyyy-MM-dd'T'HH:mm:ss.SSS," //
                    + "yyyy-MM-dd'T'HH:mm:ss," //
                    + "yyyy-MM-ddTHH:mm:ss.SSSSSSSSS," //
                    + "yyyy-MM-ddTHH:mm:ss.SSSSSS," //
                    + "yyyy-MM-ddTHH:mm:ss.SSS," //
                    + "yyyy-MM-ddTHH:mm:ss," //
                    + "yyyy-MM-dd HH:mm:ss," //
                    + "yyyy-MM-dd HH:mm," //
                    + "yyyy-MM-dd HH," //
                    + "yyyy-M-d H:m:s," //
                    + "yyyy-M-d H:m," //
                    + "yyyy-M-d H," //
                    + "yyyy/MM/dd HH:mm:ss," //
                    + "yyyy/MM/dd HH:mm," //
                    + "yyyy/MM/dd HH," //
                    + "yyyy/M/d H:m:s," //
                    + "yyyy/M/d H:m," //
                    + "yyyy/M/d H," //
                    + "yyyyMMddHHmmss," //
                    + "yyyyMMddHHmm," //
                    + "yyyyMMddHH"//
            ; // patterns 时间的格式字符串

            for (String format : patterns.split("[,]+")) {
                try {
                    ldt = LocalDateTime.parse(line, DateTimeFormatter.ofPattern(format));
                } catch (Exception ex) {
                    // do nothing
                }
                if (ldt != null)
                    break;
            }

            return ldt;
        };

        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        } else if (value instanceof LocalDate) {
            return ld2ldt.apply((LocalDate) value);
        } else if (value instanceof LocalTime) {
            return lt2ldt.apply((LocalTime) value);
        } else if (value instanceof Number) {
            return lng2ldt.apply(((Number) value).longValue());
        } else if (value instanceof Timestamp) {
            return timestamp2ldt.apply(((Timestamp) value));
        } else if (value instanceof Date) {
            return dt2ldt.apply(((Date) value));
        } else if (value instanceof String) {
            final String line = (String) value;
            final LocalDateTime _ldt = str2ldt.apply(line);
            if (Objects.nonNull(_ldt)) {
                return _ldt;
            }
            final LocalDate _ld = str2ld.apply(line);
            if (Objects.nonNull(_ld)) {
                return ld2ldt.apply(_ld);
            }
            final LocalTime _lt = str2lt.apply(line);
            if (Objects.nonNull(_lt)) {
                return lt2ldt.apply(_lt);
            }
            return null;
        } else {
            return null;
        }
    }

    /**
     * 数据滑动<br>
     * 例如 [1,2,3,4,5] 按照 width=2, step=1 进行滑动 <br>
     * flag false: [1, 2][2, 3][3, 4][4, 5][5] 如果 flag = true 则 返回 <br>
     * flag true: [1, 2][2, 3][3, 4][4, 5] <br>
     * 按照 width=2, step=2 进行滑动 <br>
     * flag false: [1, 2][3, 4] <br>
     * flag true: [1, 2][3, 4] <br》
     * 
     * @param <T>  数据元素类型
     * @param aa   数据集合
     * @param size 窗口大小
     * @param step 步长
     * @param flag 是否返回等长记录,true 数据等长 剔除 尾部的 不齐整（小于 size） 的元素,false 包含不齐整
     * @return 滑动窗口列表
     */
    public static <T> Stream<List<T>> slidingS(final T[] aa, final int size, final int step, final boolean flag) {

        return IRecord.slidingS(Arrays.asList(aa), size, step, flag);
    }

    /**
     * 数据滑动<br>
     * 例如 [1,2,3,4,5] 按照 width=2, step=1 进行滑动 <br>
     * flag false: [1, 2][2, 3][3, 4][4, 5][5] 如果 flag = true 则 返回 <br>
     * flag true: [1, 2][2, 3][3, 4][4, 5] <br>
     * 按照 width=2, step=2 进行滑动 <br>
     * flag false: [1, 2][3, 4] <br>
     * flag true: [1, 2][3, 4] <br》
     * 
     * @param <T>        数据元素类型
     * @param collection 数据集合
     * @param size       窗口大小
     * @param step       步长
     * @param flag       是否返回等长记录,true 数据等长 剔除 尾部的 不齐整（小于 size） 的元素,false 包含不齐整
     * @return 滑动窗口列表
     */
    public static <T> Stream<List<T>> slidingS(final Collection<T> collection, final int size, final int step,
            final boolean flag) {

        final int n = collection.size();
        final ArrayList<T> arrayList = collection instanceof ArrayList // 类型检测
                ? (ArrayList<T>) collection // 数组列表类型
                : new ArrayList<T>(collection); // 其他类型

        // 当flag 为true 的时候 i的取值范围是: [0,n-size] <==> [0,n+1-size)
        return Stream.iterate(0, i -> i < (flag ? n + 1 - size : n), i -> i + step) // 序列生成
                .map(i -> arrayList.subList(i, (i + size) > n ? n : (i + size)));
    }

    /**
     * 比较器,需要 键名序列keys中的每个值对象带有比较能力:Comparable
     * 
     * @param keys 键名序列
     * @return keys 序列的比较器
     */
    public static Comparator<IRecord> cmp(final List<String> keys) {
        return IRecord.cmp(keys.toArray(String[]::new), true);
    }

    /**
     * 比较器,需要 键名序列keys中的每个值对象带有比较能力:Comparable
     * 
     * @param <T>    元素类型
     * @param <U>    具有比较能力的类型
     * @param keys   keys 键名序列
     * @param mapper (key:键名,t:键值)->u 比较能力变换器
     * @param asc    是否升序,true 表示升序,小值在前,false 表示降序,大值在前
     * @return keys 序列的比较器
     */
    public static <T, U extends Comparable<?>> Comparator<IRecord> cmp(final List<String> keys,
            final BiFunction<String, T, U> mapper, final boolean asc) {
        return IRecord.cmp(keys.toArray(String[]::new), mapper, asc);
    }

    /**
     * 比较器,需要 键名序列keys中的每个值对象带有比较能力:Comparable
     * 
     * @param keys 键名序列
     * @return keys 序列的比较器
     */
    public static Comparator<IRecord> cmp(final String keys[]) {
        return IRecord.cmp(keys, true);
    }

    /**
     * 比较器,需要 键名序列keys中的每个值对象带有比较能力:Comparable
     * 
     * @param keys 键名序列
     * @param asc  是否升序,true 表示升序,小值在前,false 表示降序,大值在前
     * @return keys 序列的比较器
     */
    public static Comparator<IRecord> cmp(final String keys[], final boolean asc) {
        return cmp(keys, null, asc);
    }

    /**
     * 比较器,需要 键名序列keys中的每个值对象带有比较能力:Comparable
     * 
     * @param <T>    元素类型
     * @param <U>    具有比较能力的类型
     * @param keys   键名序列
     * @param mapper (key:键名,t:键值)->u 比较能力变换器
     * @param asc    是否升序,true 表示升序,小值在前,false 表示降序,大值在前
     * @return keys 序列的比较器
     */
    @SuppressWarnings("unchecked")
    public static <T, U extends Comparable<?>> Comparator<IRecord> cmp(final String keys[],
            final BiFunction<String, T, U> mapper, final boolean asc) {

        final BiFunction<String, T, U> final_mapper = mapper == null
                ? (String i, T o) -> o instanceof Comparable ? (U) o : (U) (o + "")
                : mapper;

        return (a, b) -> {
            final Queue<String> queue = new LinkedList<String>();
            for (String k : keys)
                queue.offer(k);// 压入队列
            while (!queue.isEmpty()) {
                final String key = queue.poll(); // 提取队首元素
                final Comparable<Object> ta = (Comparable<Object>) a.invoke(key, (T t) -> final_mapper.apply(key, t));
                final Comparable<Object> tb = (Comparable<Object>) b.invoke(key, (T t) -> final_mapper.apply(key, t));

                if (ta == null && tb == null)
                    return 0;
                else if (ta == null)
                    return -1;
                else if (tb == null)
                    return 1;
                else {
                    int ret = 0;

                    try {
                        ret = ta.compareTo(tb);// 进行元素比较
                    } catch (Exception e) {
                        final String[] aa = Stream.of(ta, tb).map(o -> o != null ? o.getClass().getName() + o : "null")
                                .toArray(String[]::new);
                        ret = aa[0].compareTo(aa[1]);// 进行元素比较
                    } // try

                    if (ret != 0) {
                        return (asc ? 1 : -1) * ret; // 返回比较结果,如果不相等直接返回,相等则继续比计较
                    } // if
                } // if
            } // while

            return 0;// 所有key都比较完毕,则认为两个元素相等
        };
    }

    /**
     * 二元算术运算符号 除法<br>
     * 
     * 非数字 则 返回第一个值
     * 
     * @param biop 归并器 (t,u)->v
     * @return (record0,record1)->record2
     */
    static BinaryOperator<IRecord> divide() {

        return IRecord.binaryOp((a, b) -> a / b);
    }

    /**
     * 二元算术运算符号 乘法 <br>
     * 
     * 非数字 则 返回第一个值
     * 
     * @param biop 归并器 (t,u)->v
     * @return (record0,record1)->record2
     */
    static BinaryOperator<IRecord> multiply() {

        return IRecord.binaryOp((a, b) -> a * b);
    }

    /**
     * 二元算术运算符号 减法 <br>
     * 
     * 非数字 则 返回第一个值
     * 
     * @param biop 归并器 (t,u)->v
     * @return (record0,record1)->record2
     */
    static BinaryOperator<IRecord> subtract() {

        return IRecord.binaryOp((a, b) -> a - b);
    }

    /**
     * 二元算术运算符号 加法 <br>
     * 
     * 非数字 则 返回第一个值
     * 
     * @param biop 归并器 (t,u)->v
     * @return (record0,record1)->record2
     */
    static BinaryOperator<IRecord> plus() {

        return IRecord.binaryOp((a, b) -> a + b);
    }

    /**
     * 二元算术运算符号 <br>
     * 
     * 非数字 则 返回第一个值
     * 
     * @param biop 归并器 (t,u)->v
     * @return (record0,record1)->record2
     */
    static BinaryOperator<IRecord> binaryOp(final BinaryOperator<Double> biop) {

        return IRecord.combine2((key, tup) -> {
            final Double[] aa = Stream.of(tup._1, tup._2).map(IRecord.obj2dbl()).toArray(Double[]::new);
            if (aa[0] != null && null != aa[1]) {
                return biop.apply(aa[0], aa[1]);
            } else { // 非数字 则 返回第一个值
                return tup._1 == null || tup._1.toString().matches("^\\s*$") ? tup._2() : tup._1;
            } // if
        });
    }

    /**
     * 生成一个 IRecord的二元运算法。 最大值
     * 
     * @param <T>       度量器的数据类型
     * @param quantizer 度量器 t->number
     * @return (record0,record1)->record2
     */
    @SuppressWarnings("unchecked")
    static <T> BinaryOperator<IRecord> max(final Function<T, Number> quantizer) {

        return IRecord.combine2((k, tup) -> {
            final double a = quantizer.apply((T) tup._1).doubleValue();
            final double b = quantizer.apply((T) tup._2).doubleValue();
            return a > b ? tup._1 : tup._2;
        });
    }

    /**
     * 生成一个 IRecord的二元运算法。最小值
     * 
     * @param <T>       度量器的数据类型
     * @param quantizer 度量器 t->number
     * @return (record0,record1)->record2
     */
    @SuppressWarnings("unchecked")
    static <T> BinaryOperator<IRecord> min(final Function<T, Number> quantizer) {

        return IRecord.combine2((k, tup) -> {
            final double a = quantizer.apply((T) tup._1).doubleValue();
            final double b = quantizer.apply((T) tup._2).doubleValue();
            return a < b ? tup._1 : tup._2;
        });
    }

    /**
     * 生成一个 IRecord的二元运算法。
     * 
     * @param <T>    第一参数类型
     * @param <U>    第二参数类型
     * @param <V>    结果类型
     * @param bifunc 归并器 (t,u)->v
     * @return (record0,record1)->record2
     */
    @SuppressWarnings("unchecked")
    static <T, U, V> BinaryOperator<IRecord> combine(final BiFunction<T, U, V> bifunc) {

        return IRecord.combine2((k, tup) -> bifunc.apply((T) tup._1, (U) tup._2));
    }

    /**
     * 生成一个 IRecord的二元运算法。
     * 
     * @param <T>    第一参数类型
     * @param <U>    第二参数类型
     * @param <V>    结果类型
     * @param bifunc 归并器 (k:键名,(t:左侧元素,u:右侧元素))->v
     * @return (record0,record1)->record2
     */
    @SuppressWarnings("unchecked")
    static <T, U, V> BinaryOperator<IRecord> combine2(final BiFunction<String, Tuple2<T, U>, V> bifunc) {

        return IRecord.combine4((tup1, tup2) -> bifunc.apply(tup1._2, (Tuple2<T, U>) tup2));
    }

    /**
     * 生成一个 IRecord的二元运算法。
     * 
     * @param <T>    第一参数类型
     * @param <U>    第二参数类型
     * @param <V>    结果类型
     * @param bifunc 归并器 (i:键名索引,(t:左侧元素,u:右侧元素))->v
     * @return (record0,record1)->record2
     */
    @SuppressWarnings("unchecked")
    static <T, U, V> BinaryOperator<IRecord> combine3(final BiFunction<Integer, Tuple2<T, U>, V> bifunc) {

        return IRecord.combine4((tup1, tup2) -> bifunc.apply(tup1._1, (Tuple2<T, U>) tup2));
    }

    /**
     * 生成一个 IRecord的二元运算法。
     * 
     * @param <T>    第一参数类型
     * @param <U>    第二参数类型
     * @param <V>    结果类型
     * @param bifunc 归并器 ((i:键名索引,k:键名),(t:左侧元素,u:右侧元素))->v
     * @return (record0,record1)->record2
     */
    @SuppressWarnings("unchecked")
    static <T, U, V> BinaryOperator<IRecord> combine4(
            final BiFunction<Tuple2<Integer, String>, Tuple2<T, U>, V> bifunc) {

        return (record_left, record_right) -> {
            final List<String> keys_left = record_left.keys();
            final List<String> keys_right = record_right.keys();
            final List<String> keys = Stream.concat(keys_left.stream(), keys_right.stream()).distinct()
                    .collect(Collectors.toList());
            final Builder rb = IRecord.rb(keys); // 返回结果的构建器
            final AtomicInteger ai = new AtomicInteger();
            final Object[] values = keys.stream().map(k -> {
                Object value = null;
                try {
                    final int i = ai.getAndIncrement(); // 记录键名索引
                    final T left = (T) record_left.get(k);
                    final U right = (U) record_right.get(k);
                    value = bifunc.apply(Tuple2.TUP2(i, k), Tuple2.TUP2(left, right)); // 返回结果
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return value;
            }).toArray(); // 计算结果值

            return rb.get(values);
        }; // BinaryOperator
    }

    /**
     * 可枚举 转 列表
     * 
     * @param <T>      元素类型
     * @param iterable 可枚举类
     * @param maxSize  最大的元素数量
     * @return 元素列表
     */
    public static <T> List<T> itr2list(final Iterable<T> iterable, final int maxSize) {

        final Stream<T> stream = StreamSupport.stream(iterable.spliterator(), false).limit(maxSize);
        return stream.collect(Collectors.toList());
    }

    /**
     * 可枚举 转 列表
     * 
     * @param <T>      元素类型
     * @param iterable 可枚举类
     * @param maxSize  最大的元素数量
     * @return 元素列表
     */
    public static <T> List<T> itr2list(final Iterable<T> iterable) {

        return IRecord.itr2list(iterable, MAX_SIZE);
    }

    /**
     * Record类型的T元素归集器
     * 
     * @param <T> 元组值类型
     * @return IRecord类型的T元素归集器
     */
    public static <T, U> Collector<Tuple2<String, T>, ?, IRecord> recclc() {
        return IRecord.recclc(e -> e);
    }

    /**
     * Record类型的T元素归集器
     * 
     * @param <T>    元素类型
     * @param <U>    元组的1#位置占位符元素类型
     * @param mapper Tuple2 类型的元素生成器 t->(str,u)
     * @return IRecord类型的T元素归集器
     */
    public static <T, U> Collector<T, ?, IRecord> recclc(final Function<T, Tuple2<String, U>> mapper) {
        return Collector.of((Supplier<List<T>>) ArrayList::new, List::add, (left, right) -> {
            left.addAll(right);
            return left;
        }, (ll) -> { // finisher
            final IRecord rec = REC(); // 空对象
            ll.stream().map(mapper).forEach(rec::add);
            return rec;
        }); // Collector.of
    }

    /**
     * Map类型的T元素归集器
     * 
     * @param <K>    键类型
     * @param <T>    元素类型
     * @param <U>    元组的1#位置占位符元素类型
     * @param mapper Tuple2 类型的元素生成器 t->(k,u)
     * @return IRecord类型的T元素归集器
     */
    public static <T, K, U> Collector<T, ?, Map<K, List<U>>> mapclc(final Function<T, Tuple2<K, U>> mapper) {
        return Collector.of((Supplier<Map<K, List<U>>>) HashMap::new, (tt, t) -> {
            final Tuple2<K, U> tup = mapper.apply(t);
            tt.computeIfAbsent(tup._1, _k -> new ArrayList<>()).add(tup._2);
        }, (left, right) -> {
            left.putAll(right);
            return left;
        }, (tt) -> { // finisher
            return tt;
        }); // Collector.of
    }

    /**
     * Map类型的T元素归集器
     * 
     * @param <K>    键类型
     * @param <T>    元素类型
     * @param <U>    元组的1#位置占位符元素类型
     * @param mapper Tuple2 类型的元素生成器 t->(k,u)
     * @param biop   二元运算算子 (u,u)->u
     * @return IRecord类型的T元素归集器
     */
    public static <T, K, U> Collector<T, ?, Map<K, U>> mapclc2(final Function<T, Tuple2<K, U>> mapper,
            final BinaryOperator<U> biop) {
        return Collector.of((Supplier<Map<K, List<U>>>) HashMap::new, (tt, t) -> {
            final Tuple2<K, U> tup = mapper.apply(t);
            tt.computeIfAbsent(tup._1, _k -> new ArrayList<>()).add(tup._2);
        }, (left, right) -> {
            left.putAll(right);
            return left;
        }, (tt) -> { // finisher
            Map<K, U> map = new LinkedHashMap<K, U>();
            tt.forEach((k, uu) -> {
                final U u = uu.stream().reduce(biop).orElse(null);
                map.put(k, u);
            });
            return map;
        }); // Collector.of
    }

    /**
     * Map类型的T元素归集器
     * 
     * @param <K>    键类型
     * @param <T>    元素类型
     * @param <U>    元组的1#位置占位符元素类型
     * @param mapper Tuple2 类型的元素生成器 t->(k,u)
     * @return IRecord类型的T元素归集器
     */
    public static <T, K, U> Collector<T, ?, Map<K, U>> mapclc2(final Function<T, Tuple2<K, U>> mapper) {
        return IRecord.mapclc2(mapper, (a, b) -> b); // 使用新值覆盖老值。
    }

    /**
     * Map类型的T元素归集器
     * 
     * @param <K>    键类型
     * @param <T>    元素类型
     * @param <U>    元组的1#位置占位符元素类型
     * @param mapper Tuple2 类型的元素生成器 t->(k,u)
     * @return IRecord类型的T元素归集器
     */
    public static <K, T> Collector<? super Tuple2<K, T>, ?, Map<K, T>> mapclc2() {
        return IRecord.mapclc2(e -> e, (a, b) -> b); // 使用新值覆盖老值。
    }

    /**
     * 最大数字数量,默认为10000
     */
    public static int MAX_SIZE = 10000; // 最大的数量

    /**
     * IRecord 构建器
     * 
     * @author gbench
     *
     */
    public static class Builder {

        /**
         * 构造IRecord构造器
         * 
         * @param keys 键名列表的迭代器
         */
        public <T> Builder(final Iterable<T> keys) {
            this.keys = StreamSupport.stream(keys.spliterator(), false).limit(10000).map(e -> e + "")
                    .collect(Collectors.toList());
        }

        public IRecord build(final Object... kvs) {
            return MyRecord.REC(kvs);
        }

        /**
         * 构造 IRecord <br>
         * 按照构建器的 键名序列表，依次把objs中的元素与其适配以生成 IRecord <br>
         * {key0:objs[0],key1:objs[1],key2:objs[2],...}
         * 
         * @param <T>  元素类型
         * @param objs 值序列, 若 objs 为 null 则返回null, <br>
         *             若 objs 长度不足以匹配 keys 将采用 循环补位的仿制给予填充 <br>
         *             若 objs 长度为0则返回一个空对象{},注意是没有元素且不是null的对象
         * @return IRecord 对象 若 objs 为 null 则返回null
         */
        public IRecord get(final Object... objs) {
            if (objs == null) { // 空值判断
                return null;
            }

            final int n = objs.length;
            final int size = this.keys.size();
            final LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>();

            for (int i = 0; n > 0 && i < size; i++) {
                final String key = keys.get(i);
                final Object value = objs[i % n];
                data.put(key, value == null ? "" : value); // key 默认为 ""
            } // for

            return this.build(data);
        }

        /**
         * 键名列表
         */
        private List<String> keys = new ArrayList<>();
    }

}
