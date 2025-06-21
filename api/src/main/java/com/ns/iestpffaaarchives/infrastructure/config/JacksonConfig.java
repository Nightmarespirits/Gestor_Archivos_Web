package com.ns.iestpffaaarchives.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Configuración global de Jackson para el manejo adecuado de la serialización JSON
 * en toda la aplicación, especialmente para resolver problemas de referencias circulares.
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.build();
        
        // Deshabilitar la serialización de propiedades nulas
        objectMapper.setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);
        
        // Deshabilitar fallar en propiedades desconocidas
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // Deshabilitar la serialización de referencias vacías
        objectMapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
        
        // Configuración para manejar referencias circulares de manera segura
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        
        return objectMapper;
    }
}
