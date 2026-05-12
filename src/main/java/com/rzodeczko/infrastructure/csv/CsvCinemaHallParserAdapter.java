package com.rzodeczko.infrastructure.csv;

import com.opencsv.bean.CsvToBeanBuilder;
import com.rzodeczko.application.dto.CreateCinemaHallDto;
import com.rzodeczko.application.exception.CinemaHallServiceException;
import com.rzodeczko.application.port.out.CinemaHallCsvParserPort;
import com.rzodeczko.application.validator.CreateCinemaHallDtoValidator;
import com.rzodeczko.application.validator.util.Validations;
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

@Component
public class CsvCinemaHallParserAdapter implements CinemaHallCsvParserPort {

    private final CreateCinemaHallDtoValidator createCinemaHallDtoValidator;

    public CsvCinemaHallParserAdapter(CreateCinemaHallDtoValidator createCinemaHallDtoValidator) {
        this.createCinemaHallDtoValidator = createCinemaHallDtoValidator;
    }

    @Override
    public Flux<CreateCinemaHallDto> parse(InputStream inputStream, List<String> errorsList) {
        return Mono.fromCallable(() -> collectCinemaHallsFromCsv(new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)), errorsList))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapIterable(Function.identity());
    }

    private List<CreateCinemaHallDto> collectCinemaHallsFromCsv(BufferedReader bufferedReader, List<String> errorsList) {
        try {
            var counter = new AtomicInteger(1);
            return new CsvToBeanBuilder<CsvCinemaHallRow>(bufferedReader)
                    .withType(CsvCinemaHallRow.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withSeparator(',')
                    .build()
                    .parse()
                    .stream()
                    .map(row -> {
                        var dto = row.toApplicationDto();
                        var errors = createCinemaHallDtoValidator.validate(dto);
                        var counterVal = counter.getAndIncrement();
                        if (Validations.hasErrors(errors)) {
                            errorsList.add("Cinema hall in row no. %s is not valid. %s"
                                    .formatted(counterVal, Validations.createErrorMessage(errors)));
                        }
                        return dto;
                    })
                    .toList();
        } catch (Exception e) {
            throw e instanceof CinemaHallServiceException ce ? ce : new CinemaHallServiceException("The file extension .csv is required");
        }
    }
}
