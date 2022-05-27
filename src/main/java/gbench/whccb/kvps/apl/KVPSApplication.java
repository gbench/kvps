package gbench.whccb.kvps.apl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 向 注册中心 进行注册自己
 * 
 * eureka.client.fetch-registry = true
 * eureka.client.registry-fetch-interval-seconds = 30
 * 
 * @author gbench
 *
 */
@ComponentScan(basePackages = { "gbench.whccb.kvps.controller", "gbench.whccb.kvps.model" })
@SpringBootApplication
public class KVPSApplication {

    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(KVPSApplication.class, args);
    }
}
