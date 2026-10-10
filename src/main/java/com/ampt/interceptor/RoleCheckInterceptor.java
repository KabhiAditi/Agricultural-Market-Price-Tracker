package com.ampt.interceptor;

import com.ampt.controller.AuthController;
import com.ampt.entity.AppUser;
import com.ampt.entity.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class RoleCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        // Check if this is a price entry/edit operation
        boolean isEntryOrEdit = uri.endsWith("/prices/new")
                || (uri.startsWith("/prices/") && uri.endsWith("/edit"))
                || ("POST".equalsIgnoreCase(method) && uri.matches(".*/prices(/\\d+)?$"));

        if (!isEntryOrEdit) {
            return true;
        }

        HttpSession session = request.getSession(false);
        AppUser user = (session != null) ? (AppUser) session.getAttribute(AuthController.SESSION_USER_KEY) : null;

        if (user == null) {
            String redirectUrl = request.getRequestURI();
            if (request.getQueryString() != null) {
                redirectUrl += "?" + request.getQueryString();
            }
            response.sendRedirect(request.getContextPath() + "/login?redirect=" +
                    URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8));
            return false;
        }

        if (user.getRole() != Role.ENTRY_USER) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Access Denied: Only ENTRY_USER role is authorized to create or edit price records.");
            return false;
        }

        return true;
    }
}
