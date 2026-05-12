package com.rzodeczko.infrastructure.csv;

import com.opencsv.bean.CsvToBeanBuilder;
import com.rzodeczko.application.dto.CreateCinemaDto;
import com.rzodeczko.application.exception.CinemaServiceException;
import com.rzodeczko.application.port.out.CinemaCsvParserPort;
import com.rzodeczko.application.validator.CreateCinemaDtoValidator;
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
public class CsvCinemaParserAdapter implements CinemaCsvParserPort {

    private final CreateCinemaDtoValidator createCinemaDtoValidator;

    public CsvCinemaParserAdapter(CreateCinemaDtoValidator createCinemaDtoValidator) {
        this.createCinemaDtoValidator = createCinemaDtoValidator;
    }

    @Override
    public Flux<CreateCinemaDto> parse(InputStream inputStream, List<String> errorsList) {
        return Mono.fromCallable(() -> collectCinemasFromCsv(new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)), errorsList))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapIterable(Function.identity());
    }

    private List<CreateCinemaDto> collectCinemasFromCsv(BufferedReader bufferedReader, List<String> errorsList) {
        try {
            var counter = new AtomicInteger(1);
            return new CsvToBeanBuilder<CsvCinemaRow>(bufferedReader)
                    .withType(CsvCinemaRow.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withSeparator(',')
                    .build()
                    .parse()
                    .stream()
                    .map(row -> {
                        var counterVal = counter.getAndIncrement();
                        var dto = row.toApplicationDto(errorsList, counterVal);
                        var errors = createCinemaDtoValidator.validate(dto);
                        if (Validations.hasErrors(errors)) {
                            errorsList.add("Cinema in row no. %s is not valid. %s"
                                    .formatted(counterVal, Validations.createErrorMessage(errors)));
                        }
                        return dto;
                    })
                    .toList();
        } catch (Exception e) {
            throw e instanceof CinemaServiceException ce ? ce : new CinemaServiceException("The file extension .csv is required");
        }
    }
}
