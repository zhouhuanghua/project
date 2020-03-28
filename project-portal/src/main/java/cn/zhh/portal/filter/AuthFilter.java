package cn.zhh.portal.filter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Objects;

@WebFilter(filterName="authFilter" , urlPatterns = "/*")
public class AuthFilter extends HttpFilter {

    private final String token_prefix = "thunder";

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (validate(request, response)) {
            chain.doFilter(request, response);
        } else {
            response.sendRedirect("/portal/auth-error.html");
        }
    }

    private boolean validate(HttpServletRequest request, HttpServletResponse response) {
        if (request.getRequestURI().endsWith("auth-error.html")) {
            return true;
        }
        String token = getToken();
        if (Objects.equals(request.getParameter("token"), token)) {
            response.addCookie(new Cookie("token", token));
            return true;
        }
        Cookie[] cookies = request.getCookies();
        if (Objects.isNull(cookies)) {
            return false;
        }
        return Arrays.stream(cookies).anyMatch(c -> Objects.equals(c.getName(), "token") && Objects.equals(c.getValue(), token));
    }

    private String getToken() {
        return token_prefix + LocalTime.now().getHour();
    }
}