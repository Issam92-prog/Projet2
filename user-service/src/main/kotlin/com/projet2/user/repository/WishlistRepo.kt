package com.projet2.user.repository

import com.projet2.user.model.WishlistItem
import com.projet2.user.model.Platform

import org.springframework.data.jpa.repository.JpaRepository

interface WishlistRepo : JpaRepository<WishlistItem, Long> {

    fun findByUserId(userId: Long): List<WishlistItem>

    fun existsByUserIdAndGameIdAndPlatform(
        userId: Long,
        gameId: String,
        platform: Platform
    ): Boolean
    fun deleteByUserIdAndGameIdAndPlatform(
        userId: Long,
        gameId: String,
        platform: Platform
    )

}

