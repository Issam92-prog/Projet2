package com.projet2.user.repository

import com.projet2.user.model.ReviewVote
import org.springframework.data.jpa.repository.JpaRepository

interface ReviewVoteRepo : JpaRepository<ReviewVote, Long> {

    fun existsByUserIdAndRateId(userId: Long, rateId: Long): Boolean
}
