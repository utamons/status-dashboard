package com.corn.service;

import com.corn.data.dto.SessionDTO;
import com.corn.data.entity.Session;
import com.corn.data.entity.User;
import com.corn.data.repository.SessionsRepo;
import com.corn.data.repository.UsersRepo;
import com.corn.data.dto.UserDTO;
import com.corn.exception.cornNonAuthenticatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.token.Sha512DigestUtils;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

import static com.corn.util.Constants.FINISH;
import static com.corn.util.Constants.START;
import static java.time.temporal.ChronoUnit.MINUTES;

/**
 * @author Oleg Zaidullin
 */
@Service
public class SessionService {
    private static final String ROLE_MAINTAINER = "maintainer";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    public static final int MAX_AGE = 24 * 60; // Max session age in minutes

    private final SessionsRepo sessionsRepo;
    private final UsersRepo usersRepo;

    public SessionService(SessionsRepo sessionsRepo, UsersRepo usersRepo) {
        this.sessionsRepo = sessionsRepo;
        this.usersRepo = usersRepo;
    }

    public SessionDTO login(UserDTO incoming) {
        logger.debug(START);
        Validator validator = new Validator();
        String username = incoming.getUsername();
        String password = incoming.getPassword();

        validator.notEmpty(username, "username")
                .notEmpty(password, "password")
                .validate();

        if (username.length()>512 || password.length()>512) // just in case
            throw new cornNonAuthenticatedException();

        String hash = Sha512DigestUtils.shaHex(password);
        User user = usersRepo.findByUsernameAndPassword(username, hash);
        if (user == null || !user.isActive() || !user.getRole().equals(ROLE_MAINTAINER))
            throw new cornNonAuthenticatedException();

        Instant expired = Clock.systemDefaultZone().instant().plus(MAX_AGE, MINUTES);
        Session session = new Session(
                user,
                Clock.systemDefaultZone().instant(),
                expired,
                Sha512DigestUtils.shaHex(username + expired + hash)
        );

        sessionsRepo.save(session);

        logger.debug(FINISH);
        return session.toValue();
    }

    public SessionDTO getSession(String token) {
        Session session = sessionsRepo.findByToken(token);
        if (session == null)
            return null;
        else
            return session.toValue();
    }

    @Scheduled(fixedRate = 60000)
    private void cleanExpired() {
        sessionsRepo.deleteExpired(Clock.systemDefaultZone().instant());
    }

    String currentUser(String token) {
        Session session = sessionsRepo.findByToken(token);
        if (session == null)
            return null;
        else
            return session.getUser().getUsername();
    }

    public void logout(String sessionId) {
        Session session = sessionsRepo.findByToken(sessionId);
        if (session != null)
            sessionsRepo.delete(session);
    }
}