package com.projet2.user.repository

import com.projet2.user.model.Rate
import org.springframework.data.jpa.repository.JpaRepository

interface RateRepo : JpaRepository<Rate, Long> {

    fun findByGameId(gameId: String): List<Rate>
    fun findByGameIdAndUserIdNot(gameId: String, userId: Long): List<Rate>

}
