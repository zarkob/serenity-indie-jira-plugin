package com.serenityindie.jiraplugin.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class OffsetDateTimeAdapter extends TypeAdapter<OffsetDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME;

    @Override
    public void write(JsonWriter out, OffsetDateTime value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(FORMATTER.format(value));
        }
    }

    @Override
    public OffsetDateTime read(JsonReader in) throws IOException {
        return OffsetDateTime.parse(in.nextString(), FORMATTER);
    }
}
