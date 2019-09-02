package com.corn.data.repository;

import com.corn.data.entity.ServiceComponent;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Oleg Zaidullin
 */
public interface ServiceComponentsRepo extends CrudRepository<ServiceComponent,Long> {
}
