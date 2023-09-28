package com.annika.error;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record CustomError(
        int status,
        String error,
        String message
) implements RestAPIResponse{
}
