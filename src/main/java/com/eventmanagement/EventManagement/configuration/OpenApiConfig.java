package com.eventmanagement.EventManagement.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(info = @Info(title = "Event APIS",version = "v1",description = "Event Management System API"))
@Configuration
public class OpenApiConfig {
}
