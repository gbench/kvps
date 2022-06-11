package gbench.util.json.jackson;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

import gbench.util.lisp.IRecord;

/**
 *
 * @author gbench
 *
 */
public class IRecordDeserializer extends StdDeserializer<IRecord> {

    private static final long serialVersionUID = 637227298143614828L;

    /**
     *
     */
    public IRecordDeserializer() {
        super(IRecord.class);
    }

    /**
     * 单层节点转换的ObjectNode变为IRecord
     * 
     * @param node
     * @return IRecord 对象
     */
    public static IRecord objnode2rec(ObjectNode node) {
        Map<String, Object> mm = new LinkedHashMap<>();
        node.fieldNames().forEachRemaining(name -> mm.put(name, node.get(name)));
        // System.out.println(mm);
        return IRecord.REC(mm);
    }

    /**
     * 单层节点转换的ObjectNode变为IRecord
     * 
     * @param node 节点
     * @return
     */
    public static Function<ObjectNode, IRecord> node2rec = IRecordDeserializer::objnode2rec;

    @Override
    public IRecord deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {

        JsonNode node = jp.getCodec().readTree(jp);
        Map<String, Object> mm = new LinkedHashMap<>();
        node.fieldNames().forEachRemaining(name -> {
            JsonNode jsn = node.get(name);
            if (jsn.isObject()) {
                mm.put(name, objnode2rec((ObjectNode) node.get(name)));
            } else {
                Object value = jsn;
                if (jsn.isTextual()) {
                    value = jsn.asText();
                } else if (jsn.isDouble()) {
                    value = jsn.asDouble();
                } else if (jsn.isInt()) {
                    value = jsn.asInt();
                } else if (jsn.isBoolean()) {
                    value = jsn.asBoolean();
                } else if (jsn.isFloat()) {
                    value = jsn.asDouble();
                } else if (jsn.isLong()) {
                    value = jsn.asLong();
                } // if
                mm.put(name, value);
            } // if
        });
        return IRecord.REC(mm);
    }
}