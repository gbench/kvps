package gbench.sandbox.weihai.devops;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import gbench.sandbox.weihai.devops.util.Base64Util;

/**
 * ItsmClient 客户端 示例 <br>
 * 
 * url: https://whbank.gazellio.com/apiIndex <br>
 * 快速查询: 1.45.56 用户登录 username: sys_admin <br>
 * password: super123456 <br>
 * 
 * @author gbench
 *
 */
public class ItsmClient {

    /**
     * 用户登录
     * 
     * @param name     账号
     * @param password 密码
     */
    public String login(final String name, final String password) {
        final String url = apiOf("/user/loginCheckUser");

        // System.err.println(url);

        final JSONObject object = new JSONObject();
        object.put("username", name);
        object.put("password", password);
        final String result = httpPost(url, object, null);

        return result;
    }

    /**
     * 获取访问token
     * 
     * @return token
     */
    public String token() {
        return this.token(null, null);
    }

    /**
     * 提取范文token
     * 
     * @param user     用户名
     * @param password 密码
     * @return 访问token
     */
    public String token(final String user, final String password) {
        final String _u = Optional.ofNullable(user).orElse(DEFAULT_USERNAME);
        final String _p = Optional.ofNullable(password).orElse(DEFAULT_PASSWORD);
        final String line = this.login(_u, _p);

        String token = null;
        try {
            final JSONObject jsn = parseJson(line);
            token = jsn.getJSONObject("rows").getString("token");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return token;
    }

    /**
     * MAP 键值对儿生成器
     * 
     * @param kvs 键,值序列:key0,value0,key1,value1,.... <br>
     *            Map结构（IRecord也是Map结构） 或是 键名,键值 序列。即 build(map) 或是
     *            build(key0,value0,key1,vlaue1,...) 的 形式， 特别注意 build(map) 时候，当且仅当
     *            kvs 的只有一个元素，即 build(map0,map1) 会被视为 键值序列
     * @return
     */
    public static JSONObject JREC(final Object... kvs) {
        return JSONObject.parseObject(toJson(REC(kvs)));
    }

    /**
     * MAP 键值对儿生成器
     * 
     * @param kvs 键,值序列:key0,value0,key1,value1,.... <br>
     *            Map结构（IRecord也是Map结构） 或是 键名,键值 序列。即 build(map) 或是
     *            build(key0,value0,key1,vlaue1,...) 的 形式， 特别注意 build(map) 时候，当且仅当
     *            kvs 的只有一个元素，即 build(map0,map1) 会被视为 键值序列
     * @return
     */
    public static Map<String, Object> REC(final Object... kvs) {
        final int n = kvs.length;
        final LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>();

        if (n == 1) { // 单一参数情况
            final Object obj = kvs[0];
            if (obj instanceof Map) { // Map情况的数据处理
                ((Map<?, ?>) obj).forEach((k, v) -> { // 键值的处理
                    data.put(k + "", v);
                }); // forEach
            } else if (obj instanceof String) { // 字符串类型的单个参数
                final Map<String, Object> jsonMap = parseJson(obj.toString()); // 尝试解析json
                if (jsonMap != null) {
                    data.putAll(jsonMap);
                }
            } // if
        } else { // 键名减值序列
            for (int i = 0; i < n - 1; i += 2) {
                data.put(kvs[i].toString(), kvs[i + 1]);
            }
        } // if
        return data;
    }

    /**
     * 解析JSON
     * 
     * @param jsonString json 字符串
     * @return Map 对象
     */
    public static Map<String, Object> fromJson(final String jsonString) {
        return parseJson(jsonString);
    }

    /**
     * 
     * @param obj jsonObject 对象
     * @return json 字符串
     */
    public static String toJson(final Object obj) {
        return JSONObject.toJSONString(obj, false);
    }

    /**
     * 解析JSON
     * 
     * @param jsonString json 字符串
     * @return JSONObject
     */
    public static JSONObject parseJson(final String jsonString) {
        return JSONObject.parseObject(jsonString);
    }

    /**
     * 格式化
     * 
     * @param jsonString
     * @return json 格式化字符串
     */
    public static String pretty(final String jsonString) {
        JSONObject object = JSONObject.parseObject(jsonString);
        String json = JSONObject.toJSONString(object, SerializerFeature.PrettyFormat,
                SerializerFeature.WriteDateUseDateFormat, SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteNullListAsEmpty);
        return json;
    }

    /**
     * 发送post请求
     * 
     * @param json
     * @param url
     * @return
     */
    public static String sendPost(String url, JSONObject json, String token) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(url);
        post.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        post.addHeader("token", token);
        String result = null;

        try {
            UrlEncodedFormEntity s = new UrlEncodedFormEntity(
                    Arrays.asList(new BasicNameValuePair("info", Base64Util.escape(json.toJSONString()))));
            post.setEntity(s);
            // 发送请求
            HttpResponse httpResponse = client.execute(post);
            // 获取响应输入流
            InputStream inStream = httpResponse.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "utf-8"));
            StringBuilder strber = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                strber.append(line);
            inStream.close();
            result = strber.toString();
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                // System.out.println("请求服务器成功，做相应处理");
            } else {
                System.out.println("请求服务端失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 生成api路径
     * 
     * @param path api 项目路径
     * @return api路径
     */
    public static String apiOf(final String path) {
        final String sep = path.startsWith("/") ? "" : "/";
        return API_SERVER_URL_PREFIX + sep + path;
    }

    /**
     * 发送post请求,带有解析翻译的
     * 
     * @param json 请求参数
     * @param url  请求地址
     * @return json 字符串
     */
    public static String httpPost(String url, JSONObject json, String token) {
        final String line = sendPost(url, json, token);
        return Base64Util.unescape(line);
    }

    /**
     * 发送post请求,带有解析翻译的
     * 
     * @param json 请求参数
     * @param url  请求地址
     * @return json 字符串
     */
    public static JSONObject httpPost2(String url, JSONObject json, String token) {
        return JSONObject.parseObject(httpPost(url, json, token));
    }

    /**
     * 默认用户名
     */
    public static String DEFAULT_USERNAME = "sys_admin";
    /**
     * 默认密码
     */
    public static String DEFAULT_PASSWORD = "super123456";

    /**
     * API 服务器接口地址前缀
     */
    public static String API_SERVER_URL_PREFIX = "https://whbank.gazellio.com/basicsApi/itsmBasicsApi";

}
