package gbench.sandbox.weihai.devops;

import static gbench.util.lisp.IRecord.REC;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import gbench.util.lisp.IRecord;
import gbench.util.lisp.MyRecord;

public class MyHttpClient {

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
    public static String fmtjson(final Object obj) throws JsonProcessingException {
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
        return json2inputstream_entity(MyRecord.toJson(map),
                rec.strOpt("$content_type").orElse("application/json;charset=UTF-8"));
    }

    /**
     * 把json对象转换成请求实体。
     * 
     * @param json json 字符串
     * @return HttpEntity
     */
    public static HttpEntity json2inputstream_entity(final String json, String contentType) {
        final byte bb[] = json.getBytes();
        final InputStream inputStream = new ByteArrayInputStream(bb, 0, bb.length);
        final InputStreamEntity entity = new InputStreamEntity(inputStream, bb.length); // 请求体
        entity.setContentType(contentType);
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
     * 指定url的请求发送
     * 
     * @param url      接口地址
     * @param params   接口与参数
     * @param encoding 接口编码
     * @return 接口返回
     */
    public static IRecord send2(final String url, final IRecord params) {
        return IRecord.REC(send(url, params, "utf8"));
    }

    /**
     * 
     * 文件接口发送
     * 
     * @param url      接口地址
     * @param params   接口与参数
     * @param encoding 接口编码
     * @return 接口返回
     */
    public static String send(final String url, final IRecord params) {
        return send(url, params, "utf8");
    }

    /**
     * 
     * 文件接口发送
     * 
     * @param url      接口地址
     * @param params   接口参数
     * @param encoding 接口编码
     * @return 接口返回
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
            final IRecord error = REC("error", 1, "msg", e.getMessage(), "stackTrace",
                    Arrays.stream(e.getStackTrace()).map(Object::toString).toArray());
            body = error.json();
        }

        return body;
    }

}
