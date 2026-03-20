package com.electronicshop.controller;

import com.electronicshop.entity.Product;
import com.electronicshop.entity.User;
import com.electronicshop.service.CartService;
import com.electronicshop.service.OrderService;
import com.electronicshop.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class CartController {

    private final CartService cartService;
    private final ProductService productService;
    private final OrderService orderService;

    public CartController(CartService cartService, ProductService productService, OrderService orderService) {
        this.cartService = cartService;
        this.productService = productService;
        this.orderService = orderService;
    }

    @PostMapping("/cart/add/{id}")
    public String addToCart(@PathVariable Long id,
                            @RequestParam(value = "quantity", required = false) Integer quantity,
                            HttpSession session) {
        int qty = (quantity == null || quantity < 1) ? 1 : quantity;
        for (int i = 0; i < qty; i++) {
            cartService.addToCart(session, id);
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/buy-now/{id}")
    public String buyNow(@PathVariable Long id,
                         @RequestParam(value = "quantity", required = false) Integer quantity,
                         HttpSession session) {
        int qty = (quantity == null || quantity < 1) ? 1 : quantity;
        for (int i = 0; i < qty; i++) {
            cartService.addToCart(session, id);
        }
        return "redirect:/cart";
    }

    @GetMapping("/cart")
    public String cart(Model model, HttpSession session) {
        List<Long> ids = cartService.getCartMap(session).keySet().stream().collect(Collectors.toList());
        List<Product> products = productService.findAll().stream().filter(p -> ids.contains(p.getId())).collect(Collectors.toList());
        List<CartService.CartItem> items = cartService.getCartItems(session, products);
        BigDecimal total = cartService.calculateTotal(items);

        model.addAttribute("cartItems", items);
        model.addAttribute("total", total);
        model.addAttribute("cartCount", cartService.itemCount(session));
        model.addAttribute("currentUser", session.getAttribute("LOGIN_USER"));
        return "cart";
    }

    @PostMapping("/cart/update")
    public String update(@RequestParam Long productId, @RequestParam Integer quantity, HttpSession session) {
        cartService.updateQuantity(session, productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove/{id}")
    public String remove(@PathVariable Long id, HttpSession session) {
        cartService.removeItem(session, id);
        return "redirect:/cart";
    }

    @PostMapping("/cart/checkout")
    public String checkout(HttpSession session) {
        User currentUser = (User) session.getAttribute("LOGIN_USER");
        if (currentUser == null) {
            return "redirect:/login";
        }

        List<Long> ids = cartService.getCartMap(session).keySet().stream().collect(Collectors.toList());
        List<Product> products = productService.findAll().stream().filter(p -> ids.contains(p.getId())).collect(Collectors.toList());
        List<CartService.CartItem> items = cartService.getCartItems(session, products);
        if (items.isEmpty()) {
            return "redirect:/cart";
        }

        BigDecimal total = cartService.calculateTotal(items);
        orderService.checkout(currentUser.getId(), items, total);
        cartService.clear(session);
        return "redirect:/my-orders";
    }
}
