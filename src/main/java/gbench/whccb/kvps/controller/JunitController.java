package gbench.whccb.kvps.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gbench.util.lisp.IRecord;

/**
 * 测试Controller
 * 
 * @author xuqinghua
 *
 */
@RequestMapping("junit")
@RestController
public class JunitController {

    /**
     * http://localhost:8089/kvps/junit/sendData
     * 
     * @param data 数据列表
     * @return IRecord
     */
    @RequestMapping(value = "sendData", consumes = "application/json")
    public IRecord sendData(final @RequestBody IRecord data) {
        final IRecord rec = IRecord.REC("code", 0);
        rec.add("data", data);
        return rec;
    }

    /**
     * http://localhost:8089/kvps/junit/sendData2
     * 
     * @param name 名称
     * @param keys 键名列表
     * @return IRecord
     */
    @RequestMapping(value = "sendData2")
    public IRecord sendData2(final String name, final String keys) {
        final IRecord rec = IRecord.REC("code", 0);
        final IRecord data = IRecord.REC("name", name, "keys", keys);
        rec.add("data", data);
        return rec;
    }

}
