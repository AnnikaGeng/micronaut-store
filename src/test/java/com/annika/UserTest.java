package com.annika;

import com.annika.entity.User;
import com.annika.entity.UserDTO;
import com.annika.entity.UserRole;
import com.annika.repository.UserRepository;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.security.authentication.UsernamePasswordCredentials;
import io.micronaut.security.token.render.BearerAccessRefreshToken;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest()
public class UserTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    private UserRepository userRepository;

    @Test
    void getAllUsersWithAdminRoleReturnsOk() throws ParseException {
        createNewAccount("sherlock", "password", UserRole.ROLE_ADMIN);
        HttpResponse<BearerAccessRefreshToken> rsp = getBearerAccessRefreshTokenHttpResponse("sherlock", "password");
        BearerAccessRefreshToken bearerAccessRefreshToken = rsp.body();

        assertEquals("sherlock", bearerAccessRefreshToken.getUsername());
        assertNotNull(bearerAccessRefreshToken.getAccessToken());
        assertTrue(JWTParser.parse(bearerAccessRefreshToken.getAccessToken()) instanceof SignedJWT);

        String accessToken = bearerAccessRefreshToken.getAccessToken();
        HttpRequest<?> req = HttpRequest.GET("/client/all/filter")
                .accept(MediaType.APPLICATION_JSON)
                .bearerAuth(accessToken);

        HttpResponse<String> result = client.toBlocking().exchange(req, String.class);
        assertEquals(200, result.getStatus().getCode());
    }

    @Test
    void getAllUsersWithNormalUserAccountReturnsBadRequest() throws ParseException {
        createNewAccount("emily", "password", UserRole.ROLE_USER);
        HttpResponse<BearerAccessRefreshToken> rsp = getBearerAccessRefreshTokenHttpResponse("emily", "password");
        BearerAccessRefreshToken bearerAccessRefreshToken = rsp.body();

        assertEquals("emily", bearerAccessRefreshToken.getUsername());
        assertNotNull(bearerAccessRefreshToken.getAccessToken());

        String accessToken = bearerAccessRefreshToken.getAccessToken();
        HttpClientResponseException e = assertThrows(HttpClientResponseException.class, () -> {
            HttpRequest<?> req = HttpRequest.GET("/client/all/filter")
                    .accept(MediaType.APPLICATION_JSON)
                    .bearerAuth(accessToken);
            client.toBlocking().exchange(req, String.class);
        });

        assertEquals(403, e.getStatus().getCode());
    }

    @Test
    void getSingleUserWithNormalUserAccountReturnsOk() throws ParseException {
        createNewAccount("joyce", "password", UserRole.ROLE_USER);
        String accessToken = getBearerAccessRefreshTokenHttpResponse("joyce", "password").body().getAccessToken();

        HttpRequest<?> req = HttpRequest.GET("/client/get").accept(MediaType.APPLICATION_JSON).bearerAuth(accessToken);

        HttpResponse<String> result = client.toBlocking().exchange(req, String.class);
        assertEquals(200, result.getStatus().getCode());
        assertEquals("""
                {"username":"joyce","type":"ROLE_USER"}""", result.body());
    }

    @Test
    void updateSingleUserWithNormalUserAccountReturnsOk() throws ParseException {
        createNewAccount("sweet", "password", UserRole.ROLE_USER);
        String accessToken = getBearerAccessRefreshTokenHttpResponse("sweet", "password").body().getAccessToken();

        HttpRequest<?> req = HttpRequest.PUT("/client/update", new UserDTO("mail", "password", UserRole.ROLE_ADMIN))
                .accept(MediaType.APPLICATION_JSON)
                .bearerAuth(accessToken);

        HttpResponse<String> result = client.toBlocking().exchange(req, String.class);
        assertEquals(200, result.getStatus().getCode());
        assertEquals("User updated", result.body());
    }

    @Test
    void deleteSingleUserReturnsOk() {
        createNewAccount("jane", "password", UserRole.ROLE_USER);
        String accessToken = getBearerAccessRefreshTokenHttpResponse("jane", "password").body().getAccessToken();

        HttpRequest<?> req = HttpRequest.DELETE("/client/delete").accept(MediaType.APPLICATION_JSON).bearerAuth(accessToken);

        HttpResponse<String> result = client.toBlocking().exchange(req, String.class);
        Optional<User> user = userRepository.findByUsername("jane");
        assertNull(user.orElse(null));
        assertEquals(200, result.getStatus().getCode());
        assertEquals("User deleted", result.body());
    }

    private HttpResponse<String> createNewAccount(String username, String password, UserRole type) {
        UserRole userType = type == null ? UserRole.ROLE_USER : type;
        HttpResponse<String> signuprsp = client.toBlocking()
                .exchange(HttpRequest.POST("/signup", new UserDTO(username, password, userType)), String.class);
        return signuprsp;
    }

    private HttpResponse<BearerAccessRefreshToken> getBearerAccessRefreshTokenHttpResponse(String username, String password) {
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(username, password);
        HttpRequest<?> res = HttpRequest.POST("/login", creds);
        HttpResponse<BearerAccessRefreshToken> loginrsp = client.toBlocking().exchange(res, BearerAccessRefreshToken.class);
        return loginrsp;
    }
}
