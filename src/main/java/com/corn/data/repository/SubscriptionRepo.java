package com.corn.data.repository;

import com.corn.data.entity.Subscription;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * @author Oleg Zaidullin
 */
public interface SubscriptionRepo extends CrudRepository<Subscription, String> {
    Subscription findTopByHash(String hash);
    Optional<Subscription> getByEmail(String email);
}
