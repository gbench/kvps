package gbench.util.json.jackson;

import java.util.Map;

import gbench.util.lisp.IRecord;

/**
 * IRecord 序列化
 * 
 * @author gbench
 *
 */
public class IRecordSerializer extends AbstractSerializer<IRecord> {

    private static final long serialVersionUID = -6713069486531158400L;

    /**
     * 序列化 <br>
     * 构造函数
     */
    public IRecordSerializer() {
        super(IRecord.class);
    }

    @Override
    public Map<String, Object> dataOf(final IRecord t) {
        return t.toMap();
    }

}