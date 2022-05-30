package gbench.util.lisp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * 数据记录对象的实现
 * 
 * @author gbench
 *
 */
public class MyRecord implements IRecord, Serializable {

    /**
     * 
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
     * 构造空数据记录
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
        final ObjectMapper objM = new ObjectMapper();
        IRecord rec = null;
        try {
            rec = IRecord.REC(objM.readValue(json, Map.class));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            System.err.println("error json:\n"+json);
        }
        return rec;
    }

    /**
     * 转换成 json 格式
     * 
     * @param obj
     * @return
     */
    public static String toJson(final Object obj) {
        final ObjectMapper objM = new ObjectMapper();
        objM.registerModule(new JavaTimeModule());
        String json = null;
        try {
            json = objM.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
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

        if (n == 1) { // 单一参数情况
            final T obj = kvs[0];
            if (obj instanceof Map) { // Map情况的数据处理
                ((Map<?, ?>) obj).forEach((k, v) -> { // 键值的处理
                    data.put(k + "", v);
                }); // forEach
            } else if (obj instanceof IRecord) {// IRecord 对象类型 复制对象数据
                data.putAll(((IRecord) obj).toMap());
            } else if (obj instanceof String) { // 字符串类型的单个参数
                final IRecord rec = MyRecord.fromJson(obj.toString()); // 尝试解析json
                if (rec != null) {
                    data.putAll(rec.toMap());
                }
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