package com.corn.data.repository;

import com.corn.data.entity.Announcement;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @author Oleg Zaidullin
 */
public interface AnnouncementsRepo extends CrudRepository<Announcement,Long> {
    List<Announcement> findAllByActive(boolean active);
}
