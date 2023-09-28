package com.annika.entity;

import com.annika.repository.ProductRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ProductMapper {

    @Inject
    private ProductRepository productRepository;
    public ProductDTO toDto(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setProduct_name(product.getProduct_name());
        dto.setPrice(product.getPrice());
        if (product.getDescription() != null) {
            dto.setDescription(product.getDescription());
        }
        return dto;
    }

    public Product toEntity(ProductDTO dto) {
        if (dto.getId() != null) {
            Product product = productRepository.findById(dto.getId()).orElse(null);
            if (product != null) {
                product.setProduct_name(dto.getProduct_name());
                product.setDescription(dto.getDescription());
                product.setPrice(dto.getPrice());
                return product;
            } else {
                return new Product(dto.getProduct_name(), dto.getDescription(), dto.getPrice());
            }
        } else {
            return new Product(dto.getProduct_name(), dto.getDescription(), dto.getPrice());
        }
    }
}
