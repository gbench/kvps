package gbench.whccb.kvps.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import gbench.util.json.MyJson;

/**
 * 自定义jackson配置 <br>
 * 1.解决long类型转成json时JS丢失精度问题 <br>
 * 2.统一设置常见日期类型序列化格式 <br>
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return MyJson.recM(); // 默认的objectMapper，具有 时间 与 IRecord 解析能力
    }
}