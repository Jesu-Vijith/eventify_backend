package com.eventmanagement.EventManagement.security;

import com.eventmanagement.EventManagement.exception.CustomException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private AccessDeniedFilter accessDeniedFilter;

    @Autowired
    private TokenFilter tokenFilter;


    private  static final String[] AUTH_DOC_LIST={
            "/api/v1/auth/**",
            "/v3/api-docs/**",
            "v3/api-docs.yaml",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(customizer->customizer.disable())
                .authorizeHttpRequests(authorize-> authorize
                        .requestMatchers("/api/signup").permitAll()
                        .requestMatchers("/api/signup_confirmation").permitAll()
                        .requestMatchers("/api/keyGeneration").permitAll()
                        .requestMatchers("/api/resend_signup_confirmation").permitAll()
                        .requestMatchers("/api/login").permitAll()
                        .requestMatchers("/api/forgotPassword").permitAll()
                        .requestMatchers("/api/forgotPassword_resend").permitAll()
                        .requestMatchers("/api/update_forgot_password").permitAll()
//                        .requestMatchers("/api/change_password").permitAll()
//                        .requestMatchers("/api/logout").permitAll()
//                        .requestMatchers("/api/delete_user").permitAll()
                        .requestMatchers("/api/refreshToken").permitAll()

//                        .requestMatchers("/api/event/create").permitAll()
//                        .requestMatchers("/api/event/getAllEvents/{userId}").permitAll()
//                        .requestMatchers("/api/event/getEventById/{userId}/{eventId}").permitAll()
//                        .requestMatchers("/api/event/delete/{eventId}").permitAll()
//                        .requestMatchers("/api/event/update/{eventId}").permitAll()
//
//                        .requestMatchers("/api/ticket/bookTicket").permitAll()
//                        .requestMatchers("/api/ticket/getAllTickets/{attendeeId}").permitAll()
//                        .requestMatchers("/api/ticket/getTicketById/{ticketId}").permitAll()
//                        .requestMatchers("/api/ticket/getAllEvents").permitAll()
//                        .requestMatchers("/api/ticket/cancelTicket/{ticketId}").permitAll()
//
//                        .requestMatchers("/api/paypal/confirmPayment").permitAll()
//                        .requestMatchers("/api/paypal/cancel").permitAll()
//                        .requestMatchers("/api/paypal/success").permitAll()


                        .requestMatchers("/api/passwordEncrypt").permitAll()
                        .requestMatchers("/api/encryptedUsernamePassword").permitAll()
                        .requestMatchers("/api/encryptedEmailCodePassword").permitAll()
                        .requestMatchers("/api/encryptedPasswords").permitAll()
                        .requestMatchers(AUTH_DOC_LIST).permitAll()
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptionHandling ->
                exceptionHandling
                        .accessDeniedHandler(accessDeniedFilter)
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");

                            CustomException errorResponse = new CustomException(
                                    "Unauthorized",
                                    "You do not have permission to access this resource.",
                                    HttpStatus.UNAUTHORIZED
                            );

                            ObjectMapper objectMapper = new ObjectMapper();
                            String jsonResponse = objectMapper.writeValueAsString(errorResponse);
                            response.getWriter().write(jsonResponse);
                            response.getWriter().flush();
                        })).addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class)
                        .build();
    }
}
