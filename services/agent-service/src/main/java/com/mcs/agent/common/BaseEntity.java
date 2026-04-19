package com.mcs.agent.common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public abstract class BaseEntity {
    @Id
    private String id;

    @CreatedDate
    @JsonSerialize(using = InstantSerializer.class)
    @JsonDeserialize(using = BaseEntity.InstantDeserializer.class)
    private Instant createdAt;

    @LastModifiedDate
    @JsonSerialize(using = InstantSerializer.class)
    @JsonDeserialize(using = BaseEntity.InstantDeserializer.class)
    private Instant updatedAt;

    /**
     * Handles all three formats Jackson may produce for Instant:
     *   - INTEGER : epoch-milliseconds  (e.g. 1776465159006)
     *   - FLOAT   : epoch-seconds.nanos (e.g. 1776465159.006000000)
     *   - STRING  : ISO-8601            (e.g. "2026-04-19T08:00:00Z")
     */
    public static final class InstantDeserializer extends StdDeserializer<Instant> {
        public InstantDeserializer() {
            super(Instant.class);
        }

        @Override
        public Instant deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            JsonToken token = p.currentToken();

            if (token == JsonToken.VALUE_NUMBER_INT) {
                // epoch-milliseconds
                return Instant.ofEpochMilli(p.getLongValue());
            }

            if (token == JsonToken.VALUE_NUMBER_FLOAT) {
                // epoch-seconds with fractional nanoseconds (e.g. 1776465159.006000000)
                BigDecimal v = p.getDecimalValue();
                long seconds = v.longValue();
                long nanos = v.subtract(BigDecimal.valueOf(seconds))
                              .abs()
                              .movePointRight(9)
                              .longValue();
                return Instant.ofEpochSecond(seconds, nanos);
            }

            if (token == JsonToken.VALUE_STRING) {
                String text = p.getText();
                if (text == null || text.isBlank()) return null;
                // Try ISO-8601 first, fall back to numeric string
                try {
                    return Instant.parse(text);
                } catch (Exception e) {
                    BigDecimal v = new BigDecimal(text);
                    long seconds = v.longValue();
                    long nanos = v.subtract(BigDecimal.valueOf(seconds))
                                  .abs()
                                  .movePointRight(9)
                                  .longValue();
                    return Instant.ofEpochSecond(seconds, nanos);
                }
            }

            return (Instant) ctx.handleUnexpectedToken(Instant.class, p);
        }
    }
}
