package gbench.sandbox.weihai.devops;

import org.junit.jupiter.api.Test;

import com.alibaba.fastjson.JSONObject;

import gbench.whccb.client.ItsmClient;

/**
 * 
 * @author xuqinghua
 *
 */
public class ItsmApiJunit extends ItsmClient {

    /**
     * 获取首页配置信息
     * 
     * @return {loginLogo: 登陆页面Logo地址,indexLogo: 首页面Logo地址,loginBGImage:
     *         登陆页面背景图片地址,emailAddress: 快速提交邮箱地址}
     */
    public JSONObject getIndexConfigMessage() {
        final String api = apiOf("/SysConfig/getIndexConfigMessage");
        final JSONObject json = httpPost2(api, JREC(), this.token());
        return json;
    }

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
    public void foo_api_demo() {
        System.out.println(this.getIndexConfigMessage());
    }

}
