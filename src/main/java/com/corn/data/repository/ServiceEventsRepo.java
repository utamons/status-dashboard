package com.corn.data.repository;

import com.corn.data.entity.ServiceEvent;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @author Oleg Zaidullin
 */
public interface ServiceEventsRepo extends CrudRepository<ServiceEvent,Long> {
    List<ServiceEvent> findTop10ByResolvedOrderByEventDateDesc(boolean resolved);
}
