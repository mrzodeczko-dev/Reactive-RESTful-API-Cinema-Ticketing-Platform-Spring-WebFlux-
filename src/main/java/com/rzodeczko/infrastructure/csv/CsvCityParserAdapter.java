package com.rzodeczko.infrastructure.csv;

import com.opencsv.bean.CsvToBeanBuilder;
import com.rzodeczko.application.dto.CreateCityDto;
import com.rzodeczko.application.exception.CityServiceException;
import com.rzodeczko.application.port.out.CityCsvParserPort;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.apache.logging.log4j.util.Strings.isBlank;

@Component
public class CsvCityParserAdapter implements CityCsvParserPort {

    @Override
    public Flux<CreateCityDto> parse(InputStream inputStream, List<String> errorsList) {
        return Mono.fromCallable(() -> collectCitiesFromCsv(new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)), errorsList))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapIterable(Function.identity());
    }

    private List<CreateCityDto> collectCitiesFromCsv(BufferedReader bufferedReader, List<String> errorsList) {
        try {
            var counter = new AtomicInteger(1);
            return new CsvToBeanBuilder<CsvCityRow>(bufferedReader)
                    .withType(CsvCityRow.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withSeparator(',')
                    .build()
                    .parse()
                    .stream()
                    .map(row -> {
                        var counterVal = counter.getAndIncrement();
                        if (isBlank(row.getName())) {
                            errorsList.add("City in row no. %s is not valid. City name is blank".formatted(counterVal));
                        }
                        return row.toApplicationDto();
                    })
                    .toList();
        } catch (Exception e) {
            throw e instanceof CityServiceException ce ? ce : new CityServiceException("The file extension .csv is required");
        }
    }
}
