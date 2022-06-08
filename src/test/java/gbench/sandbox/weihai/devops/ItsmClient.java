package gbench.sandbox.weihai.devops;

import static gbench.util.lisp.IRecord.FT;
import static gbench.util.lisp.IRecord.REC;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import gbench.util.lisp.IRecord;

public class ItsmClient extends MyHttpClient {
    
    /**
     * 
     * @return
     */
    public String access_token() {
        final String access_token = this.oid(); // 提取访问token
        return access_token;
    }
    
    /**
     * 
     * @param url
     * @return
     */
    public String apiOf(String url) {
        final String api = FT("$0$1", host, url);
        return api;
    }
    
    /**
     * 
     * @param api
     * @param params
     * @return
     */
    public IRecord post_json(final String api, final IRecord params) {
        final IRecord req0 = REC( //
                "$method", "post", "$entity_type", "json", "$header", REC("Authorization", access_token()));
        final IRecord req_params = req0.derive((params));
        return DevOpsClient.send2(api, req_params);
    }

    /**
     * 
     * @param api
     * @param params
     * @return
     */
    public IRecord get(final String api, final IRecord params) {
        final IRecord req0 = REC( //
                "$method", "get", "$header", REC("Authorization", access_token()));
        final IRecord req_params = req0.derive((params));
        return DevOpsClient.send2(api, req_params);
    }
    
    /**
     * 时间戳对象id
     * 
     * @return 时间戳对象id
     */
    public String oid() {
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddhhmmssSSSSSSS");
        return LocalDateTime.now().format(dtf);
    }

    
    private String host = "http://localhost:8089/kvps";

}
