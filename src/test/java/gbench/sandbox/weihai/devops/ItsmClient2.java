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
 * 
 * @author xuqinghua
 *
 */
public class ItsmClient2 {

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
        String result = ItsmClient2.sendPost(url, object, null);
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
     * @param URL
     * @return
     */
    public static String sendPost(String URL, JSONObject json, String token) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(URL);
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

    public static String ROOT_USER = "sys_admin";
    public static String ROOT_PASSWORD = "super123456";
    public static String HOST = "https://whbank.gazellio.com";

}
