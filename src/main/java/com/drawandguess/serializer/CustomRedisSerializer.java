package com.drawandguess.serializer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.lang.Nullable;

public class CustomRedisSerializer implements RedisSerializer<Object> {

    private final Charset charset;

    public CustomRedisSerializer() {
        this.charset = StandardCharsets.UTF_8;
    }

    public CustomRedisSerializer(Charset charset) {
        this.charset = charset;
    }

    @Override
    @Nullable
    public byte[] serialize(@Nullable Object value) {
        if (value == null)
            return null;

        String target = value.toString();
        return (target == null ? null : target.getBytes(charset));
    }

    @Override
    @Nullable
    public Object deserialize(@Nullable byte[] bytes) {
        if (bytes == null)
            return null;

        String result = (bytes == null ? null : new String(bytes, charset));
        return result;
    }

}
