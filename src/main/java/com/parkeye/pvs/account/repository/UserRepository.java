package com.parkeye.pvs.account.repository;

import com.parkeye.pvs.account.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {}
