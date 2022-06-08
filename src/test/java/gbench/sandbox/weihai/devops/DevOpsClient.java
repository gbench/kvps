package gbench.sandbox.weihai.devops;

import static gbench.util.io.Output.println;
import static gbench.util.lisp.IRecord.FT;
import static gbench.util.lisp.IRecord.REC;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import gbench.util.lisp.IRecord;

public class DevOpsClient extends MyHttpClient {

    public String access_token() {
        // 获取参数
        final IRecord token_resp = send2(FT("$0$1", host, "/auth/oauth/token"), REC( //
                "$entity_type", "url_encoded_form_entity", "$header",
                REC("Authorization", "Basic Y2xpZW50OnNlY3JldA=="), //
                "username", "TOKEN_API", //
                "password", "U2FsdGVkX18qz8S9JC", //
                "scope", "all", //
                "grant_type", "password"));
        println(token_resp);
        final String access_token = token_resp.str("access_token"); // 提取访问token
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
     * @return 时间戳对象id
     */
    public String oid() {
        final DateTimeFormatter dtf=DateTimeFormatter.ofPattern("yyyyMMddhhmmssSSSSSSS");
        return LocalDateTime.now().format(dtf);
    }

    String host = "http://10.24.24.53:8182";
}
