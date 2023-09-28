package com.annika.auth.jwt;

import com.annika.entity.User;
import com.annika.entity.UserDTO;
import com.annika.error.UserNotFoundException;
import com.annika.repository.UserRepository;
import com.annika.service.UserService;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.authentication.AuthenticationProvider;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.hibernate.sql.ast.tree.expression.Collation;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import static com.fasterxml.jackson.databind.type.LogicalType.Collection;

@Singleton
public class AuthenticationProviderUserPassword implements AuthenticationProvider<HttpRequest<?>> {

    private final static Logger LOG = Logger.getLogger(AuthenticationProviderUserPassword.class.getName());

    @Inject
    private UserRepository userRepository;
    @Inject
    private UserService userService;

    @Override
    public Publisher<AuthenticationResponse> authenticate(@Nullable HttpRequest<?> httpRequest,
                                                          AuthenticationRequest<?, ?> authenticationRequest) {
        return Flux.create(emitter -> {
            final String username = authenticationRequest.getIdentity().toString();
            final String password = authenticationRequest.getSecret().toString();
            try {
                User user = userRepository.findByUsername(username).get();
                if (user.getPassword().equals(password)) {
                    HashSet<String> role = new HashSet<>();
                    role.add(user.getType().toString());
                    final Set<String> roles = role;
                    emitter.next(AuthenticationResponse.success(username, roles));
                } else {
                    emitter.error(AuthenticationResponse.exception(
                            "Invalid password for user [" + username + "]"
                    ));
                }
            } catch (NoSuchElementException e) {
                throw new UserNotFoundException("User not found: " + username);
            }
            emitter.complete();
        }, FluxSink.OverflowStrategy.ERROR);
    }
}
