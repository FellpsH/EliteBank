package com.fellps.apibank.repository;

import com.fellps.apibank.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findByEntityNameAndEntityId(String entityName, Long entityId, Pageable pageable);
    Page<AuditLog> findByPerformedById(Long performedById, Pageable pageable);
}

