package com.projet2.user.repository

import com.projet2.user.model.DlcPurchase
import org.springframework.data.jpa.repository.JpaRepository

interface DlcPurchaseRepo : JpaRepository<DlcPurchase, Long> {

    fun existsByUserIdAndDlcId(userId: Long, dlcId: String): Boolean
}
