package com.projet2.user.repository

import com.projet2.user.model.Rate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface RateRepo : JpaRepository<Rate, Long> {

    fun findByGameId(gameId: String): List<Rate>
    fun findByGameIdAndUserIdNot(gameId: String, userId: Long): List<Rate>
    fun findByUserId(userId: Long): List<Rate>
    fun existsByUserIdAndGameId(userId: Long, gameId: String): Boolean

}
