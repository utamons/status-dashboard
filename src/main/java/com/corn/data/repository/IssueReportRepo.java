package com.corn.data.repository;

import com.corn.data.entity.IssueReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Oleg Zaidullin
 */
public interface IssueReportRepo extends CrudRepository<IssueReport,Long> {
    Page<IssueReport> findAllByProcessed(boolean processed, Pageable pageable);
}
