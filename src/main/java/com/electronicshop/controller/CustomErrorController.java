package com.electronicshop.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, HttpServletResponse response, Model model) {
        // Get error status code
        Object status = request.getAttribute("javax.servlet.error.status_code");
        Object message = request.getAttribute("javax.servlet.error.message");
        Object originalUri = request.getAttribute("javax.servlet.error.request_uri");

        // Prevent confusing direct access to /error when no real error attributes exist
        if (status == null && "/error".equals(request.getRequestURI())) {
            return "redirect:/";
        }

        int statusCode;
        try {
            statusCode = status != null ? Integer.parseInt(status.toString()) : 500;
        } catch (NumberFormatException ex) {
            statusCode = 500;
        }
        response.setStatus(statusCode);

        String requestUri = originalUri != null ? originalUri.toString() : request.getRequestURI();

        model.addAttribute("status", statusCode);
        model.addAttribute("error", getErrorTitle(statusCode));
        model.addAttribute("message", message != null ? message.toString() : getErrorMessage(statusCode));
        model.addAttribute("requestUri", requestUri);

        // Return specific error page based on status code
        if (statusCode == 404) {
            return "error/404";
        } else if (statusCode == 500) {
            return "error/500";
        } else if (statusCode == 403) {
            return "error/error";
        } else if (statusCode == 401) {
            return "error/error";
        } else {
            return "error/error";
        }
    }

    private String getErrorTitle(int statusCode) {
        switch (statusCode) {
            case 400:
                return "Yêu Cầu Không Hợp Lệ";
            case 401:
                return "Chưa Đăng Nhập";
            case 403:
                return "Truy Cập Bị Từ Chối";
            case 404:
                return "Trang Không Tìm Thấy";
            case 500:
                return "Lỗi Máy Chủ Nội Bộ";
            case 503:
                return "Dịch Vụ Không Khả Dụng";
            default:
                return "Đã Xảy Ra Lỗi";
        }
    }

    private String getErrorMessage(int statusCode) {
        switch (statusCode) {
            case 400:
                return "Yêu cầu không hợp lệ. Vui lòng kiểm tra lại dữ liệu gửi đi.";
            case 401:
                return "Bạn cần đăng nhập để truy cập trang này.";
            case 403:
                return "Bạn không có quyền truy cập tương ứng vào trang này.";
            case 404:
                return "Trang bạn đang tìm kiếm không tồn tại hoặc đã bị xóa.";
            case 500:
                return "Có lỗi xảy ra trên máy chủ. Vui lòng thử lại sau.";
            case 503:
                return "Dịch vụ hiện không khả dụng. Vui lòng thử lại sau.";
            default:
                return "Đã xảy ra lỗi không mong muốn.";
        }
    }
}

