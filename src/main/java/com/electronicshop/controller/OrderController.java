package com.electronicshop.controller;

import com.electronicshop.entity.Order;
import com.electronicshop.entity.OrderItem;
import com.electronicshop.entity.Product;
import com.electronicshop.entity.User;
import com.electronicshop.service.CartService;
import com.electronicshop.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;

    public OrderController(OrderService orderService, CartService cartService) {
        this.orderService = orderService;
        this.cartService = cartService;
    }

    @GetMapping("/my-orders")
    public String myOrders(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("LOGIN_USER");
        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("orders", orderService.getOrdersByUser(currentUser.getId()));
        model.addAttribute("cartCount", cartService.itemCount(session));
        model.addAttribute("currentUser", currentUser);
        return "my-orders";
    }

    @GetMapping("/my-orders/{id}")
    public String orderDetail(@PathVariable Long id, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("LOGIN_USER");
        if (currentUser == null) {
            return "redirect:/login";
        }

        Order order = orderService.getById(id).orElse(null);
        if (order == null || !order.getUserId().equals(currentUser.getId())) {
            return "redirect:/my-orders";
        }

        List<OrderItem> items = orderService.getItemsByOrderId(id);
        Map<Long, Product> productMap = orderService.getProductMap(items);

        model.addAttribute("order", order);
        model.addAttribute("items", items);
        model.addAttribute("productMap", productMap);
        model.addAttribute("cartCount", cartService.itemCount(session));
        model.addAttribute("currentUser", currentUser);
        return "order-detail";
    }
}
