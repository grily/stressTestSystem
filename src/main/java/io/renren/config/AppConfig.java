package io.renren.config;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.Properties;

@Configuration
public class AppConfig {
    @Bean
    public String getActiveType() {
        //1:加载配置文件
        Resource app = new FileSystemResource("application.yml");
        if (!app.exists()) {
            app = new ClassPathResource("application.yml");
        }
        YamlPropertiesFactoryBean yamlPropertiesFactoryBean = new YamlPropertiesFactoryBean();
        // 2:将加载的配置文件交给 YamlPropertiesFactoryBean
        yamlPropertiesFactoryBean.setResources(app);
        // 3：将yml转换成 key：val
        Properties properties = yamlPropertiesFactoryBean.getObject();
        String active = properties.getProperty("spring.profiles.active");
        return active;
    }
}
