package org.example.config;

import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.protobuf.services.ProtoReflectionServiceV1;
import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcReflectionConfig {

    /** Публикация схемы сервиса, чтобы можно было отправлять запросы без файла proto. */
    @Bean
    public GrpcServerConfigurer reflectionGrpcServerConfigurer() {
        return serverBuilder -> serverBuilder.addService(ProtoReflectionServiceV1.newInstance());
    }
}
