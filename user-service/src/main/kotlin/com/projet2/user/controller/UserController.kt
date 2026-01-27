package com.projet2.user.controller

import com.projet2.user.dto.BuyDlcRequest
import com.projet2.user.dto.BuyRequest
import com.projet2.user.dto.CreateUserRequest
import com.projet2.user.dto.FeedItem
import com.projet2.user.dto.FeedType
import com.projet2.user.dto.RateRequest
import com.projet2.user.dto.UpdatePlayTimeRequest
import com.projet2.user.dto.VoteReviewRequest
import com.projet2.user.dto.WishlistRequest
import com.projet2.user.model.Buy
import com.projet2.user.model.DlcPurchase
import com.projet2.user.model.Platform
import com.projet2.user.model.Rate
import com.projet2.user.model.ReviewVote
import com.projet2.user.model.User
import com.projet2.user.model.WishlistItem
import com.projet2.user.repository.BuyRepo
import com.projet2.user.repository.DlcPurchaseRepo
import com.projet2.user.repository.DlcRepo
import com.projet2.user.repository.RateRepo
import com.projet2.user.repository.ReviewVoteRepo
import com.projet2.user.repository.UserRepo
import com.projet2.user.repository.WishlistRepo
import java.time.Instant
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(
    private val userRepo: UserRepo,
    private val reviewVoteRepo: ReviewVoteRepo,
    private val buyRepo: BuyRepo,
    private val rateRepo: RateRepo,
    private val dlcRepo: DlcRepo,
    private val dlcPurchaseRepo: DlcPurchaseRepo,

    private val wishlistRepo: WishlistRepo
) {

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
    @PostMapping("/{userId}/buy")
    fun buyGame(
        @PathVariable userId: Long,
        @RequestBody req: BuyRequest
    ): Buy {
        val user = userRepo.findById(userId)
            .orElseThrow { RuntimeException("User not found") }

        val buy = Buy(
            userId = user.id,
            gameId = req.gameId,
            gameName = req.gameName,
            platform = req.platform
        )


        return buyRepo.save(buy)
    }
    @GetMapping("/{userId}/library")
    fun getLibrary(@PathVariable userId: Long): List<Buy> {
        userRepo.findById(userId)
            .orElseThrow { RuntimeException("User not found") }

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

        val updatedBuy = buy.copy(
            playTimeHours = req.hours
        )

        return buyRepo.save(updatedBuy)
    }




    @PostMapping("/{userId}/rate")
    fun rateGame(
        @PathVariable userId: Long,
        @RequestBody req: RateRequest
    ): Rate {

        userRepo.findById(userId)
            .orElseThrow { RuntimeException("User not found") }

        val ownsGame = buyRepo.existsByUserIdAndGameId(userId, req.gameId)
        if (!ownsGame) {
            throw RuntimeException("User does not own this game")
        }

        val rate = Rate(
            userId = userId,
            gameId = req.gameId,
            gameName = req.gameName,
            note = req.note,
            comment = req.comment
        )


        return rateRepo.save(rate)
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

        val alreadyVoted = reviewVoteRepo.existsByUserIdAndRateId(userId, rateId)
        if (alreadyVoted) {
            throw RuntimeException("User already voted on this review")
        }

        val vote = ReviewVote(
            userId = userId,
            rateId = rateId,
            useful = req.useful
        )

        return reviewVoteRepo.save(vote)
    }

//ajouter à la wishlist
@PostMapping("/{userId}/wishlist")
fun addToWishlist(
    @PathVariable userId: Long,
    @RequestBody req: WishlistRequest
): WishlistItem {
    userRepo.findById(userId)
        .orElseThrow { RuntimeException("User not found") }

    val exists = wishlistRepo.existsByUserIdAndGameIdAndPlatform(
        userId,
        req.gameId,
        req.platform
    )

    if (exists) {
        throw RuntimeException("Game already in wishlist for this platform")
    }

    val item = WishlistItem(
        userId = userId,
        gameId = req.gameId,
        gameName = req.gameName,
        platform = req.platform
    )

    return wishlistRepo.save(item)
}
    //supr un jeu de la wishlist
    @DeleteMapping("/{userId}/wishlist/{gameId}")
    fun removeFromWishlist(
        @PathVariable userId: Long,
        @PathVariable gameId: String,
        @RequestParam platform: Platform
    ) {
        userRepo.findById(userId)
            .orElseThrow { RuntimeException("User not found") }

        val exists = wishlistRepo.existsByUserIdAndGameIdAndPlatform(
            userId,
            gameId,
            platform
        )

        if (!exists) {
            throw RuntimeException("Game not found in wishlist")
        }

        wishlistRepo.deleteByUserIdAndGameIdAndPlatform(
            userId,
            gameId,
            platform
        )
    }


    //consulter la wishlist
    @GetMapping("/{userId}/wishlist")
    fun getWishlist(@PathVariable userId: Long): List<WishlistItem> {
        return wishlistRepo.findByUserId(userId)
    }

    @PostMapping("/{userId}/dlc/buy")
    fun buyDlc(
        @PathVariable userId: Long,
        @RequestBody req: BuyDlcRequest
    ): DlcPurchase {

        // 1️⃣ Vérifier utilisateur
        userRepo.findById(userId)
            .orElseThrow { RuntimeException("User not found") }

        // 2️⃣ Vérifier que le DLC existe
        val dlc = dlcRepo.findById(req.dlcId)
            .orElseThrow { RuntimeException("DLC not found") }

        // 3️⃣ Vérifier que le jeu de base est possédé
        val ownsGame = buyRepo.existsByUserIdAndGameId(userId, dlc.gameId)
        if (!ownsGame) {
            throw RuntimeException("User does not own base game")
        }

        // 4️⃣ Vérifier que le DLC n’est pas déjà acheté
        if (dlcPurchaseRepo.existsByUserIdAndDlcId(userId, dlc.id)) {
            throw RuntimeException("DLC already purchased")
        }

        val purchase = DlcPurchase(
            userId = userId,
            dlcId = dlc.id,
            gameId = dlc.gameId
        )

        return dlcPurchaseRepo.save(purchase)
    }
//flux d'info crée dynamiquement
    @GetMapping("/{userId}/feed")
    fun getFeed(@PathVariable userId: Long): List<FeedItem> {

        val feed = mutableListOf<FeedItem>()

        // A️⃣ Jeux possédés
        val ownedGames = buyRepo.findByUserId(userId)

        // 🔹 Nouvelles évaluations
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

        // 🔹 Nouvelles extensions (simulé)
        ownedGames.forEach {
            feed.add(
                FeedItem(
                    type = FeedType.NEW_DLC,
                    gameId = it.gameId,
                    gameName = it.gameName,
                    message = "New extension available for ${it.gameName}",
                    createdAt = Instant.now()
                )
            )
        }

        // 🔹 Baisses de prix (wishlist)
        val wishlist = wishlistRepo.findByUserId(userId)
        wishlist.forEach {
            feed.add(
                FeedItem(
                    type = FeedType.PRICE_DROP,
                    gameId = it.gameId,
                    gameName = it.gameName,
                    message = "Price drop for ${it.gameName} on ${it.platform}",
                    createdAt = Instant.now()
                )
            )
        }

        return feed.sortedByDescending { it.createdAt }
    }





}
