package gbench.util.json.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import gbench.util.lisp.*;

public class IRecordModule extends SimpleModule {

    /**
     * 序列号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 构造函数
     */
    public IRecordModule() {
        super("IRecord Serializer", new Version(0, 0, 1, "0.0.1-SNAPSHOT", "gbench.tartarus", "common"));
        this.addDeserializer(IRecord.class, new IRecordDeserializer());
        this.addSerializer(new IRecordSerializer());
    }

}
