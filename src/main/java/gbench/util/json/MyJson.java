package gbench.util.json;

import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    public static ObjectMapper of(final com.fasterxml.jackson.databind.Module... modules) {
        final ObjectMapper objM = new ObjectMapper();
        final List<com.fasterxml.jackson.databind.Module> list = new ArrayList<com.fasterxml.jackson.databind.Module>();
        for (final com.fasterxml.jackson.databind.Module m : modules) {
            list.add(m);
        }

        list.stream().distinct().forEach(e -> {
            objM.registerModule(e);
        });

        return objM;
    }

    /**
     * 带有 IRecord 类型解析功能的 Mapper
     * 
     * @return ObjectMapper
     */
    public static ObjectMapper recM() {
        return of(new IRecordModule());
    }

    /**
     * 生成 json 字符串
     * 
     * @param obj 目标对象
     * @return 生成 json 字符串
     */
    public static String toJson(final Object obj) {
        try {
            return recM().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
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
            rec = recM().readValue(json, IRecord.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return rec;
    }

}
