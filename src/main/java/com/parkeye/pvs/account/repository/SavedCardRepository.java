package com.parkeye.pvs.account.repository;

import com.parkeye.pvs.account.entity.SavedCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavedCardRepository extends JpaRepository<SavedCardEntity, Long> {
    List<SavedCardEntity> findByUserIdOrderByIdAsc(Long userId);
}
