package com.corn.data.repository;

import com.corn.data.entity.User;
import org.springframework.data.repository.CrudRepository;

public interface UsersRepo extends CrudRepository<User,Long> {
    User findByUsernameAndPassword(String username, String password);
}
