package com.projet2.user.repository

import com.projet2.user.model.ReviewVote
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ReviewVoteRepo : JpaRepository<ReviewVote, Long> {

    fun existsByUserIdAndRateId(userId: Long, rateId: Long): Boolean
    fun countByRateIdAndUsefulTrue(rateId: Long): Long
    fun countByRateIdAndUsefulFalse(rateId: Long): Long
}
