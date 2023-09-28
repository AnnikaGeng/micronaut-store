## User-Products API

This is a simple API that allows users to create accounts and add products to their accounts. It also allows users to view all products added by other users.

## Authentication
- `/signup/` - POST - Creates a new user
- `/login/` - POST - Logs in a user

## Clients (authentication required)
- `/client/all/filter/?max=<int>&offset=<int>` - GET - Returns a list of all users, pagination enabled, admin only
- `/client/get/` - GET - Returns a client's information and its products
- `/client/update/` - PUT - Updates a client's information, not including products information
- `/client/delete/` - DELETE - Deletes a client and their relationship with products

## Client and Product Relationship (authentication required)
- `/client/products/filter{?max, offset}` - GET - Returns a list of all products related to the client, pagination enabled
- `/client/products/add/<int:product_id>` - POST - Add an existing product to the client's account
- `/client/products/delete/<int:product_id>` - DELETE - Removes a product this client has from their account

## Products (authentication required)
- `/products/filter{?max, offset}` - GET - Returns a list of all products, pagination enabled
- `/products/add/` - POST - Creates a new product, admin only
- `/products/<int:product_id>` - GET - Returns a product
- `/products/update/<int:product_id>` - PUT - Updates a product, admin only
- `/products/delete/<int:product_id>` - DELETE - Deletes a product, and all relationships with clients, admin only
- `/products/<int:product_id>/clients/` - GET - Returns a list of all clients that have this product, pagination enabled, admin only

