package com.annika.repository;

import com.annika.entity.Product;
import com.annika.entity.User;
import com.annika.entity.UserProduct;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;

@Repository
public interface UserProductRepository extends CrudRepository<UserProduct, Long> {
    List<UserProduct> findByUser(User user);

    void deleteByUser(User user);

    List<UserProduct> findByProduct(Product product);

    UserProduct findByUserAndProduct(User user, Product product);

    void deleteByProduct(Product product);
}
