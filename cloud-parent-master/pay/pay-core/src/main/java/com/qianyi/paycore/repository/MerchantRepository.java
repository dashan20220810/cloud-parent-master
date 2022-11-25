package com.qianyi.paycore.repository;

import com.qianyi.paycore.model.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantRepository extends JpaRepository<Merchant,Long> {
}
