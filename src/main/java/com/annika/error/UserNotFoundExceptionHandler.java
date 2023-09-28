package com.annika.error;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;

@Produces
@Singleton
@Requires(classes = {UserNotFoundException.class, ExceptionHandler.class})
public class UserNotFoundExceptionHandler implements ExceptionHandler<UserNotFoundException, HttpResponse<CustomError>>{

    @Override
    public HttpResponse<CustomError> handle(HttpRequest request, UserNotFoundException exception) {
        return HttpResponse.badRequest(
                new CustomError(HttpStatus.BAD_REQUEST.getCode(),
                        "User not found",
                        exception.getMessage()
                )
        );
    }
}
