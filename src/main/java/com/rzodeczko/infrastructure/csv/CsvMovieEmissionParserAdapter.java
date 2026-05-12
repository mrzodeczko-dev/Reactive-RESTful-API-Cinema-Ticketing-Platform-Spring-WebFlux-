package com.rzodeczko.infrastructure.csv;

import com.opencsv.bean.CsvToBeanBuilder;
import com.rzodeczko.application.dto.CreateMovieEmissionDto;
import com.rzodeczko.application.exception.MovieEmissionServiceException;
import com.rzodeczko.application.port.out.MovieEmissionCsvParserPort;
import com.rzodeczko.application.validator.CreateMovieEmissionDtoValidator;
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
public class CsvMovieEmissionParserAdapter implements MovieEmissionCsvParserPort {

    private final CreateMovieEmissionDtoValidator createMovieEmissionDtoValidator;

    public CsvMovieEmissionParserAdapter(CreateMovieEmissionDtoValidator createMovieEmissionDtoValidator) {
        this.createMovieEmissionDtoValidator = createMovieEmissionDtoValidator;
    }

    @Override
    public Flux<CreateMovieEmissionDto> parse(InputStream inputStream, List<String> errorsList) {
        return Mono.fromCallable(() -> collectMovieEmissionsFromCsv(new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)), errorsList))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapIterable(Function.identity());
    }

    private List<CreateMovieEmissionDto> collectMovieEmissionsFromCsv(BufferedReader bufferedReader, List<String> errorsList) {
        try {
            var counter = new AtomicInteger(1);
            return new CsvToBeanBuilder<CsvMovieEmissionRow>(bufferedReader)
                    .withType(CsvMovieEmissionRow.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withSeparator(',')
                    .build()
                    .parse()
                    .stream()
                    .map(row -> {
                        var dto = row.toApplicationDto();
                        var errors = createMovieEmissionDtoValidator.validate(dto);
                        var counterVal = counter.getAndIncrement();
                        if (Validations.hasErrors(errors)) {
                            errorsList.add("Movie emission in row no. %s is not valid. %s"
                                    .formatted(counterVal, Validations.createErrorMessage(errors)));
                        }
                        return dto;
                    })
                    .toList();
        } catch (Exception e) {
            throw e instanceof MovieEmissionServiceException mee ? mee : new MovieEmissionServiceException("The file extension .csv is required");
        }
    }
}
