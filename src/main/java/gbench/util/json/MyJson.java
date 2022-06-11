package gbench.util.json;

import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gbench.util.lisp.IRecord;
import gbench.util.json.jackson.IRecordModule;

/**
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
     * 
     * @param obj
     * @return
     */
    public static String toJson(Object obj) {
        try {
            return recM().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 
     * @param obj
     * @return
     */
    public static String pretty(Object obj) {
        try {
            return MyJson.recM().writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

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
