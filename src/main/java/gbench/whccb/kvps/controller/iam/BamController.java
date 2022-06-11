package gbench.whccb.kvps.controller.iam;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gbench.util.lisp.IRecord;

/**
 * 
 * @author xuqinghua
 *
 */
@RequestMapping("/bam-protocol-service")
@RestController
public class BamController {

    /**
     * http://localhost:8089/kvps/bam-protocol-service/hello
     * 
     * @return
     */
    @RequestMapping("/hello")
    public IRecord hello() {
        final IRecord rec = IRecord.REC();
        rec.add("msg", "hello world","time",LocalDateTime.now());
        return rec;
    }

    /**
     * 
     * @param client_id     应用标识 客户端应用注册ID
     * @param client_secret 密钥 客户端应用注册密钥
     * @param code          授权码 调用authorize接口获得的授权码code
     * @param grant_type    认证方式 请求类型，默认authorization_code
     * @param response
     * @throws IOException
     */
    @RequestMapping("oauth2/getToken")
    public Map<String, Object> authenticate(final String client_id, final String client_secret,
            final String redirect_uri, final String code, final HttpServletResponse response) throws IOException {

//        1、验证参数有效性
//        2、验证授权码有效性及范围
//        3、根据以上判断、验证及认证结果返回JSON数据
        final String access_token = UUID.randomUUID().toString();
        final String refresh_token = UUID.randomUUID().toString();
        final String uid = "admin";
        final IRecord ret = IRecord.REC("access_token", access_token, "expires_in", 1500, "refresh_token",
                refresh_token, "uid", uid);

        return ret.toMap2();
    }

    /**
     * 
     * @param access_token
     * @param client_id
     * @return
     */
    @RequestMapping("oauth2/getUserInfo")
    public Map<String, Object> getUserInfo(final String access_token, final String client_id) {
//        1、验证参数有效性
//        2、根据应用配置的属性权限列表，查询用户信息返回
//        3、根据以上判断、验证及认证结果返回JSON数据
        IRecord ret = IRecord.REC();

        return ret.toMap();
    }

}
