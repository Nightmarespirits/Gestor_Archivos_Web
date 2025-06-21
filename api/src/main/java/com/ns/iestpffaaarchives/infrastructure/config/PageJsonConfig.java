package com.ns.iestpffaaarchives.infrastructure.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;

import java.io.IOException;

/**
 * Configuración para la serialización estable de objetos Page en la API.
 * Esta configuración evita la advertencia de "Serializing PageImpl instances as-is is not supported"
 */
@Configuration
public class PageJsonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer pageCustomizer() {
        return builder -> builder.serializerByType(Page.class, new PageSerializer());
    }

    /**
     * Serializador personalizado para objetos Page que crea una estructura JSON 
     * estable y predecible independiente de cambios futuros en la implementación de Page.
     */
    public static class PageSerializer extends JsonSerializer<Page<?>> {
        @Override
        public void serialize(Page<?> page, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            
            // Datos del contenido de la página
            gen.writeObjectField("content", page.getContent());
            
            // Metadatos de paginación
            gen.writeObjectFieldStart("pagination");
            gen.writeNumberField("page", page.getNumber());
            gen.writeNumberField("size", page.getSize());
            gen.writeNumberField("totalPages", page.getTotalPages());
            gen.writeNumberField("totalElements", page.getTotalElements());
            gen.writeBooleanField("first", page.isFirst());
            gen.writeBooleanField("last", page.isLast());
            gen.writeBooleanField("empty", page.isEmpty());
            gen.writeEndObject();
            
            gen.writeEndObject();
        }
    }
}
