package gbench.util.json;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;

import gbench.util.lisp.IRecord;
import gbench.util.json.jackson.IRecordModule;

/**
 * Json 工具类
 * 
 * @author xuqinghua
 *
 */
public class MyJson {

    /**
     * 
     * @param modules
     * @return
     */
    public static ObjectMapper of(final Module... modules) {
        final ObjectMapper objM = new ObjectMapper();
        final List<Module> list = new ArrayList<Module>();

        for (final Module m : modules) {
            list.add(m);
        }

        list.stream().distinct().forEach(e -> {
            objM.registerModule(e);
        });

        objM.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true); // 允许省略字段名的引号

        return objM;
    }

    /**
     * 带有 IRecord 类型解析功能的 Mapper
     * 
     * @param key objM 的缓存键名
     * @return 默认的 注册了 time 与 IRecord的 ObjectMapper
     */
    public static ObjectMapper recM(final Object key) {
        return objMCache.computeIfAbsent(key, k -> {
            // 日期序列化设置
            final JavaTimeModule javaTimeModule = new JavaTimeModule();
            javaTimeModule.addSerializer(Date.class,
                    new DateSerializer(false, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")));
            javaTimeModule.addSerializer(LocalDateTime.class,
                    new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            javaTimeModule.addSerializer(LocalDate.class,
                    new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            javaTimeModule.addSerializer(LocalTime.class,
                    new LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")));

            // IRecord 序列化设置
            final IRecordModule recModule = new IRecordModule();

            return of(recModule, javaTimeModule);
        });
    }

    /**
     * 带有 IRecord 类型解析功能的 Mapper
     * 
     * @return ObjectMapper
     */
    public static ObjectMapper recM() {

        return recM("default");
    }

    /**
     * 生成 json 字符串
     * 
     * @param obj 目标对象
     * @return 生成 json 字符串
     */
    public static String toJson(final Object obj) {
        try {
            return toJson2(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 生成 json 字符串, 带有 异常抛出
     * 
     * @param obj 目标对象
     * @return 生成 json 字符串
     */
    public static String toJson2(final Object obj) throws JsonProcessingException {

        return recM().writeValueAsString(obj);

    }

    /**
     * 返回美化后的json字符串
     * 
     * @param obj 目标对象
     * @return 美化后的json字符串
     */
    public static String pretty(final Object obj) {
        try {
            return MyJson.recM().writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 把json字符串转成IRecord 对象
     * 
     * @param json json字符串转
     * @return IRecord 对象
     */
    public static IRecord fromJson(final String json) {
        IRecord rec = null;

        try {
            rec = fromJson2(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return rec;
    }

    /**
     * 把json字符串转成IRecord 对象 带有 异常抛出
     * 
     * @param json json字符串转
     * @return IRecord 对象
     */
    public static IRecord fromJson2(final String json) throws JsonProcessingException {
        final IRecord rec = recM().readValue(json, IRecord.class);

        return rec;
    }

    public static Map<Object, ObjectMapper> objMCache = new HashMap<Object, ObjectMapper>(); // objM 对象的缓存

}
