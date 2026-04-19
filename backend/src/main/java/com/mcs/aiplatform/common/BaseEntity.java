package com.mcs.aiplatform.common;

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

    /** Handles epoch-millis (int), epoch-seconds.nanos (float), and ISO-8601 (string). */
    public static final class InstantDeserializer extends StdDeserializer<Instant> {
        public InstantDeserializer() { super(Instant.class); }

        @Override
        public Instant deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            JsonToken token = p.currentToken();
            if (token == JsonToken.VALUE_NUMBER_INT) {
                return Instant.ofEpochMilli(p.getLongValue());
            }
            if (token == JsonToken.VALUE_NUMBER_FLOAT) {
                BigDecimal v = p.getDecimalValue();
                long secs  = v.longValue();
                long nanos = v.subtract(BigDecimal.valueOf(secs)).abs()
                              .movePointRight(9).longValue();
                return Instant.ofEpochSecond(secs, nanos);
            }
            if (token == JsonToken.VALUE_STRING) {
                String text = p.getText();
                if (text == null || text.isBlank()) return null;
                try { return Instant.parse(text); }
                catch (Exception e) {
                    BigDecimal v = new BigDecimal(text);
                    long secs  = v.longValue();
                    long nanos = v.subtract(BigDecimal.valueOf(secs)).abs()
                                  .movePointRight(9).longValue();
                    return Instant.ofEpochSecond(secs, nanos);
                }
            }
            return (Instant) ctx.handleUnexpectedToken(Instant.class, p);
        }
    }
}
