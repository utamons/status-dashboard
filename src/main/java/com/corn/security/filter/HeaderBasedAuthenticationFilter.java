package com.corn.security.filter;

import com.corn.data.dto.SessionDTO;
import com.corn.data.dto.UserDTO;
import com.corn.util.Constants;
import com.corn.security.handler.HeaderBasedAuthenticationSuccessHandlerImpl;
import com.corn.security.service.NoOpAuthenticationManager;
import com.corn.service.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.corn.util.Constants.FINISH;
import static com.corn.util.Constants.START;

/**
 * The filter gets user sessions and allows/denies authentication
 *
 * @author Oleg Zaidullin
 */
public class HeaderBasedAuthenticationFilter extends
        AbstractAuthenticationProcessingFilter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String         TOKEN_FILTER_APPLIED = "TOKEN_FILTER_APPLIED";
    private final SessionService sessionService;

    public HeaderBasedAuthenticationFilter(RequestMatcher matcher,
                                           SessionService sessionService) {
        super(matcher);
        super.setAuthenticationManager(new NoOpAuthenticationManager());
        setAuthenticationSuccessHandler(new HeaderBasedAuthenticationSuccessHandlerImpl());
        this.sessionService = sessionService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse arg1) {
        logger.debug("Trying to authenticate {}", request.getRequestURI());

        request.setAttribute(TOKEN_FILTER_APPLIED, Boolean.TRUE);

        String token = request.getHeader(Constants.SESSION_ID);
        AbstractAuthenticationToken userAuthenticationToken = authenticateByToken(token);

        logger.debug("FINISH (Successful)");
        return userAuthenticationToken;
    }

    /**
     * authenticate the user based on token
     *
     * @return token
     */
    private AbstractAuthenticationToken authenticateByToken(String token) {
        logger.debug("START, token={}", token);
        if (null == token) {
            logger.debug("FINISH (null token)");
            throw new AuthenticationServiceException("Bad Token");
        }

        AbstractAuthenticationToken authToken;

        UserDTO securityUser;

        SessionDTO session = sessionService.getSession(token);

        if (session != null) {
            securityUser = session.getUser();
            authToken = new UsernamePasswordAuthenticationToken(
                    securityUser.getUsername(), null, getAuthorities(securityUser));
        } else {
            logger.debug("FINISH Not found {}", token);
            throw new AuthenticationServiceException("Bad Token");
        }

        logger.debug(FINISH);
        return authToken;
    }


    @SuppressWarnings("SameReturnValue")
    private Collection<? extends GrantedAuthority> getAuthorities(UserDTO user) {
        List<GrantedAuthority> result = new ArrayList<>();
        result.add((GrantedAuthority) user::getRole);
        return result;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain filterChain) throws IOException, ServletException {
        logger.debug(START);
        HttpServletRequest  request  = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (request.getAttribute(TOKEN_FILTER_APPLIED) != null) {
            logger.debug("FINISH (applied to {})", request.getRequestURI());
            filterChain.doFilter(request, response);
        } else {
            logger.debug("FINISH (not applied to {})", request.getRequestURI());
            super.doFilter(req, res, filterChain);
        }
    }

}
