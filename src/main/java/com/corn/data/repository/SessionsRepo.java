package com.corn.data.repository;

import com.corn.data.entity.Session;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

public interface SessionsRepo extends CrudRepository<Session,Long> {

    @Transactional
    @Modifying(flushAutomatically = true)
    @Query("delete from Session s where s.expiredAt <= :dt")
    void deleteExpired(@Param("dt") Instant dt);

    Session findByToken(String token);
}
