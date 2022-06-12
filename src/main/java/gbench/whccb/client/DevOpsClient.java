package gbench.whccb.client;

import static gbench.util.io.Output.println;
import static gbench.util.lisp.IRecord.FT;
import static gbench.util.lisp.IRecord.REC;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
     * 
     * @return 时间戳对象id
     */
    public String oid() {
        synchronized (DevOpsClient.class) { // 保持时间同步
            final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddhhmmssSSSSSSS");
            try {
                Thread.sleep(10); //
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return LocalDateTime.now().format(dtf);
        }
    }

    /**
     * 根据流程ID提取审批节点(流程审批记录)
     * 
     * @param flowId 流程id
     * @return
     */
    public List<Map<String, Object>> approvalNodes(final String flowId) {
        final List<Map<String, Object>> nodes = new ArrayList<Map<String, Object>>();
        Arrays.stream("".split("[,]+"))
                .map(e -> REC("actionName", "同意", "activityName", "项目申请部门-总经理-审批", "createDate",
                        LocalDate.now().toString(), "createUserName", "zhangsan", "msg", "项目申请部门-总经理-审批-同意"))
                .map(e -> e.toMap2()).forEach(nodes::add);

        return nodes;
    }

    private String host = "http://10.24.24.53:8182";
}
