package com.corn.security.config;

import com.corn.security.filter.HeaderBasedAuthenticationFilter;
import com.corn.service.SessionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.AntPathMatcher;

/**
 * REST stateless security configuration. We don't store any state in the app, but we use Redis for keeping user sessions.
 * This makes the app more scalable and durable (user session is available from many instances of the app, and remains alive even if
 * all instances are temporarily down). All you need - just passing 'session-id' header in authenticated requests. Session object might
 * contain user role and so on..
 *
 * @author Oleg Zaidullin
 */
@Configuration
public class APISecurityConfig extends WebSecurityConfigurerAdapter {
    private final SessionService sessionService;
    private static final String[] PERMIT_ALL_GET = {
            "/favicon.ico", "/polyfills.js", "/runtime.js", "/styles.js", "/vendor.js", "/main.js", "/polyfills.js.map",
            "/runtime.js.map", "/styles.js.map", "/vendor.js.map", "/main.js.map", "/index.html", "/", "/home", "/history",
            "/report", "/subscribe", "/login", "/maintain", "/error", "/api/status", "/api/announcement", "/apple-touch-icon-precomposed.png",
            "/apple-touch-icon.png"

    };
    private static final String[] PERMIT_ALL_POST = {
            "/api/login", "/error", "/api/issueReport", "/api/subscription", "/api/confirm"
    };
    private static final String[] PERMIT_ALL_DELETE = {
            "/error",  "/api/subscription"
    };
    private static final String[] PERMIT_ALL_PUT = {
            "/error",
    };

    public APISecurityConfig(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf()
                .disable() // the app is stateless, and we don't use cookies, we use a request header instead. So we can disable csrf.
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, PERMIT_ALL_GET).permitAll()
                .antMatchers(HttpMethod.POST, PERMIT_ALL_POST).permitAll()
                .antMatchers(HttpMethod.PUT, PERMIT_ALL_PUT).permitAll()
                .antMatchers(HttpMethod.DELETE, PERMIT_ALL_DELETE).permitAll()
                .antMatchers(HttpMethod.OPTIONS).permitAll()
                .anyRequest().fullyAuthenticated()
                .and()
                .addFilterBefore(tokenBasedAuthenticationFilter(),
                        BasicAuthenticationFilter.class).sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .exceptionHandling()
                .authenticationEntryPoint(new Http403ForbiddenEntryPoint());
    }

    @Bean
    public HeaderBasedAuthenticationFilter tokenBasedAuthenticationFilter() {
        return new HeaderBasedAuthenticationFilter(request ->
                needAuthentication(request.getMethod(), request.getRequestURI()), sessionService
        );
    }

    private boolean needAuthentication(String method, String url) {
        switch (method) {
            case "GET":
                return doesNotContain(PERMIT_ALL_GET, url);
            case "POST":
                return doesNotContain(PERMIT_ALL_POST, url);
            case "PUT":
                return doesNotContain(PERMIT_ALL_PUT, url);
            case "DELETE":
                return doesNotContain(PERMIT_ALL_DELETE, url);
            case "OPTIONS":
                return false;
            default:
                return true;
        }
    }

    private boolean doesNotContain(String[] array, String value) {
        AntPathMatcher matcher = new AntPathMatcher();
        boolean result = true;
        for (String v : array) {
            if (matcher.match(v,value)) {
                result = false;
                break;
            }
        }
        return result;
    }
}
