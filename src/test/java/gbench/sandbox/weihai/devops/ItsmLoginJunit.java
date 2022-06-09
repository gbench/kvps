package gbench.sandbox.weihai.devops;

import org.junit.jupiter.api.Test;

/**
 * 
 * @author xuqinghua
 *
 */
public class ItsmLoginJunit extends ItsmClient2 {


    @Test
    public void foo_login() {
        final String json = this.login("sys_admin", "super123456");
        System.out.println(pretty(json));
    }

    @Test
    public void foo_token() {
        System.out.println(this.token());
    }

}
