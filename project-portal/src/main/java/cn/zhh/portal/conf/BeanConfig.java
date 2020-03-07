package cn.zhh.portal.conf;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置类
 *
 * @author Zhou Huanghua
 */
@Configuration
public class BeanConfig {

    @Bean
    public BinaryLogClient binaryLogClient(@Value("${binlog.host}") String host,
                                           @Value("${binlog.port}") Integer port,
                                           @Value("${binlog.schema}") String schema,
                                           @Value("${binlog.username}") String username,
                                           @Value("${binlog.password}") String password) {
        return new BinaryLogClient(host, port, schema, username, password);
    }


}
