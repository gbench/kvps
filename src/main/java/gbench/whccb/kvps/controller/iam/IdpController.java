package gbench.whccb.kvps.controller.iam;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;

import gbench.util.lisp.IRecord;

/**
 * 
 * @author xuqinghua
 *
 */
@RequestMapping("idp")
public class IdpController {

    /**
     * 
     * @param response_type
     * @param state
     * @param redirect_uri
     * @throws IOException
     */
    @RequestMapping("authCenter/authenticate")
    public void authenticate(final String response_type, final String state, final String redirect_uri,
            final String client_id, final HttpServletResponse response) throws IOException {

//        1、判断参数
//        2、验证client_id是否有效
//        3、显示认证授权页面。
//        4、验证身份后页面跳转至redirect_uri并附有参数授权码

        final String seq = redirect_uri.contains("&") ? "&" : "?";
        final String url = IRecord.FT("$0$1$2", redirect_uri, seq, state);

        response.sendRedirect(url);
    }

}
