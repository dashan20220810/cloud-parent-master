package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.Notice;
import com.qianyi.casinocore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long>, JpaSpecificationExecutor<Notice> {
    @Query("from Notice n where n.isShelves=true order by n.createTime desc")
    List<Notice> newest();

    @Query("from Notice n order by n.updateTime desc")
    List<Notice> findByNoticeList();

    Notice findAllById(Long id);
}
