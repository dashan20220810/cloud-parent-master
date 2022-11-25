package com.qianyi.casinocore.repository;


import com.qianyi.casinocore.model.LunboPic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PictureRepository extends JpaRepository<LunboPic,Long> , JpaSpecificationExecutor<LunboPic> {
    LunboPic findAllById(Long id);

    LunboPic findByNo(Integer no);

//    @Query("from LunboPic l order by l.updateTime desc")
//    List<LunboPic> findByLunboPicList();

    List<LunboPic> findByTheShowEndAndUrlNotNull(Integer theShowEnd);
}
