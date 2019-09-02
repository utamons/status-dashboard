package com.corn.data;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@SuppressWarnings("unused")
public class InstantDeserializer extends StdDeserializer<Instant> {

   static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    public InstantDeserializer() {
        super(Instant.class);
    }

    protected InstantDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Instant deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String date = jsonParser.getText();

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(PATTERN)
                        .withZone(ZoneId.systemDefault());

        return Instant.from(formatter.parse(date));
    }
}
