package com.saka.qms.repository;

import com.saka.qms.model.Detail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DetailRepository extends JpaRepository<Detail, Integer> {
    @Query("""
            select detail
            from Detail detail
            where detail.relatedItemId = :relatedItemId
              and (detail.isDeleted = false or detail.isDeleted is null)
            order by detail.sequence asc, detail.id asc
            """)
    List<Detail> findByRelatedItemIdAndIsDeletedFalseOrderBySequenceAscIdAsc(
            @Param("relatedItemId") Integer relatedItemId);
}
