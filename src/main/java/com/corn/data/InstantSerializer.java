package com.corn.data;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static com.corn.data.InstantDeserializer.PATTERN;

@SuppressWarnings("unused")
public class InstantSerializer  extends StdSerializer<Instant> {

    public InstantSerializer () {
        super(Instant.class);
    }

    public InstantSerializer(StdSerializer<?> src) {
        super(src);
    }

    @Override
    public void serialize(Instant instant, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(PATTERN)
                        .withLocale( Locale.UK )
                        .withZone( ZoneId.systemDefault() );

        jsonGenerator.writeString(formatter.format(instant));
    }
}
