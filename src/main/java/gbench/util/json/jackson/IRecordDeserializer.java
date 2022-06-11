package gbench.util.json.jackson;

import java.util.Map;

import gbench.util.lisp.IRecord;

/**
 * IRecord 反序列化 <br>
 * 
 * @author gbench
 *
 */
public class IRecordDeserializer extends AbstractDeserializer<IRecord> {

    private static final long serialVersionUID = 637227298143614828L;

    /**
     * 反序列化 <br>
     * 构造函数
     */
    public IRecordDeserializer() {
        super(IRecord.class);
    }

    @Override
    public IRecord create(final Map<String, Object> tuples) {
        return IRecord.REC(tuples);
    }

}