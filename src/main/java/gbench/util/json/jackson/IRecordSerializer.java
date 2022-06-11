package gbench.util.json.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import gbench.util.lisp.IRecord;
import gbench.util.lisp.Tuple2;

/**
 * IRecord 序列化
 * 
 * @author gbench
 *
 */
public class IRecordSerializer extends StdSerializer<IRecord> {

    private static final long serialVersionUID = -6713069486531158400L;

    /**
     * 序列化 <br>
     * 构造函数
     */
    public IRecordSerializer() {
        super(IRecord.class);
    }

    @Override
    public void serializeWithType(final IRecord value, final JsonGenerator generator, final SerializerProvider provider,
            final TypeSerializer typeSer) throws IOException {
        this.serialize(value, generator, provider);
    }

    @Override
    public void serialize(final IRecord value, final JsonGenerator generator, final SerializerProvider provider)
            throws IOException {
        generator.writeStartObject();
        for (final Tuple2<String, Object> kvp : value.tuples()) {
            generator.writeObjectField(kvp._1, kvp._2);
        }
        generator.writeEndObject();
    }

}