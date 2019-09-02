package com.corn.data.repository;

import com.corn.data.entity.ServiceStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServiceStatusRepo extends CrudRepository<ServiceStatus,Long> {
    List<ServiceStatus> findAllByCurrent(boolean current);

    // Null if not found
    @Query("select s.current from ServiceStatus s where s.id = :id")
    Boolean isStatusCurrent(@Param("id") long id);

    // Null if not found
    @Query("select s.statusType from ServiceStatus s where s.id = :id")
    String getStatusType(@Param("id") long id);
}
