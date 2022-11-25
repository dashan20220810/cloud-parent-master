package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.CodeNumChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CodeNumChangeRepository extends JpaRepository<CodeNumChange,Long>, JpaSpecificationExecutor<CodeNumChange> {
}
