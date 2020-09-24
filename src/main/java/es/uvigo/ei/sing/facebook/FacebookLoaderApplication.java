package es.uvigo.ei.sing.facebook;

import es.uvigo.ei.sing.facebook.utils.FacebookConfiguration;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@Log4j2
@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableConfigurationProperties(FacebookConfiguration.class)
@EnableRetry
public class FacebookLoaderApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(FacebookLoaderApplication.class).web(WebApplicationType.NONE).headless(false).run(args);
    }
}
