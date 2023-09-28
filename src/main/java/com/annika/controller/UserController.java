package com.annika.controller;

import com.annika.entity.UserDTO;
import com.annika.service.UserService;
import com.nimbusds.jwt.JWTParser;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;

import java.text.ParseException;
import java.util.List;
import java.util.Optional;

import static io.micronaut.http.HttpHeaders.AUTHORIZATION;

@Controller("/client")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class UserController {

    @Inject
    private UserService userService;

    @Get("/all/filter{?max, offset}")
    @Secured({"ROLE_ADMIN"})
    public List<UserDTO> getAllUsers(@QueryValue Optional<Integer> max, @QueryValue Optional<Integer> offset) {
        List<UserDTO> users = userService.getAllUsersAndProducts();
        return users.stream()
                .skip(offset.orElse(0))
                .limit(max.orElse(10))
                .toList();
    }

    @Get("/get")
    public UserDTO getSingleUser(@Header(AUTHORIZATION) String token) throws ParseException {
        String username = JWTParser.parse(token.substring(7))
                .getJWTClaimsSet()
                .getSubject();
        return userService.getUserDetailAndProducts(username);
    }

    @Put("/update")
    public HttpResponse updateUser(@Header(AUTHORIZATION) String token, @Body UserDTO userDTO) throws ParseException {
        String username = JWTParser.parse(token.substring(7))
                .getJWTClaimsSet()
                .getSubject();
        userService.updateUser(username, userDTO);
        return HttpResponse.ok("User updated");
    }

    @Delete("/delete")
    public HttpResponse deleteUser(@Header(AUTHORIZATION) String token) throws ParseException {
        String username = JWTParser.parse(token.substring(7))
                .getJWTClaimsSet()
                .getSubject();
        userService.deleteUser(username);
        return HttpResponse.ok("User deleted");
    }
}
