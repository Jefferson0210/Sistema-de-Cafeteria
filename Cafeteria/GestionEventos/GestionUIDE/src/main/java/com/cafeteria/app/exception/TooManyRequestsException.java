package com.cafeteria.app.exception;

/** Se excedió un límite de rate limiting (por email). El handler global la mapea a 429 + Retry-After. */
public class TooManyRequestsException extends RuntimeException {

    private final long retryAfterSeconds;

    public TooManyRequestsException(long retryAfterSeconds) {
        super("Demasiados intentos, espera un momento");
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
