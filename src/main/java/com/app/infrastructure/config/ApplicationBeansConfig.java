package com.app.infrastructure.config;

import com.app.application.service.CinemaHallService;
import com.app.application.service.CinemaService;
import com.app.application.service.CityService;
import com.app.application.service.EmailService;
import com.app.application.service.MovieEmissionService;
import com.app.application.service.MovieService;
import com.app.application.service.StatisticsService;
import com.app.application.service.TicketOrderService;
import com.app.application.service.TicketPurchaseService;
import com.app.application.service.TicketService;
import com.app.application.service.UsersService;
import com.app.application.validator.AddCinemaHallToCinemaDtoValidator;
import com.app.application.validator.CreateCinemaDtoValidator;
import com.app.application.validator.CreateCinemaHallDtoValidator;
import com.app.application.validator.CreateMailDtoValidator;
import com.app.application.validator.CreateMailsDtoValidator;
import com.app.application.validator.CreateMovieDtoValidator;
import com.app.application.validator.CreateTicketPurchaseDtoValidator;
import com.app.application.validator.CreateTicketsOrderDtoValidator;
import com.app.application.validator.CreateUserDtoValidator;
import com.app.domain.cinema.CinemaRepository;
import com.app.domain.cinema_hall.CinemaHallRepository;
import com.app.domain.city.CityRepository;
import com.app.domain.movie.MovieRepository;
import com.app.domain.movie_emission.MovieEmissionRepository;
import com.app.domain.security.AdminRepository;
import com.app.domain.security.UserRepository;
import com.app.domain.ticket.TicketRepository;
import com.app.domain.ticket_order.TicketOrderRepository;
import com.app.domain.ticket_purchase.TicketPurchaseRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.reactive.TransactionalOperator;

/**
 * Wires application-layer services and validators as Spring beans without
 * polluting the application layer itself with framework annotations. This
 * keeps {@code com.app.application.*} free of Spring stereotypes so the
 * application core remains framework-agnostic (clean DDD / hexagonal style).
 */
@Configuration
public class ApplicationBeansConfig {

    @Bean
    public AddCinemaHallToCinemaDtoValidator addCinemaHallToCinemaDtoValidator() {
        return new AddCinemaHallToCinemaDtoValidator();
    }

    @Bean
    public CreateCinemaDtoValidator createCinemaDtoValidator() {
        return new CreateCinemaDtoValidator();
    }

    @Bean
    public CreateCinemaHallDtoValidator createCinemaHallDtoValidator() {
        return new CreateCinemaHallDtoValidator();
    }

    @Bean
    public CreateMailDtoValidator createMailDtoValidator() {
        return new CreateMailDtoValidator();
    }

    @Bean
    public CreateMailsDtoValidator createMailsDtoValidator() {
        return new CreateMailsDtoValidator();
    }

    @Bean
    public CreateMovieDtoValidator createMovieDtoValidator() {
        return new CreateMovieDtoValidator();
    }

    @Bean
    public CreateTicketPurchaseDtoValidator createTicketPurchaseDtoValidator() {
        return new CreateTicketPurchaseDtoValidator();
    }

    @Bean
    public CreateTicketsOrderDtoValidator createTicketsOrderDtoValidator() {
        return new CreateTicketsOrderDtoValidator();
    }

    @Bean
    public CreateUserDtoValidator createUserDtoValidator() {
        return new CreateUserDtoValidator();
    }

    @Bean
    public CinemaHallService cinemaHallService(CinemaHallRepository cinemaHallRepository,
                                               CinemaRepository cinemaRepository,
                                               TransactionalOperator transactionalOperator) {
        return new CinemaHallService(cinemaHallRepository, cinemaRepository, transactionalOperator);
    }

    @Bean
    public CinemaService cinemaService(CinemaRepository cinemaRepository,
                                       CinemaHallRepository cinemaHallRepository,
                                       CityRepository cityRepository,
                                       CreateCinemaDtoValidator createCinemaDtoValidator,
                                       TransactionalOperator transactionalOperator) {
        return new CinemaService(cinemaRepository, cinemaHallRepository, cityRepository,
                createCinemaDtoValidator, transactionalOperator);
    }

    @Bean
    public CityService cityService(CityRepository cityRepository,
                                   CinemaRepository cinemaRepository,
                                   CinemaHallRepository cinemaHallRepository,
                                   TransactionalOperator transactionalOperator) {
        return new CityService(cityRepository, cinemaRepository, cinemaHallRepository, transactionalOperator);
    }

    @Bean
    public EmailService emailService(JavaMailSender mailSender,
                                     CreateMailDtoValidator createMailDtoValidator,
                                     CreateMailsDtoValidator createMailsDtoValidator) {
        return new EmailService(mailSender, createMailDtoValidator, createMailsDtoValidator);
    }

    @Bean
    public MovieEmissionService movieEmissionService(MovieEmissionRepository movieEmissionRepository,
                                                     CinemaHallRepository cinemaHallRepository,
                                                     MovieRepository movieRepository,
                                                     TransactionalOperator transactionalOperator) {
        return new MovieEmissionService(movieEmissionRepository, cinemaHallRepository, movieRepository,
                transactionalOperator);
    }

    @Bean
    public MovieService movieService(MovieRepository movieRepository,
                                     UserRepository userRepository,
                                     CreateMovieDtoValidator createMovieDtoValidator) {
        return new MovieService(movieRepository, userRepository, createMovieDtoValidator);
    }

    @Bean
    public StatisticsService statisticsService(TicketPurchaseRepository ticketPurchaseRepository,
                                               CityRepository cityRepository,
                                               MovieRepository movieRepository) {
        return new StatisticsService(ticketPurchaseRepository, cityRepository, movieRepository);
    }

    @Bean
    public TicketOrderService ticketOrderService(TicketOrderRepository ticketOrderRepository,
                                                 MovieEmissionRepository movieEmissionRepository,
                                                 UserRepository userRepository,
                                                 TicketRepository ticketRepository,
                                                 CreateTicketsOrderDtoValidator createTicketsOrderDtoValidator,
                                                 TransactionalOperator transactionalOperator) {
        return new TicketOrderService(ticketOrderRepository, movieEmissionRepository, userRepository,
                ticketRepository, createTicketsOrderDtoValidator, transactionalOperator);
    }

    @Bean
    public TicketPurchaseService ticketPurchaseService(TicketPurchaseRepository ticketPurchaseRepository,
                                                       CreateTicketPurchaseDtoValidator createTicketPurchaseDtoValidator,
                                                       MovieEmissionRepository movieEmissionRepository,
                                                       MovieRepository movieRepository,
                                                       UserRepository userRepository,
                                                       CinemaHallRepository cinemaHallRepository,
                                                       TicketRepository ticketRepository,
                                                       CityRepository cityRepository,
                                                       TicketOrderRepository ticketOrderRepository,
                                                       CinemaRepository cinemaRepository,
                                                       TransactionalOperator transactionalOperator) {
        return new TicketPurchaseService(ticketPurchaseRepository, createTicketPurchaseDtoValidator,
                movieEmissionRepository, movieRepository, userRepository, cinemaHallRepository,
                ticketRepository, cityRepository, ticketOrderRepository, cinemaRepository,
                transactionalOperator);
    }

    @Bean
    public TicketService ticketService() {
        return new TicketService();
    }

    @Bean
    public UsersService usersService(UserRepository userRepository,
                                     CreateUserDtoValidator createUserDtoValidator,
                                     PasswordEncoder passwordEncoder,
                                     AdminRepository adminRepository,
                                     TransactionalOperator transactionalOperator) {
        return new UsersService(userRepository, createUserDtoValidator, passwordEncoder,
                adminRepository, transactionalOperator);
    }
}
