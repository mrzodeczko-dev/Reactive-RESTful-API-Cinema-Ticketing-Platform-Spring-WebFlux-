package com.rzodeczko.infrastructure.persistence.config.converter;

import com.rzodeczko.domain.vo.Position;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class PositionToStringConverter implements Converter<Position, String> {

    @Override
    public String convert(Position position) {
        return position.toString();
    }
}
