package gbench.sandbox.weihai.devops;

import org.junit.jupiter.api.Test;

import com.alibaba.fastjson.JSONObject;

/**
 * 
 * @author xuqinghua
 *
 */
public class ItsmApiJunit extends ItsmClient {

    /**
     * 登录演示
     */
    @Test
    public void foo_login() {
        final String json = this.login("sys_admin", "super123456");
        System.out.println(pretty(json));
    }

    /**
     * token 演示
     */
    @Test
    public void foo_token() {
        System.out.println(this.token());
    }

    /**
     * token api 调用演练
     */
    @Test
    public void foo_api() {
        final String api = apiOf("basicsApi/itsmBasicsApi/SysConfig/getIndexConfigMessage");
        final JSONObject json = send_post2(api, JREC(), this.token());
        System.out.println(json);
    }

}
