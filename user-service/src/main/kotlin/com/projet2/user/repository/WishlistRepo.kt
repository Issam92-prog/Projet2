package com.projet2.user.repository

import com.projet2.user.model.WishlistItem

import org.springframework.data.jpa.repository.JpaRepository

interface WishlistRepo : JpaRepository<WishlistItem, Long> {

    fun findByUserId(userId: Long): List<WishlistItem>

    fun existsByUserIdAndGameIdAndPlatform(
        userId: Long,
        gameId: String,
        platform: String
    ): Boolean
    fun deleteByUserIdAndGameIdAndPlatform(
        userId: Long,
        gameId: String,
        platform: String
    )

}

