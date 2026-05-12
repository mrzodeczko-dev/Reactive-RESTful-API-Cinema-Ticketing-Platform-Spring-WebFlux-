package com.rzodeczko.presentation.csv;

import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Flux;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.function.Function;

@Component
public class CsvMultipartFileReader {

    private static final String FILE_PART_NAME = "file";

    public <T> Flux<T> readCsvFile(ServerRequest request,
                                   Function<InputStream, Flux<T>> importer,
                                   Function<String, ? extends RuntimeException> exceptionFactory) {
        return request.multipartData()
                .flatMapMany(parts -> {
                    Part part = parts.toSingleValueMap().get(FILE_PART_NAME);
                    if (!(part instanceof FilePart filePart)) {
                        return Flux.error(exceptionFactory.apply("CSV file part 'file' is required"));
                    }
                    return DataBufferUtils.join(filePart.content())
                            .flatMapMany(dataBuffer -> {
                                var bytes = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(bytes);
                                DataBufferUtils.release(dataBuffer);
                                return importer.apply(new ByteArrayInputStream(bytes));
                            });
                });
    }
}
