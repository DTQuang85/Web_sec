package com.electronicshop.service;

import com.electronicshop.entity.Product;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartService {

    public static final String CART_SESSION_KEY = "CART";

    public static class CartItem {
        private Product product;
        private int quantity;

        public CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public Product getProduct() {
            return product;
        }

        public int getQuantity() {
            return quantity;
        }

        public BigDecimal getSubtotal() {
            return product.getPrice().multiply(BigDecimal.valueOf(quantity));
        }
    }

    @SuppressWarnings("unchecked")
    public Map<Long, Integer> getCartMap(HttpSession session) {
        Object cartObj = session.getAttribute(CART_SESSION_KEY);
        if (cartObj == null) {
            Map<Long, Integer> newCart = new LinkedHashMap<>();
            session.setAttribute(CART_SESSION_KEY, newCart);
            return newCart;
        }
        return (Map<Long, Integer>) cartObj;
    }

    public void addToCart(HttpSession session, Long productId) {
        Map<Long, Integer> cart = getCartMap(session);
        cart.put(productId, cart.getOrDefault(productId, 0) + 1);
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    public void updateQuantity(HttpSession session, Long productId, int quantity) {
        Map<Long, Integer> cart = getCartMap(session);
        if (quantity <= 0) {
            cart.remove(productId);
        } else {
            cart.put(productId, quantity);
        }
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    public void removeItem(HttpSession session, Long productId) {
        Map<Long, Integer> cart = getCartMap(session);
        cart.remove(productId);
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    public void clear(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }

    public List<CartItem> getCartItems(HttpSession session, List<Product> products) {
        Map<Long, Integer> cart = getCartMap(session);
        List<CartItem> items = new ArrayList<>();
        for (Product product : products) {
            Integer quantity = cart.get(product.getId());
            if (quantity != null && quantity > 0) {
                items.add(new CartItem(product, quantity));
            }
        }
        return items;
    }

    public BigDecimal calculateTotal(List<CartItem> items) {
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int itemCount(HttpSession session) {
        return getCartMap(session).values().stream().mapToInt(Integer::intValue).sum();
    }
}
