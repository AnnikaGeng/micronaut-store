package com.annika.controller;

import com.annika.entity.ProductDTO;
import com.annika.entity.User;
import com.annika.entity.UserDTO;
import com.annika.entity.UserProduct;
import com.annika.repository.ProductRepository;
import com.annika.repository.UserProductRepository;
import com.annika.repository.UserRepository;
import com.annika.service.UserService;
import com.nimbusds.jwt.JWTParser;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;

import java.text.ParseException;
import java.util.List;
import java.util.Optional;

import static io.micronaut.http.HttpHeaders.AUTHORIZATION;

@Controller("/client/products/")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class UserProductController {

    @Inject
    private UserService userService;

    @Inject
    private UserProductRepository userProductRepository;

    @Inject
    private UserRepository userRepository;
    @Inject
    private ProductRepository productRepository;

    @Get("/add/{id}")
    public HttpResponse addProductToUser(@Header(AUTHORIZATION) String token, Long id) throws ParseException {
        String username = JWTParser.parse(token.substring(7))
                .getJWTClaimsSet()
                .getSubject();
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            UserProduct userProduct = new UserProduct();
            userProduct.setUser(user.get());
            userProduct.setProduct(productRepository.findById(id).get());
            userProductRepository.save(userProduct);
            return HttpResponse.ok("Product added to user");
        }
        return HttpResponse.badRequest("Something went wrong, try again later");
    }

    @Get("/filter{?max, offset}")
    public List<ProductDTO> getAllProducts(@Header(AUTHORIZATION) String token,
                                           @QueryValue Optional<Integer> max,
                                           @QueryValue Optional<Integer> offset) throws ParseException {
        String username = JWTParser.parse(token.substring(7))
                .getJWTClaimsSet()
                .getSubject();
        List<UserProduct> userProducts = userProductRepository.findByUser(userRepository.findByUsername(username).get());
        return userProducts.stream()
                .map(userProduct -> new ProductDTO(userProduct.getProduct().getProduct_name(),
                        userProduct.getProduct().getDescription(),
                        userProduct.getProduct().getPrice()))
                .skip(offset.orElse(0))
                .limit(max.orElse(10))
                .toList();
    }

    @Delete("/delete/{id}")
    public HttpResponse deleteProductFromClient(@Header(AUTHORIZATION) String token, @PathVariable Long id) throws ParseException {
        String username = JWTParser.parse(token.substring(7))
                .getJWTClaimsSet()
                .getSubject();
        User user = userRepository.findByUsername(username).get();
        UserProduct userProduct = userProductRepository.findByUserAndProduct(user, productRepository.findById(id).get());
        userProductRepository.delete(userProduct);
        return HttpResponse.ok("Product deleted from user");
    }
}
