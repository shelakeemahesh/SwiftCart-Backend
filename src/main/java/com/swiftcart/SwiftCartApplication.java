package com.swiftcart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = {
    org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchClientAutoConfiguration.class,
    org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration.class,
    org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration.class,
    org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration.class,
    org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration.class
})
@EnableCaching
@EnableAsync
public class SwiftCartApplication {
    public static void main(String[] args) {
        SpringApplication.run(SwiftCartApplication.class, args);
    }
}
