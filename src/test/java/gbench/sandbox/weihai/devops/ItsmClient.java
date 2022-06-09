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
import gbench.util.lisp.IRecord;
import gbench.util.lisp.MyRecord;

/**
 * 
 * @author xuqinghua
 *
 */
public class ItsmClient {

    /**
     * url https://whbank.gazellio.com/apiIndex <br>
     * account: sys_admin <br>
     * password: super123456 <br>
     * 
     * @param name     账号
     * @param password 密码
     */
    public String login(String name, String password) {
        final String url = apiOf("/basicsApi/itsmBasicsApi/user/loginCheckUser");
        System.err.println(url);
        JSONObject object = new JSONObject();
        object.put("username", name);
        object.put("password", password);
        String result = ItsmClient.sendPost(url, object, null);
        String res = Base64Util.unescape(result);
        return res;
    }

    /**
     * 获取请求token
     * 
     * @return token
     */
    public String token() {
        return this.token(null, null);
    }

    /**
     * 
     * @param user
     * @param password
     * @return
     */
    public String token(String user, String password) {
        final String _u = Optional.ofNullable(user).orElse(ROOT_USER);
        final String _p = Optional.ofNullable(password).orElse(ROOT_PASSWORD);
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
     * 
     * @param line
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> json2map(String line) {
        final Map<String, Object> json = new LinkedHashMap<String, Object>();
        json.putAll((Map<String, Object>) JSONObject.parse(line));
        return json;
    }

    /**
     * 
     * @param line
     * @return
     */
    public static JSONObject parseJson(String line) {
        return JSONObject.parseObject(line);
    }

    /**
     * 
     * @param obj
     * @return
     */
    public static String toJson(final Object obj) {
        return JSONObject.toJSONString(obj, false);
    }

    /**
     * 格式化
     * 
     * @param obj
     * @return
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
        String result;
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
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * 生成api路径
     * 
     * @param path api 项目路径
     * @return
     */
    public static String apiOf(final String path) {
        final String sep = path.startsWith("/") ? "" : "/";
        return HOST + sep + path;
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
    public static JSONObject JREC(Object... kvs) {
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
    public static Map<String, Object> REC(Object... kvs) {
        final int n = kvs.length;
        final LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>();

        if (n == 1) { // 单一参数情况
            final Object obj = kvs[0];
            if (obj instanceof Map) { // Map情况的数据处理
                ((Map<?, ?>) obj).forEach((k, v) -> { // 键值的处理
                    data.put(k + "", v);
                }); // forEach
            } else if (obj instanceof IRecord) {// IRecord 对象类型 复制对象数据
                data.putAll(((IRecord) obj).toMap());
            } else if (obj instanceof String) { // 字符串类型的单个参数
                final IRecord rec = MyRecord.fromJson(obj.toString()); // 尝试解析json
                if (rec != null) {
                    data.putAll(rec.toMap());
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
     * 发送post请求,带有解析翻译的
     * 
     * @param json
     * @param url
     * @return
     */
    public static String send_post(String url, JSONObject json, String token) {
        final String line = sendPost(url, json, token);
        return Base64Util.unescape(line);
    }

    /**
     * 发送post请求,带有解析翻译的
     * 
     * @param json
     * @param url
     * @return
     */
    public static JSONObject send_post2(String url, JSONObject json, String token) {
        return JSONObject.parseObject(send_post(url, json, token));
    }

    public static String ROOT_USER = "sys_admin";
    public static String ROOT_PASSWORD = "super123456";
    public static String HOST = "https://whbank.gazellio.com";

}
