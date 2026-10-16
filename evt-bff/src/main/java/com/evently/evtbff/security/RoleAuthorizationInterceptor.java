
package com.evently.evtbff.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Map;

@Component
public class RoleAuthorizationInterceptor
        implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws IOException {

        if (!(handler instanceof HandlerMethod method)) {
            return true;
        }

        RoleRequired roleRequired =
                method.getMethodAnnotation(RoleRequired.class);

        if (roleRequired == null) {
            roleRequired = method.getBeanType()
                    .getAnnotation(RoleRequired.class);
        }

        // No role restriction on this endpoint
        if (roleRequired == null) {
            return true;
        }

        Object roleAttribute = request.getAttribute("role");

        if (roleAttribute == null ||
                !roleRequired.value().equals(roleAttribute.toString())) {

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Forbidden: insufficient role\"}"
            );

            return false;
        }

        return true;
    }
}
