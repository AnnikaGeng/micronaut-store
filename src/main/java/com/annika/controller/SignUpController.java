package com.annika.controller;

import com.annika.entity.UserDTO;
import com.annika.error.CustomError;
import com.annika.error.RestAPIResponse;
import com.annika.service.UserService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import jakarta.validation.Valid;

import javax.swing.text.html.Option;
import java.util.Optional;

@Controller("/signup")
@Secured(SecurityRule.IS_ANONYMOUS)
public class SignUpController {

    @Inject
    private UserService userService;

    @Post
    public HttpResponse<RestAPIResponse> signUp(@Body @Valid UserDTO userDTO) {
        Optional<UserDTO> user = userService.getUserByUsername(userDTO.getUsername());
        if (user.isPresent()) {
            return HttpResponse.badRequest().body(
                    new CustomError(
                            HttpStatus.BAD_REQUEST.getCode(),
                            "User already exists",
                            "Choose a new username"
                    )
            );
        }
        userService.createUser(userDTO);
        return HttpResponse.ok();
    }
}
