package com.ecom.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.ecom.model.Cart;
import com.ecom.model.Product;
import com.ecom.model.UserDtls;
import com.ecom.repository.CartRepository;
import com.ecom.repository.ProductRepository;
import com.ecom.repository.UserRepository;
import com.ecom.service.CartService;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Cart saveCart(Integer productId, Integer userId) {
        UserDtls userDtls = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found!"));

        Cart cartStatus = cartRepository.findByProductIdAndUserId(productId, userId);

        if (ObjectUtils.isEmpty(cartStatus)) {
            cartStatus = new Cart();
            cartStatus.setProduct(product);
            cartStatus.setUser(userDtls);
            cartStatus.setQuantity(1);
            cartStatus.setTotalPrice(1 * product.getDiscountPrice());
        } else {
            cartStatus.setQuantity(cartStatus.getQuantity() + 1);
            cartStatus.setTotalPrice(cartStatus.getQuantity() * cartStatus.getProduct().getDiscountPrice());
        }
        return cartRepository.save(cartStatus);
    }

    @Override
    public List<Cart> getCartsByUser(Integer userId) {
        List<Cart> carts = cartRepository.findByUserId(userId);

        Double totalOrderPrice = carts.stream()
            .mapToDouble(c -> c.getProduct().getDiscountPrice() * c.getQuantity())
            .sum();

        carts.forEach(c -> c.setTotalOrderPrice(totalOrderPrice));

        return carts;
    }

    @Override
    public Integer getCountCart(Integer userId) {
        return cartRepository.countByUserId(userId);
    }

    @Override
    public void updateQuantity(String sy, Integer cid) {
        Cart cart = cartRepository.findById(cid)
                .orElseThrow(() -> new RuntimeException("Cart item not found!"));

        int updateQuantity = "de".equalsIgnoreCase(sy) ? cart.getQuantity() - 1 : cart.getQuantity() + 1;

        if (updateQuantity <= 0) {
            cartRepository.delete(cart);
        } else {
            cart.setQuantity(updateQuantity);
            cartRepository.save(cart);
        }
    }
}
