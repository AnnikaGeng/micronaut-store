package com.annika;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.security.authentication.UsernamePasswordCredentials;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static io.micronaut.http.HttpStatus.UNAUTHORIZED;
import static io.micronaut.http.MediaType.TEXT_PLAIN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@MicronautTest()
public class AuthenticationTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void accessingASecuredUrlWithoutAuthenticatingReturnsUnauthorized() {
        HttpClientResponseException e = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(HttpRequest.GET("/client").accept(TEXT_PLAIN));
        });

        assertEquals(UNAUTHORIZED, e.getStatus());
    }

    @Test
    void signupUsingValidCredentialsReturnsOk() {
        HttpResponse<String> signuprsp = createNewAccount("sherlock", "password");
        assertEquals(200, signuprsp.getStatus().getCode());
    }

    @Test
    void signupWithDuplicateUsernameReturnsBadRequest() {
        HttpResponse<String> signup = createNewAccount("amy", "password");
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials("amy", "password");
        HttpRequest<?> res = HttpRequest.POST("/signup", creds);
        HttpClientResponseException e = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(res, String.class);
        });

        assertEquals(400, e.getStatus().getCode());
    }

    @Test
    void loginWithValidCredentialsReturnsOk() {
        createNewAccount("emma", "password");
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials("emma", "password");
        HttpRequest<?> res = HttpRequest.POST("/login", creds);
        HttpResponse<String> loginrsp = client.toBlocking().exchange(res, String.class);
        assertEquals(200, loginrsp.getStatus().getCode());
    }

    @Test
    void loginWithInvalidCredentialsReturnsBadRequest() {
        createNewAccount("lily", "password");
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials("lily", "wrongpassword");
        HttpRequest<?> res = HttpRequest.POST("/login", creds);
        HttpClientResponseException e = assertThrows(HttpClientResponseException.class, () -> {
            client.toBlocking().exchange(res, String.class);
        });

        assertEquals(401, e.getStatus().getCode());
    }

    private HttpResponse<String> createNewAccount(String username, String password) {
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(username, password);
        HttpRequest<?> res = HttpRequest.POST("/signup", creds);
        HttpResponse<String> signuprsp = client.toBlocking().exchange(res, String.class);
        return signuprsp;
    }

}
