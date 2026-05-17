package com.rzodeczko.presentation.routing;

import com.rzodeczko.presentation.routing.handlers.CinemasHandler;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class CinemasRouting extends BaseJsonRouter {

    @Bean
    @RouterOperations({
            @RouterOperation(method = RequestMethod.GET, path = "/cinemas", beanClass = CinemasHandler.class, beanMethod = "getAll"),
            @RouterOperation(method = RequestMethod.GET, path = "/cinemas/city/{city}", beanClass = CinemasHandler.class, beanMethod = "getAllCinemasByCity")
    })
    public RouterFunction<ServerResponse> cinemasRouterFunction(CinemasHandler cinemasHandler) {
        return route()
                .path("/cinemas", builder -> builder
                        .nest(jsonAccept(), nested -> nested
                                .GET("", _ -> cinemasHandler.getAll())
                                .GET("/city/{cityName}", cinemasHandler::getAllCinemasByCity)
                        )
                )
                .build();
    }
}
