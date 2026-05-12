package com.rzodeczko.infrastructure.persistence.config.converter;

import com.rzodeczko.domain.vo.Position;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class StringToPositionConverter implements Converter<String, Position> {

    @Override
    public Position convert(String value) {
        return new Position(value);
    }
}
