package com.ecommerce.mvp.common.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.TimeZone

@Configuration
class JacksonConfig {

    companion object {
        /**
         * The single standard datetime format used everywhere in API responses.
         * Example output: "2026-04-10T11:23:45.123Z"
         *  - ISO-8601 compliant
         *  - Always UTC (the trailing "Z" explicitly means UTC / offset +00:00)
         *  - Millisecond precision (3 decimal places) — consistent, no trailing zeros
         */
        val UTC_FORMATTER: DateTimeFormatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .withZone(ZoneOffset.UTC)
    }

    @Bean
    fun jacksonCustomizer(): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer { builder ->

            // Custom serializer: Instant → "2026-04-10T11:23:45.123Z"
            val javaTimeModule = JavaTimeModule()
            javaTimeModule.addSerializer(Instant::class.java, InstantUtcSerializer())

            builder.modules(javaTimeModule)
            // Do not write dates as numeric timestamps
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            // Jackson's own timezone set to UTC (covers java.util.Date if any slips through)
            builder.timeZone(TimeZone.getTimeZone("UTC"))
        }
    }

    /**
     * Serializes any [Instant] field to the project-wide UTC format:
     * "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
     */
    private class InstantUtcSerializer : StdSerializer<Instant>(Instant::class.java) {
        override fun serialize(value: Instant, gen: JsonGenerator, provider: SerializerProvider) {
            gen.writeString(UTC_FORMATTER.format(value))
        }
    }
}

