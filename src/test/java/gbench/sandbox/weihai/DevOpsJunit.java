package gbench.sandbox.weihai;

import static gbench.util.lisp.IRecord.REC;
import static gbench.util.io.Output.println;
import static java.util.Arrays.asList;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import gbench.util.lisp.DFrame;
import gbench.util.lisp.IRecord;
import gbench.util.lisp.MyRecord;

/**
 * 
 * @author xuqinghua
 *
 */
public class DevOpsJunit {

    /**
     * 准备Get请求
     * 
     * @param req_params 请求参数
     * @return HttpGet
     */
    public static HttpGet REQ_GET(final IRecord req_params) {
        final String apiurl = req_params.str("$apiurl");
        final List<NameValuePair> nvps = rec2nvps(req_params);// name to values
        final HttpGet get = new HttpGet();
        final Predicate<String> is_blank = line -> line.matches("^\\s*$");
        String params = URLEncodedUtils.format(nvps, "UTF-8").trim();

        String _url = apiurl;
        if (apiurl.contains("?")) { // 参数中含有参数
            final int i = apiurl.indexOf("?"); //
            _url = apiurl.substring(0, i);
            final String params_in_url = apiurl.substring(i + 1).trim(); // url 中的参数
            if (!is_blank.test(params_in_url) && !is_blank.test(params))
                params = IRecord.FT("$0&$1", params_in_url, params); // url 中含有&
            else if (!is_blank.test(params_in_url))
                params = params_in_url;
        }
        if (!is_blank.test(params)) {
            _url = IRecord.FT("$0?$1", _url, params);
        }
        // System.out.println("url:--->"+_url);
        get.setURI(URI.create(_url));

        return get;
    }

    /**
     * 准备Get请求
     * 
     * @param req_params 请求参数
     * @return HttpGet
     */
    public static HttpPost REQ_POST(final IRecord req_params) {
        final String apiurl = req_params.str("$apiurl");
        final HttpPost post = new HttpPost(apiurl);
        final String entity_type = req_params.strOpt("$entity_type").orElse("url_encoded_entity");
        if (entity_type.equals("inputstream_entity") || "json".equals(entity_type)) {
            post.setEntity(rec2inputstream_entity(req_params));
        } else if (entity_type.equals("multipart_entity")) {
            post.setEntity(rec2multipart_entity(req_params));
        } else {
            post.setEntity(rec2url_encoded_form_entity(req_params));
        }

        return post;
    }

    /**
     * 准备HTTP请求：默认是Post请求 <br>
     * $connect_timeout 连接过期时间 默认为5000 <br>
     * $connect_request_timeout 连接请求过期时间 默认为2000 <br>
     * $socket_timeout 套接字过期时间 默认为5000 <br>
     * 
     * @param req_params 请求参数包括 客户端命令参数$xxx <br>
     * @return HttpUriRequest
     */
    public static HttpUriRequest REQUEST(final IRecord req_params) {

        final RequestConfig reqconf = RequestConfig.custom()
                .setConnectTimeout(req_params.i4Opt("$connect_timeout").orElse(5000))
                .setConnectionRequestTimeout(req_params.i4Opt("$connect_request_timeout").orElse(2000))
                .setSocketTimeout(req_params.i4Opt("$socket_timeout").orElse(5000)).build();

        final HttpRequestBase req = Optional.ofNullable(req_params.str("$method")).map(method -> {
            switch (method) {
            case "get":
                return REQ_GET(req_params);
            default:
                return REQ_POST(req_params);
            }
        }).orElse(REQ_POST(req_params)); // 默认为POST请求

        req.setConfig(reqconf); // 设置请求
        req_params.opt("$header").map(IRecord::REC).orElse(REC()).forEach((key, value) -> {
            req.setHeader(key, value + "");
        });// 设置请求头

        return req;
    }

    /**
     * 设置请求参数
     * 
     * @param rec 请求参数
     * @return 键值对序列
     */
    public static List<NameValuePair> rec2nvps(final IRecord rec) {
        final Map<String, Object> paramMap = rec.filter(kv -> !kv._1().startsWith("$")).toMap();
        final List<NameValuePair> params = new ArrayList<>();
        final Set<Map.Entry<String, Object>> set = paramMap.entrySet();
        for (final Map.Entry<String, Object> entry : set) {
            params.add(new BasicNameValuePair(entry.getKey(), entry.getValue() + ""));
        }
        return params;
    }

    /**
     * 转换成 json 格式
     * 
     * @param obj
     * @return
     */
    public static IRecord json2rec(final String json) {
        final IRecord rec = IRecord.REC(json);
        return rec;
    }

    /**
     * json 格式化
     * 
     * @param obj json 数据对象 IRecord,Map 类型结构
     * @return
     * @throws JsonProcessingException
     */
    public String fmtjson(final Object obj) throws JsonProcessingException {
        final ObjectMapper objM = new ObjectMapper();
        final ObjectWriter objW = objM.writerWithDefaultPrettyPrinter();
        final Object o = obj instanceof IRecord ? ((IRecord) obj).toMap2() : obj;
        return objW.writeValueAsString(o);
    }

    /**
     * 记录转实体:注意这里把rec中的所有参数转换成HTTP请求参数．除掉了客户端自定义字段$开头字段名．
     * 
     * @param rec 过滤掉所有以$开头的字段信息，他们是客户端的的自定义关键字
     * @return HttpEntity
     */
    public static HttpEntity rec2inputstream_entity(final IRecord rec) {
        final Map<String, Object> map = rec.filter(e -> !e._1().startsWith("$")).toMap2();// 去除调$属性
        return json2inputstream_entity(MyRecord.toJson(map));
    }

    /**
     * 把json对象转换成请求实体。
     * 
     * @param json json 字符串
     * @return HttpEntity
     */
    public static HttpEntity json2inputstream_entity(final String json) {
        final byte bb[] = json.getBytes();
        final InputStream inputStream = new ByteArrayInputStream(bb, 0, bb.length);
        final HttpEntity entity = new InputStreamEntity(inputStream, bb.length); // 请求体
        return entity;
    }

    /**
     * 把json对象转换成请求实体。
     * 
     * @param json json 字符串
     * @return HttpEntity
     */
    public static HttpEntity rec2url_encoded_form_entity(final IRecord params) {

        UrlEncodedFormEntity entity = null;
        try {
            entity = new UrlEncodedFormEntity(rec2nvps(params), "utf8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return entity;
    }

    /**
     * 把json对象转换成请求实体。
     * 
     * @param json json 字符串
     * @return HttpEntity
     */
    public static HttpEntity rec2multipart_entity(final IRecord params) {

        HttpEntity entity = null;
        final ContentType contentType = params.strOpt("$content_type") //
                .map(ContentType::create) //
                .orElse(ContentType.create("text/plain", "utf8"));
        final MultipartEntityBuilder builder = MultipartEntityBuilder.create().setContentType(contentType);
        params.filter(e -> !e._1.startsWith("$")).forEach((key, val) -> {
            if (val instanceof byte[]) {
                builder.addBinaryBody(key, (byte[]) val);
            } else if (val instanceof File) {
                builder.addBinaryBody(key, (File) val);
            } else if (val instanceof File) {
                builder.addBinaryBody(null, (InputStream) val);
            } else {
                builder.addTextBody(key, val + "", ContentType.create("text/plain", "utf8"));
            }
        }); // forEach

        entity = builder.build();

        return entity;
    }

    /**
     * 
     * 文件接口发送
     * 
     * @param url
     * @param params
     * @param encoding
     * @return
     */
    public static IRecord send2(final String url, final IRecord params) {
        return IRecord.REC(send(url, params, "utf8"));
    }

    /**
     * 
     * 文件接口发送
     * 
     * @param url
     * @param params
     * @param encoding
     * @return
     */
    public static String send(final String url, final IRecord params) {
        return send(url, params, "utf8");
    }

    /**
     * 
     * 文件接口发送
     * 
     * @param url
     * @param params
     * @param encoding
     * @return
     */
    public static String send(final String url, final IRecord params, final String encoding) {
        String body = "";
        // 1、创建HttpClient对象
        final CloseableHttpClient httpClient = HttpClients.createDefault();
        params.set("$apiurl", url);
        // 2、创建请求方法对象
        final HttpUriRequest req = REQUEST(params);

        try {
            // 4、执行请求
            final CloseableHttpResponse response = httpClient.execute(req);
            // 5、获取结果实体
            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                body = EntityUtils.toString(entity, encoding);
            }
            EntityUtils.consume(entity);
            // 7、释放连接
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return body;
    }

    @Test
    public void foo() {
        final String url = "http://localhost:8089/snowwhite/media/file/list?_=1";
        final String body = send(url, REC("$method", "post", "key", "E:/slicee/temp/snowwhite/ufms"));
        println(body);
        println(json2rec(body));
    }

    /**
     * token
     */
    @Test
    public void auth_oauth_token() {
        String host = "http://localhost:8089/snowwhite";
        host = "http://10.24.24.53:8182";
        final String api = IRecord.FT("$0$1", host, "/auth/oauth/token");
        println(api);
        final String data = send(api, REC( //
                "$entity_type", "url_encoded_form_entity", "$header",
                REC("Authorization", "Basic Y2xpZW50OnNlY3JldA=="), //
                "username", "TOKEN_API", //
                "password", "U2FsdGVkX18qz8S9JC", //
                "scope", "all", //
                "grant_type", "password"));
        println(data);
    }

    /**
     * 
     * @author xuqinghua
     *
     */
    public static class KVPS {
        final String host = "http://localhost:8089";
        final String bz_registry_key = "E:/slicee/temp/snowwhite/kvps/registry.json";

        /**
         * 写入
         * 
         * @param key  键名
         * @param data 键数据
         * @return IRecord
         */
        public IRecord list(final String key) {
            final String api = IRecord.FT("$0$1", host, "/snowwhite/media/file/list");
            final IRecord response = send2(api, REC("key", key));
            return response;
        }

        /**
         * 写入
         * 
         * @param key  键名
         * @param data 键数据
         * @return IRecord
         */
        public IRecord put(final String key, final IRecord data) {
            final String api = IRecord.FT("$0$1", host, "/snowwhite/media/file/write");
            final IRecord response = send2(api, REC("key", key, "lines", data.json()));
            return response;
        }

        /**
         * 读取
         * 
         * @param key 键名
         * @return 键数据
         */
        public IRecord get(final String key) {
            final String url2 = IRecord.FT("$0$1$2", host, "/snowwhite/media/file/download?key=", key);
            final IRecord response = send2(url2, REC());
            return response;
        }

        /**
         * biz key 的注册表
         * 
         * @return
         */
        public IRecord bz_registry() {
            return this.get(bz_registry_key);
        }

        /**
         * 业务key 的计算 <br>
         * 不存在则创建，存在则读取，乐观锁模式，也就是 假设数据是存在的，不存在则写入 <br>
         * 
         * @param bz_key 业务key
         * @param lines  业务值
         * @return
         */
        public IRecord computeIfAbsent(final String bz_key, final IRecord lines) {
            final IRecord bzreg = this.bz_registry();
            if (bzreg.size() < 0) {
                this.put(this.bz_registry_key, REC());
            }

            final IRecord gen_url = this.generate_url(bz_key); // 生成URL
            final String gen_put_url = gen_url.str("gen_put_url");
            final String gen_get_url = gen_url.str("gen_get_url");
            final String gen_key = gen_url.str("gen_key");
            final String reg_get_url = bzreg.str(bz_key);
            if (reg_get_url == null) {
                this.put(bz_registry_key, bzreg.add(bz_key, gen_get_url)); // 登记 key
                // this.put(gen_key, lines);
                send2(gen_put_url, REC("key", gen_key, "lines", lines.json())); // 写入 与 this.put(gen_key, lines) 等价
                return this.get(gen_key);
            } else { // 注册表的读取url
                return send2(reg_get_url, REC()); // 直接读
            }
        }

        /**
         * 业务key 的计算
         * 
         * @param bz_key 业务key
         * @param lines  业务值
         * @return
         */
        public IRecord computeIfPresent(final String bz_key, final IRecord lines) {
            final IRecord reg = this.bz_registry();
            if (reg.size() < 0) {
                this.put(this.bz_registry_key, REC());
            }

            final IRecord gen_url = this.generate_url(bz_key); // 生成URL
            final String gen_put_url = gen_url.str("gen_put_url");
            final String gen_get_url = gen_url.str("gen_get_url");
            final String gen_key = gen_url.str("gen_key");
            final String reg_get_url = reg.str(bz_key);
            if (reg_get_url == null) { // 值不存在
                return REC(); // do nothing
            } else { // 注册表的读取url
                this.put(bz_registry_key, reg.add(bz_key, gen_get_url)); // 更新注册key
                // this.put(gen_key, lines);
                send2(gen_put_url, REC("key", gen_key, "lines", lines.json())); // 写入 与 this.put(gen_key, lines) 等价
                return send2(gen_get_url, lines); // 直接读
            }
        }

        /**
         * 生成 URL
         * 
         * @param bz_key 业务key
         * @return {bz_key,gen_key,gen_put_url,gen_get_key}
         */
        public IRecord generate_url(final String bz_key) {
            final String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddhhmmssSSSS"));
            final String gen_key = IRecord.FT("$0$1$2.json", "E:/slicee/temp/snowwhite/kvps/devops/proj/", bz_key,
                    timestamp);
            final String gen_put_url = IRecord.FT("$0$1", host, "/snowwhite/media/file/write");
            final String gen_get_url = IRecord.FT("$0$1$2", host, "/snowwhite/media/file/download?key=", gen_key);

            return IRecord.REC("bz_key", bz_key, "gen_key", gen_key, "gen_put_url", gen_put_url, "gen_get_url",
                    gen_get_url);
        }
    }

    /**
     * KVPS 的演示
     */
    @Test
    public void qux() {
        String host = "http://localhost:8089";
        final String api = IRecord.FT("$0$1", host, "/snowwhite/media/file/write");
        final String kvps_home = "E:/slicee/temp/snowwhite/kvps/devops/";
        final String key = IRecord.FT("$0$1", kvps_home, "project.json");
        final IRecord data = send2(api, REC( //
                "key", key, // 文件key
                "lines", REC("name", "第一个项目").json()));
        println(data);
        final String url2 = IRecord.FT("$0$1", host, data.str("downloadUrl"));
        final IRecord data2 = send2(url2, REC());
        println(data2);
    }

    /**
     * KVPS 的演示
     * 
     * @throws Exception
     */
    @Test
    public void qux2() throws Exception {
        final KVPS kvps = new KVPS();
        final String kvp_home = "E:/slicee/temp/snowwhite/kvps/devops/proj/";
        final String key = IRecord.FT("$0$1", kvp_home, "proj002.json");
        final IRecord proj_inst = REC("name", "proj001", "teamgroup", asList( //
                REC("name", "张三", "role", "项目经理", "mobile", "123") //
                , REC("name", "李四", "role", "技术组长", "mobile", "123") //
                , REC("name", "王五", "role", "测试组长", "mobile", "123") //
        ), "create_time", LocalDate.now(), //
                "budget", REC("hardware", asList( //
                        REC("name", "服务器", "amount", 10) //
                        , REC("name", "交换机", "amount", 20)), //
                        "software", asList( //
                                REC("name", "操作系统", "amount", 30) //
                                , REC("name", "数据库", "amount", 40)) //
                ), // budget
                "reqs",
                asList(REC("key", "req001", "file-key", "E:/slicee/temp/snowwhite/reqs/req001.docx", "version", "1.0"), //
                        REC("key", "req002", "file-key", "E:/slicee/temp/snowwhite/reqs/req002.docx", "version", "1.1") //
                ), //
                "reqdecs",
                asList(REC("req-key", "req001", "reqents", asList(REC("reqent-key", "reqent001", "name", "项目地图"),
                        REC("reqent-key", "reqent001", "name", "里程碑计划"))) //
                        , REC("req-key", "req002", "reqents", asList(REC("reqent-key", "reqent001", "name", "文档管理"),
                                REC("reqent-key", "reqent001", "name", "会议管理"))) //

                ) //
        ); // REC

        /////////////////////////////////////////
        // KVPS 的使用演示
        /////////////////////////////////////////
        final DFrame files = kvps.list(kvp_home).llS("files").map(IRecord::REC).collect(DFrame.dfmclc);
        println("\nKVPS dumps");
        println(files);
        kvps.put(key, proj_inst); // 数据实例的写入
        final IRecord proj001 = kvps.get(key); // 数据键值的读取

        // 读取的数据对象
        println(fmtjson(proj001.toMap2()));

        println("\n项目团队");
        final DFrame teamgroup = proj001.llS("teamgroup").map(IRecord::REC).collect(DFrame.dfmclc);
        println(teamgroup);
        println("\n硬件预算");
        final DFrame hardware = proj001.pathllS("budget/hardware").map(IRecord::REC).collect(DFrame.dfmclc);
        println(hardware);
        println("\n需求文档");
        final DFrame reqdecs = proj001.pathllS("reqdecs").map(IRecord::REC).collect(DFrame.dfmclc);
        println(reqdecs);
        println("\n需求分解条目");
        reqdecs.rowS().forEach(e -> {
            final DFrame reqents_line = e.llS("reqents").map(IRecord::REC).collect(DFrame.dfmclc);
            println("--");
            println("req-key", e.str("req-key"));
            println(reqents_line);
        });

        // 软件预算
        final DoubleSummaryStatistics stats_software = proj001.pathllS("budget/software").map(IRecord::REC)
                .map(e -> e.dbl("amount")).collect(Collectors.summarizingDouble(e -> e));
        println(stats_software);
    }

    /**
     * 业务场景的模拟
     */
    @Test
    public void qux3() {
        final KVPS kvps = new KVPS();
        // 查看业务键结构
        println(kvps.bz_registry());
        final String bz_key = "proj001";
        println(kvps.generate_url(bz_key));
        // 读取&初始写：不存在则创建，存在则读取，乐观锁模式，也就是 假设数据是存在的，不存在则写入
        println(kvps.computeIfAbsent(bz_key, REC("name", bz_key + "_init", "create_time", LocalDateTime.now())));
        // 更新：不存在,do nothing,存在则 更新。
        println(kvps.computeIfPresent(bz_key,
                REC("name", bz_key + "_" + LocalDateTime.now(), "create_time", LocalDateTime.now())));
    }

    static {

    }

}
