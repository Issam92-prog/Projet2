package com.projet2.user.controller

import com.projet2.user.dto.*
import com.projet2.user.kafka.EventPublisher
import com.projet2.user.kafka.Topics
import com.projet2.user.mapper.BuyToGamePurchasedMapper
import com.projet2.user.mapper.RateToGameReviewMapper
import com.projet2.user.model.*
import com.projet2.user.repository.*
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(
    private val userRepo: UserRepo,
    private val buyRepo: BuyRepo,
    private val rateRepo: RateRepo,
    private val reviewVoteRepo: ReviewVoteRepo,
    private val wishlistRepo: WishlistRepo,
    private val dlcRepo: DlcRepo,
    private val dlcPurchaseRepo: DlcPurchaseRepo,
    private val eventPublisher: EventPublisher
) {

    /* ===================== USER ===================== */

    @PostMapping
    fun createUser(@RequestBody req: CreateUserRequest): User {
        val user = User(
            pseudo = req.pseudo,
            firstName = req.firstName,
            lastName = req.lastName,
            birthDate = req.birthDate
        )
        return userRepo.save(user)
    }

    /* ===================== BUY GAME ===================== */

    @PostMapping("/{userId}/buy")
    fun buyGame(
        @PathVariable userId: Long,
        @RequestBody req: BuyRequest
    ): Buy {

        userRepo.findById(userId)
            .orElseThrow { RuntimeException("User not found") }

        val buy = Buy(
            userId = userId,
            gameId = req.gameId,
            gameName = req.gameName,
            platform = req.platform,
            price = req.price
        )

        val savedBuy = buyRepo.save(buy)

        // 🔔 Kafka event
        val event = BuyToGamePurchasedMapper.map(savedBuy)
        eventPublisher.publish(
            Topics.GAME_PURCHASED,
            userId.toString(),
            event
        )

        return savedBuy
    }

    @GetMapping("/{userId}/library")
    fun getLibrary(@PathVariable userId: Long): List<Buy> {
        return buyRepo.findAllByUserId(userId)
    }

    @PutMapping("/{userId}/games/{gameId}/playtime")
    fun updatePlayTime(
        @PathVariable userId: Long,
        @PathVariable gameId: String,
        @RequestBody req: UpdatePlayTimeRequest
    ): Buy {

        val buy = buyRepo.findByUserIdAndGameId(userId, gameId)
            ?: throw RuntimeException("Game not owned")

        return buyRepo.save(
            buy.copy(playTimeHours = req.hours)
        )
    }

    /* ===================== RATE ===================== */

    @PostMapping("/{userId}/rate")
    fun rateGame(
        @PathVariable userId: Long,
        @RequestBody req: RateRequest
    ): Rate {

        if (!buyRepo.existsByUserIdAndGameId(userId, req.gameId)) {
            throw RuntimeException("User does not own this game")
        }

        val rate = Rate(
            userId = userId,
            gameId = req.gameId,
            gameName = req.gameName,
            note = req.note,
            comment = req.comment
        )

        val savedRate = rateRepo.save(rate)

        // 🔔 Kafka event
        val event = RateToGameReviewMapper.map(savedRate)
        eventPublisher.publish(
            Topics.GAME_REVIEWED,
            userId.toString(),
            event
        )

        return savedRate
    }

    @PostMapping("/rates/{rateId}/vote")
    fun voteReview(
        @PathVariable rateId: Long,
        @RequestParam userId: Long,
        @RequestBody req: VoteReviewRequest
    ): ReviewVote {

        val rate = rateRepo.findById(rateId)
            .orElseThrow { RuntimeException("Rate not found") }

        if (rate.userId == userId) {
            throw RuntimeException("Cannot vote on your own review")
        }

        if (reviewVoteRepo.existsByUserIdAndRateId(userId, rateId)) {
            throw RuntimeException("Already voted")
        }

        return reviewVoteRepo.save(
            ReviewVote(
                userId = userId,
                rateId = rateId,
                useful = req.useful
            )
        )
    }

    /* ===================== WISHLIST ===================== */

    @PostMapping("/{userId}/wishlist")
    fun addToWishlist(
        @PathVariable userId: Long,
        @RequestBody req: WishlistRequest
    ): WishlistItem {

        if (wishlistRepo.existsByUserIdAndGameIdAndPlatform(userId, req.gameId, req.platform)) {
            throw RuntimeException("Already in wishlist")
        }

        return wishlistRepo.save(
            WishlistItem(
                userId = userId,
                gameId = req.gameId,
                gameName = req.gameName,
                platform = req.platform
            )
        )
    }

    @DeleteMapping("/{userId}/wishlist/{gameId}")
    fun removeFromWishlist(
        @PathVariable userId: Long,
        @PathVariable gameId: String,
        @RequestParam platform: Platform
    ) {
        wishlistRepo.deleteByUserIdAndGameIdAndPlatform(userId, gameId, platform)
    }

    @GetMapping("/{userId}/wishlist")
    fun getWishlist(@PathVariable userId: Long): List<WishlistItem> {
        return wishlistRepo.findByUserId(userId)
    }

    /* ===================== DLC ===================== */

    @PostMapping("/{userId}/dlc/buy")
    fun buyDlc(
        @PathVariable userId: Long,
        @RequestBody req: BuyDlcRequest
    ): DlcPurchase {

        val dlc = dlcRepo.findById(req.dlcId)
            .orElseThrow { RuntimeException("DLC not found") }

        if (!buyRepo.existsByUserIdAndGameId(userId, dlc.gameId)) {
            throw RuntimeException("Base game not owned")
        }

        if (dlcPurchaseRepo.existsByUserIdAndDlcId(userId, dlc.id)) {
            throw RuntimeException("DLC already purchased")
        }

        return dlcPurchaseRepo.save(
            DlcPurchase(
                userId = userId,
                dlcId = dlc.id,
                gameId = dlc.gameId
            )
        )
    }

    /* ===================== FEED ===================== */

    @GetMapping("/{userId}/feed")
    fun getFeed(@PathVariable userId: Long): List<FeedItem> {

        val feed = mutableListOf<FeedItem>()
        val ownedGames = buyRepo.findByUserId(userId)

        ownedGames.forEach { buy ->
            val reviews = rateRepo.findByGameIdAndUserIdNot(buy.gameId, userId)
            reviews.forEach {
                feed.add(
                    FeedItem(
                        type = FeedType.NEW_REVIEW,
                        gameId = it.gameId,
                        gameName = it.gameName,
                        message = "New review: ${it.note}/5",
                        createdAt = it.ratedAt
                    )
                )
            }
        }

        return feed.sortedByDescending { it.createdAt }
    }
}
