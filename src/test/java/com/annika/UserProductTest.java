package com.annika;

import com.annika.controller.ProductController;
import com.annika.entity.Product;
import com.annika.entity.ProductDTO;
import com.annika.entity.UserDTO;
import com.annika.entity.UserRole;
import com.annika.repository.ProductRepository;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.security.authentication.UsernamePasswordCredentials;
import io.micronaut.security.token.render.BearerAccessRefreshToken;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@MicronautTest()
public class UserProductTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    private ProductRepository productRepository;

    @Test
    void getAllProductsForUserReturnsOk() {
        createNewAccount("jane", "password", UserRole.ROLE_ADMIN);
        String accessToken = getBearerAccessRefreshTokenHttpResponse("jane", "password").body().getAccessToken();

        HttpRequest<?> req = HttpRequest.GET("/client/products/filter")
                .accept(io.micronaut.http.MediaType.APPLICATION_JSON)
                .bearerAuth(accessToken);
        HttpResponse<String> result = client.toBlocking().exchange(req, String.class);
        assertEquals(200, result.getStatus().getCode());
        assertEquals("[]", result.body());  // empty list


        //        add new product
        ProductDTO product = new ProductDTO("Test product", "Test description", 10.0);
        HttpRequest<?> addProductReq = HttpRequest.POST("/products/add", product)
                .bearerAuth(accessToken);
        HttpResponse<String> addProductRes = client.toBlocking().exchange(addProductReq, String.class);

        //        add product to user
        HttpRequest<?> req1 = HttpRequest.GET("/client/products/add/1")
                .bearerAuth(accessToken);
        HttpResponse<String> res0 = client.toBlocking().exchange(req1, String.class);

        HttpResponse<String> res = client.toBlocking().exchange(req, String.class);
        assertEquals(200, res.getStatus().getCode());
        assertEquals("[{\"product_name\":\"Test product\",\"description\":\"Test description\",\"price\":10.0}]", res.body());
    }

    @Test
    void addProductToUserReturnsOk() {
        createNewAccount("jeff", "password", UserRole.ROLE_ADMIN);
        String accessToken = getBearerAccessRefreshTokenHttpResponse("jeff", "password").body().getAccessToken();

        ProductDTO product = new ProductDTO("Test product", "Test description", 10.0);
        HttpRequest<?> req = HttpRequest.POST("/products/add", product)
                .bearerAuth(accessToken);

        HttpResponse<String> res = client.toBlocking().exchange(req, String.class);
        assertEquals(200, res.getStatus().getCode());

        HttpRequest<?> req2 = HttpRequest.GET("/client/products/add/1")
                .bearerAuth(accessToken);
        HttpResponse<String> result = client.toBlocking().exchange(req2, String.class);
        assertEquals(200, result.getStatus().getCode());
        assertEquals("Product added to user", result.body());
    }

    @Test
    void deleteProductFromUserAccount() {
        createNewAccount("mike", "password", UserRole.ROLE_ADMIN);
        String accessToken = getBearerAccessRefreshTokenHttpResponse("mike", "password").body().getAccessToken();

        ProductDTO product = new ProductDTO("Test product", "Test description", 10.0);
        HttpRequest<?> req = HttpRequest.POST("/products/add", product)
                .bearerAuth(accessToken);
        HttpResponse<String> res = client.toBlocking().exchange(req, String.class);

        HttpRequest<?> req2 = HttpRequest.GET("/client/products/add/1")
                .bearerAuth(accessToken);
        HttpResponse<String> result = client.toBlocking().exchange(req2, String.class);
        assertEquals(200, result.getStatus().getCode());
        assertEquals("Product added to user", result.body());

        HttpRequest<?> req3 = HttpRequest.DELETE("/client/products/delete/1")
                .bearerAuth(accessToken);
        HttpResponse<String> result2 = client.toBlocking().exchange(req3, String.class);
        assertEquals(200, result2.getStatus().getCode());
        assertEquals("Product deleted from user", result2.body());
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
