package com.annika.controller;

import com.annika.entity.*;
import com.annika.error.CustomError;
import com.annika.repository.ProductRepository;
import com.annika.repository.UserProductRepository;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

@Controller("/products")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class ProductController {

    @Inject
    private ProductRepository productRepository;

    @Inject
    private ProductMapper productMapper;

    @Inject
    private UserProductRepository userProductRepository;

    @Inject
    private UserMapper userMapper;

    @Get("{id}")
    public HttpResponse getProduct(@PathVariable Long id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return HttpResponse.badRequest().body(
                    new CustomError(
                            HttpStatus.BAD_REQUEST.getCode(),
                            "Product not found",
                            "Choose a new product"
                    )
            );
        }
        return HttpResponse.ok(productMapper.toDto(product));
    }

    @Post("/add")
    @Secured({"ROLE_ADMIN"})
    public HttpResponse addProduct(@Body @Valid ProductDTO productDTO) {
        Product product = productMapper.toEntity(productDTO);
        productRepository.save(product);
        return HttpResponse.ok(product);
    }

    @Get("/filter{?max, offset}")
    public List<ProductDTO> getAllProducts(@QueryValue Optional<Integer> max, @QueryValue Optional<Integer> offset) {
        List<Product> products = productRepository.findAll();
        List<ProductDTO> allProducts = products.stream()
                .map(productMapper::toDto)
                .skip(offset.orElse(0))
                .limit(max.orElse(10))
                .toList();
        return allProducts;
    }

    @Put("/update/{id}")
    @Secured({"ROLE_ADMIN"})
    public HttpResponse updateProduct(@PathVariable Long id, @Body @Valid ProductDTO productDTO) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return HttpResponse.badRequest("Product not found");
        }
        product.setProduct_name(productDTO.getProduct_name());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        productRepository.update(product);
        return HttpResponse.ok("Product updated");
    }

    @Delete("/delete/{id}")
    @Secured({"ROLE_ADMIN"})
    public HttpResponse deleteProduct(@PathVariable Long id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return HttpResponse.badRequest("Product not found");
        }
        userProductRepository.deleteByProduct(product);
        productRepository.delete(product);
        return HttpResponse.ok("Product deleted");
    }

    @Get("/{id}/clients")
    @Secured({"ROLE_ADMIN"})
    public HttpResponse getClients(@PathVariable Long id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return HttpResponse.badRequest("Product not found");
        }
        List<UserProduct> userProducts = userProductRepository.findByProduct(product);
        List<UserDTO> userDTOS = userProducts.stream()
                .map(userProduct -> userMapper.toDTO(userProduct.getUser()))
                .toList();
        return HttpResponse.ok(userDTOS);
    }
}
