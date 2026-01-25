package com.projet2.user.repository

import com.projet2.user.model.Buy
import org.springframework.data.jpa.repository.JpaRepository

interface BuyRepo : JpaRepository<Buy, Long> {

    fun findByUserId(userId: Long): List<Buy>
    fun findAllByUserId(userId: Long): List<Buy>
    fun existsByUserIdAndGameId(userId: Long, gameId: String): Boolean



}
