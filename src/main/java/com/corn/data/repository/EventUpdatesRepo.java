package com.corn.data.repository;

import com.corn.data.entity.EventUpdate;
import com.corn.data.entity.ServiceEvent;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Oleg Zaidullin
 */
public interface EventUpdatesRepo extends CrudRepository<EventUpdate,Long> {
    List<EventUpdate> findAllByEvent(ServiceEvent event);

    @Transactional
    @Modifying
    @Query("delete from EventUpdate s where s.id = :id")
    void deleteElem(@Param("id") long id);
}
