package gbench.whccb.kvps.config;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import gbench.util.json.jackson.IRecordModule;

/**
 * 自定义jackson配置 <br>
 * 1.解决long类型转成json时JS丢失精度问题 <br>
 * 2.统一设置常见日期类型序列化格式 <br>
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
//        // 序列换成json时,将所有的long变成string
//        final SimpleModule lng2strModule = new SimpleModule();
//        lng2strModule.addSerializer(Long.class, ToStringSerializer.instance);
//        lng2strModule.addSerializer(Long.TYPE, ToStringSerializer.instance);

        // 日期序列化设置
        final JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(Date.class,
                new DateSerializer(false, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        final IRecordModule recordModule = new IRecordModule();
        final List<com.fasterxml.jackson.databind.Module> modules = Arrays.asList(javaTimeModule, recordModule);
        final ObjectMapper objM = new ObjectMapper();

        modules.forEach(objM::registerModule); // 注册模块

        return objM;
    }
}