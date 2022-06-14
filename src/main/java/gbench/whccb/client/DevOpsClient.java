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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.StreamSupport;

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
     * @param url
     * @return
     */
    public String apiOf(String url) {
        final String api = FT("$0$1", host, url);
        return api;
    }

    /**
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
    public List<Map<String, Object>> approvalNodes(final Object flowId) {
        final List<Map<String, Object>> nodes = new ArrayList<Map<String, Object>>();
        final Function<String, IRecord> tonode = s -> {
            return REC("actionName", "同意", "activityName", "项目申请部门-总经理-审批", "createDate",
                    LocalDate.now().toString(), "createUserName", s, "msg", "项目申请部门-总经理-审批-同意");
        };
        final Consumer<Iterable<Object>> handle = itr -> {
            StreamSupport.stream(itr.spliterator(), false).map(e -> {
                if (e instanceof IRecord) {
                    return e;
                } else {
                    return tonode.apply(e + "");
                }
            });
        };

        if (flowId == null) {
            Arrays.stream("".split("[,]+"))
                    .map(tonode)
                    .map(e -> e.toMap2()).forEach(nodes::add);
        } else if (flowId instanceof Integer) { // 真正的流程Id
            // 查询数据库获取 nodes
        } else if (flowId instanceof Iterable) { // 可遍历类型
            final Iterable<Object> itr = (Iterable<Object>) flowId;
            handle.accept(itr);
        } else if (flowId.getClass().isAnnotation()) { // 数组类型
            final Object[] itr = (Object[]) flowId;
            handle.accept(Arrays.asList(itr));
        }

        return nodes;
    }

    private String host = "http://10.24.24.53:8182";
}
