package com.annika;

import com.annika.entity.Product;
import com.annika.entity.ProductDTO;
import com.annika.entity.UserDTO;
import com.annika.entity.UserRole;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.security.authentication.UsernamePasswordCredentials;
import io.micronaut.security.token.render.BearerAccessRefreshToken;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest()
public class ProductTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void getALLProductTest() {

        createNewAccount("amy", "password", UserRole.ROLE_USER);
        String accessToken = getBearerAccessRefreshTokenHttpResponse("amy", "password").body().getAccessToken();

        HttpRequest<?> req = HttpRequest.GET("/products/filter")
                .bearerAuth(accessToken);
        HttpResponse<String> res = client.toBlocking().exchange(req, String.class);
        assertEquals(200, res.getStatus().getCode());
        assertEquals("[]", res.body());


        //        add new product
        createNewAccount("bob", "password", UserRole.ROLE_ADMIN);
        String accessToken2 = getBearerAccessRefreshTokenHttpResponse("bob", "password").body().getAccessToken();

        ProductDTO product = new ProductDTO("Test product", "Test description", 10.0);
        HttpRequest<?> addProductReq = HttpRequest.POST("/products/add", product)
                .bearerAuth(accessToken2);
        HttpResponse<String> resOfNonEmpty = client.toBlocking().exchange(addProductReq, String.class);


        //        recheck all products
        res = client.toBlocking().exchange(req, String.class);
        assertEquals(200, res.getStatus().getCode());
        assertNotNull(res.body());
    }

    @Test
    void addNewProductTest() {
        createNewAccount("jeff", "password", UserRole.ROLE_ADMIN);
        String accessToken = getBearerAccessRefreshTokenHttpResponse("jeff", "password").body().getAccessToken();

        ProductDTO product = new ProductDTO("Test product", "Test description", 10.0);
        HttpRequest<?> req = HttpRequest.POST("/products/add", product)
                .bearerAuth(accessToken);

        HttpResponse<String> res = client.toBlocking().exchange(req, String.class);
        assertEquals(200, res.getStatus().getCode());
        assertNotNull(res.body());
    }

    @Test
    void addProductAndReturnProductDetail() {
        createNewAccount("uu", "password", UserRole.ROLE_ADMIN);
        String accessToken = getBearerAccessRefreshTokenHttpResponse("uu", "password").body().getAccessToken();

        ProductDTO product = new ProductDTO("Test product", "Test description", 10.0);
        HttpRequest<?> req = HttpRequest.POST("/products/add", product)
                .bearerAuth(accessToken);
        HttpResponse<String> res = client.toBlocking().exchange(req, String.class);

        // get product detail
        HttpRequest<?> req2 = HttpRequest.GET("/products/1")
                .bearerAuth(accessToken);
        HttpResponse<String> res2 = client.toBlocking().exchange(req2, String.class);
        assertEquals(200, res2.getStatus().getCode());
        assertNotNull(res2.body());
    }

    @Test
    void updateProductWithAdminAccountReturnOk() {
        createNewAccount("kk", "password", UserRole.ROLE_ADMIN);
        String accessToken = getBearerAccessRefreshTokenHttpResponse("kk", "password").body().getAccessToken();

        ProductDTO product = new ProductDTO("Test product", "Test description", 10.0);
        HttpRequest<?> req = HttpRequest.POST("/products/add", product)
                .bearerAuth(accessToken);
        HttpResponse<ProductDTO> res = client.toBlocking().exchange(req, ProductDTO.class);
        Long id = res.body().getId();

        ProductDTO product2 = new ProductDTO("Test product2", "Test description2", 20.0);
        HttpRequest<?> req2 = HttpRequest.PUT("/products/update/" + id, product2)
                .bearerAuth(accessToken);
        HttpResponse<String> res2 = client.toBlocking().exchange(req2, String.class);
        assertEquals(200, res2.getStatus().getCode());
        assertEquals("Product updated", res2.body());
    }

    @Test
    void deleteProductAndRelationshipWithUsers() {
        createNewAccount("ll", "password", UserRole.ROLE_ADMIN);
        String accessToken = getBearerAccessRefreshTokenHttpResponse("ll", "password").body().getAccessToken();

        ProductDTO product = new ProductDTO("Test product", "Test description", 10.0);
        HttpRequest<?> req = HttpRequest.POST("/products/add", product)
                .bearerAuth(accessToken);
        HttpResponse<ProductDTO> res = client.toBlocking().exchange(req, ProductDTO.class);
        Long id = res.body().getId();

        //        add product to user
        HttpRequest<?> req2 = HttpRequest.GET("/client/products/add/" + id)
                .bearerAuth(accessToken);
        HttpResponse<String> res2 = client.toBlocking().exchange(req2, String.class);
        assertEquals(200, res2.getStatus().getCode());
        assertEquals("Product added to user", res2.body());

        //        delete product
        HttpRequest<?> req3 = HttpRequest.DELETE("/products/delete/" + id)
                .bearerAuth(accessToken);
        HttpResponse<String> res3 = client.toBlocking().exchange(req3, String.class);
        assertEquals(200, res3.getStatus().getCode());
        assertEquals("Product deleted", res3.body());

        //        check if product is deleted
        HttpClientResponseException e = assertThrows(HttpClientResponseException.class, () -> {
            HttpRequest<?> req4 = HttpRequest.GET("/products/" + id)
                    .bearerAuth(accessToken);
            client.toBlocking().exchange(req4, String.class);
        });
        assertEquals(400, e.getStatus().getCode());
        assertEquals("Choose a new product", e.getMessage());

        //        check if product is deleted from user
        HttpRequest<?> req5 = HttpRequest.GET("/client/products/filter")
                .bearerAuth(accessToken);
        HttpResponse<String> res5 = client.toBlocking().exchange(req5, String.class);
        assertEquals(200, res5.getStatus().getCode());
        assertEquals("[]", res5.body());
    }

    @Test
    void returnAListOfUsersWhoHaveAProduct() {
        createNewAccount("mm", "password", UserRole.ROLE_ADMIN);
        String accessToken = getBearerAccessRefreshTokenHttpResponse("mm", "password").body().getAccessToken();

        ProductDTO product = new ProductDTO("Test product", "Test description", 10.0);
        HttpRequest<?> req = HttpRequest.POST("/products/add", product)
                .bearerAuth(accessToken);
        HttpResponse<ProductDTO> res = client.toBlocking().exchange(req, ProductDTO.class);
        Long id = res.body().getId();

        //        add product to user
        HttpRequest<?> req2 = HttpRequest.GET("/client/products/add/" + id)
                .bearerAuth(accessToken);
        HttpResponse<String> res2 = client.toBlocking().exchange(req2, String.class);


        //        check if the user have the product
        HttpRequest<?> req3 = HttpRequest.GET("/products/" + id + "/clients")
                .bearerAuth(accessToken);
        HttpResponse<String> res3 = client.toBlocking().exchange(req3, String.class);
        assertEquals(200, res3.getStatus().getCode());
        assertEquals("[{\"username\":\"mm\",\"type\":\"ROLE_ADMIN\"}]", res3.body());
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
