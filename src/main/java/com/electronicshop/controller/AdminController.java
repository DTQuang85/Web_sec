package com.electronicshop.controller;

import com.electronicshop.entity.Order;
import com.electronicshop.entity.OrderItem;
import com.electronicshop.entity.Product;
import com.electronicshop.entity.User;
import com.electronicshop.service.OrderService;
import com.electronicshop.service.ProductService;
import com.electronicshop.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
public class AdminController {

    private static final String ROLE_USER = "USER";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";

    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;

    public AdminController(UserService userService,
                           ProductService productService,
                           OrderService orderService) {
        this.userService = userService;
        this.productService = productService;
        this.orderService = orderService;
    }

    @GetMapping("/admin")
    public String adminRoot(HttpSession session) {
        if (!hasBackofficeAccess(session)) {
            return "redirect:/login";
        }
        if (isSuperAdmin(session)) {
            return "redirect:/admin/root";
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/root")
    public String superAdminControl(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "") String keyword,
                                    Model model,
                                    HttpSession session) {
        if (!isSuperAdmin(session)) {
            return "redirect:/admin/dashboard";
        }

        Pageable pageable = PageRequest.of(page, 8);
        Page<User> users = userService.getUsers(keyword, pageable);
        model.addAttribute("users", users);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentUser", session.getAttribute("LOGIN_USER"));
        return "admin/root-control";
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model, HttpSession session) {
        if (!hasBackofficeAccess(session)) {
            return "redirect:/login";
        }

        Map<String, Object> data = orderService.dashboardData();
        model.addAttribute("totalUsers", userService.countUsers());
        model.addAttribute("totalOrders", orderService.countOrders());
        model.addAttribute("totalProducts", productService.countProducts());
        model.addAttribute("totalRevenue", orderService.totalRevenue());
        model.addAttribute("chartLabels", data.get("chartLabels"));
        model.addAttribute("chartValues", data.get("chartValues"));
        model.addAttribute("topProducts", data.get("topProducts"));
        model.addAttribute("currentUser", session.getAttribute("LOGIN_USER"));
        return "admin/dashboard";
    }

    @GetMapping("/admin/users")
    public String users(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "") String keyword,
                        Model model,
                        HttpSession session) {
        if (!hasBackofficeAccess(session)) {
            return "redirect:/login";
        }

        Pageable pageable = PageRequest.of(page, 8);
        Page<User> users = userService.getUsers(keyword, pageable);
        model.addAttribute("users", users);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentUser", session.getAttribute("LOGIN_USER"));
        return "admin/users";
    }

    @PostMapping("/admin/users/save")
    public String saveUser(@RequestParam(required = false) Long id,
                           @RequestParam(value = "username", required = false) String username,
                           @RequestParam(value = "password", required = false) String password,
                           @RequestParam(value = "role", required = false) String role,
                           HttpSession session) {
        if (!hasBackofficeAccess(session)) {
            return "redirect:/login";
        }

        boolean superAdmin = isSuperAdmin(session);
        User user = id == null ? new User() : userService.findById(id).orElse(new User());
        String existingRole = normalizeRole(user.getRole());
        String requestedRole = normalizeRole(role);

        if (!superAdmin && ROLE_SUPER_ADMIN.equals(existingRole)) {
            return "redirect:/admin/users";
        }

        if (!superAdmin && ROLE_SUPER_ADMIN.equals(requestedRole)) {
            requestedRole = ROLE_ADMIN;
        }

        String normalizedUsername = username != null ? username.trim() : "";
        if (normalizedUsername.isBlank()) {
            return "redirect:/admin/users";
        }

        user.setUsername(normalizedUsername);
        if (password != null && !password.isBlank()) {
            user.setPassword(password);
        }
        user.setRole(requestedRole);
        userService.save(user);
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, HttpSession session) {
        if (!hasBackofficeAccess(session)) {
            return "redirect:/login";
        }

        User currentUser = (User) session.getAttribute("LOGIN_USER");
        if (currentUser != null && currentUser.getId() != null && currentUser.getId().equals(id)) {
            return "redirect:/admin/users";
        }

        User target = userService.findById(id).orElse(null);
        if (target != null && ROLE_SUPER_ADMIN.equals(normalizeRole(target.getRole())) && !isSuperAdmin(session)) {
            return "redirect:/admin/users";
        }

        userService.delete(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/admin/products")
    public String products(Model model, HttpSession session) {
        if (!hasBackofficeAccess(session)) {
            return "redirect:/login";
        }

        model.addAttribute("products", productService.findAll());
        model.addAttribute("currentUser", session.getAttribute("LOGIN_USER"));
        return "admin/products";
    }

    @PostMapping("/admin/products/save")
    public String saveProduct(@RequestParam(required = false) Long id,
                              @RequestParam String name,
                              @RequestParam String description,
                              @RequestParam BigDecimal price,
                              @RequestParam(required = false) String imageUrl,
                              HttpSession session) {
        if (!hasBackofficeAccess(session)) {
            return "redirect:/login";
        }

        Product product = id == null ? new Product() : productService.findById(id).orElse(new Product());
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);

        if (imageUrl != null && !imageUrl.isBlank()) {
            product.setImage(imageUrl.trim());
        }

        productService.save(product);
        return "redirect:/admin/products";
    }

    @PostMapping("/admin/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, HttpSession session) {
        if (!hasBackofficeAccess(session)) {
            return "redirect:/login";
        }

        productService.delete(id);
        return "redirect:/admin/products";
    }

    @GetMapping("/admin/orders")
    public String orders(Model model, HttpSession session) {
        if (!hasBackofficeAccess(session)) {
            return "redirect:/login";
        }

        model.addAttribute("orders", orderService.allOrders());
        model.addAttribute("currentUser", session.getAttribute("LOGIN_USER"));
        return "admin/orders";
    }

    @GetMapping("/admin/orders/{id}")
    public String orderDetail(@PathVariable Long id, Model model, HttpSession session) {
        if (!hasBackofficeAccess(session)) {
            return "redirect:/login";
        }

        Order order = orderService.getById(id).orElse(null);
        if (order == null) {
            return "redirect:/admin/orders";
        }

        List<OrderItem> items = orderService.getItemsByOrderId(id);
        Map<Long, Product> productMap = orderService.getProductMap(items);
        model.addAttribute("order", order);
        model.addAttribute("items", items);
        model.addAttribute("productMap", productMap);
        model.addAttribute("currentUser", session.getAttribute("LOGIN_USER"));
        return "admin/order-detail";
    }

    @PostMapping("/admin/orders/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam String status,
                               HttpSession session) {
        if (!hasBackofficeAccess(session)) {
            return "redirect:/login";
        }

        orderService.getById(id).ifPresent(order -> {
            order.setStatus(status);
            orderService.save(order);
        });
        return "redirect:/admin/orders/" + id;
    }

    private boolean hasBackofficeAccess(HttpSession session) {
        User user = (User) session.getAttribute("LOGIN_USER");
        if (user == null) {
            return false;
        }
        String role = normalizeRole(user.getRole());
        return ROLE_ADMIN.equals(role) || ROLE_SUPER_ADMIN.equals(role);
    }

    private boolean isSuperAdmin(HttpSession session) {
        User user = (User) session.getAttribute("LOGIN_USER");
        return user != null && ROLE_SUPER_ADMIN.equals(normalizeRole(user.getRole()));
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return ROLE_USER;
        }
        String normalized = role.trim().toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }

        if (ROLE_SUPER_ADMIN.equals(normalized)) {
            return ROLE_SUPER_ADMIN;
        }
        if (ROLE_ADMIN.equals(normalized)) {
            return ROLE_ADMIN;
        }
        return ROLE_USER;
    }

}
