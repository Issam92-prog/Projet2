package com.projet2.user.controller

import com.projet2.user.dto.*
import com.projet2.user.kafka.EventPublisher
import com.projet2.user.kafka.Topics
import com.projet2.user.mapper.BuyToGamePurchasedMapper
import com.projet2.user.mapper.RateToGameReviewMapper
import com.projet2.user.model.*
import com.projet2.user.repository.*
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users") // Préfixe standardisé
class UserController(
    private val userRepo: UserRepo,
    private val buyRepo: BuyRepo,
    private val rateRepo: RateRepo,
    private val reviewVoteRepo: ReviewVoteRepo,
    private val wishlistRepo: WishlistRepo,
    private val eventPublisher: EventPublisher
) {

    private val log = LoggerFactory.getLogger(UserController::class.java)

    /* ===================== PROFIL JOUEUR (Nouveau) ===================== */

    @GetMapping("/{targetId}/profile")
    fun getUserProfile(
        @PathVariable targetId: Long,
        @RequestParam(required = false) requesterId: Long?
    ): ResponseEntity<UserProfileDTO> {

        // 1. Trouver l'utilisateur cible
        val targetUser = userRepo.findById(targetId)
            .orElseThrow { RuntimeException("Utilisateur introuvable") }

        // 2. Récupérer ses données
        val myGames = buyRepo.findAllByUserId(targetId)
        val myReviews = rateRepo.findByUserId(targetId) // Assure-toi d'avoir cette méthode dans RateRepo

        // 3. Logique de Confidentialité
        val isMe = (requesterId != null && requesterId == targetId)

        val profile = if (isMe) {
            // CAS 1 : C'est MOI (je vois tout)
            UserProfileDTO(
                userId = targetUser.id.toString(),
                username = targetUser.pseudo,
                joinedAt = targetUser.createdAt.toString(),
                email = targetUser.email,
                totalPlayTime = myGames.sumOf { it.playTimeHours },
                gamesOwned = myGames.map { GameSummary(it.gameName, it.playTimeHours) },
                reviews = myReviews.map { ReviewSummary(it.gameId, it.note, it.comment) }
            )
        } else {
            // CAS 2 : C'est un AUTRE (Vue publique limitée)
            UserProfileDTO(
                userId = targetUser.id.toString(),
                username = targetUser.pseudo,
                joinedAt = targetUser.createdAt.toString(),
                email = null, // Masqué
                totalPlayTime = null, // Masqué
                gamesOwned = myGames.take(5).map { GameSummary(it.gameName, it.playTimeHours) }, // Aperçu seulement
                reviews = myReviews.map { ReviewSummary(it.gameId, it.note, it.comment) } // Les avis restent souvent publics
            )
        }

        return ResponseEntity.ok(profile)
    }

    /* ===================== BUY GAME ===================== */

    @PostMapping("/{userId}/buy")
    fun buyGame(
        @PathVariable userId: Long,
        @RequestBody req: BuyRequest
    ): Buy {

        // 1. Vérifier l'utilisateur
        userRepo.findById(userId)
            .orElseThrow { RuntimeException("Utilisateur introuvable") }

        // 2. LOGIQUE DLC : Vérification du jeu de base
        val isDlcValue = req.isDlc ?: false
        if (isDlcValue) {
            if (req.parentGameId == null) {
                throw RuntimeException("Erreur: L'ID du jeu parent est requis pour acheter un DLC")
            }

            // AMÉLIORATION : On vérifie que le joueur possède le jeu parent SUR LA MÊME PLATEFORME
            // (On ne peut pas acheter un DLC sur PC si on a le jeu sur PS5)
            val ownsBaseGame = buyRepo.existsByUserIdAndGameIdAndPlatform(userId, req.parentGameId, req.platform)

            if (!ownsBaseGame) {
                throw RuntimeException("⛔ Impossible d'acheter ce DLC sur ${req.platform} : Vous ne possédez pas le jeu de base sur cette plateforme !")
            }
        }

        // 3. Vérifier si on possède déjà cet article SUR CETTE PLATEFORME
        // C'est ici que la magie multi-plateforme opère.
        if (buyRepo.existsByUserIdAndGameIdAndPlatform(userId, req.gameId, req.platform)) {
            throw RuntimeException("Vous possédez déjà ce jeu/DLC sur la plateforme ${req.platform} !")
        }

        // 4. Enregistrement
        val buy = Buy(
            userId = userId,
            gameId = req.gameId,
            gameName = req.gameName,
            platform = req.platform, // String
            price = req.price,
            isDlc = isDlcValue
        )

        val savedBuy = buyRepo.save(buy)

        // 5. Kafka Event
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

        // 1. On récupère l'achat complet (au lieu de juste vérifier s'il existe)
        val purchase = buyRepo.findByUserIdAndGameId(userId, req.gameId)
            ?: throw RuntimeException("Vous ne possédez pas ce jeu, vous ne pouvez pas le noter.")

        // 2. CONDITION DE TEMPS DE JEU (Exemple: 2 heures minimum)
        val minPlayTime = 2.0 // Heures
        if (purchase.playTimeHours < minPlayTime) {
            throw RuntimeException("Trop tôt pour juger ! Vous devez jouer au moins $minPlayTime heures avant de donner votre avis (Temps actuel : ${purchase.playTimeHours}h).")
        }

        // Condition de ne pas avoir deja donné un avis
        if (rateRepo.existsByUserIdAndGameId(userId, req.gameId)) {
            throw RuntimeException("Vous avez déjà donné votre avis sur ce jeu !")
        }

        // 3. On enregistre la note
        val rate = Rate(
            userId = userId,
            gameId = req.gameId,
            gameName = req.gameName,
            note = req.note,
            comment = req.comment
        )

        val savedRate = rateRepo.save(rate)

        // 4. Kafka event
        val event = RateToGameReviewMapper.map(savedRate)
        log.info("📝 Avis créé par User {} pour le jeu {}", userId, req.gameName)
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

        log.info("👍 Vote enregistré pour l'avis ID {}: Utile={}", rateId, req.useful)

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
        @RequestParam platform: String
    ) {
        wishlistRepo.deleteByUserIdAndGameIdAndPlatform(userId, gameId, platform)
    }

    @GetMapping("/{userId}/wishlist")
    fun getWishlist(@PathVariable userId: Long): List<WishlistItem> {
        return wishlistRepo.findByUserId(userId)
    }


    /* ===================== FEED ===================== */

    @GetMapping("/{userId}/feed")
    fun getFeed(@PathVariable userId: Long): List<FeedItem> {
        val feed = mutableListOf<FeedItem>()
        val ownedGames = buyRepo.findAllByUserId(userId)

        ownedGames.forEach { buy ->
            val reviews = rateRepo.findByGameIdAndUserIdNot(buy.gameId, userId)
            reviews.forEach { review ->

                // ✅ CORRECTION & AJOUT : On appelle les nouvelles méthodes du Repo
                val useful = reviewVoteRepo.countByRateIdAndUsefulTrue(review.id)
                val useless = reviewVoteRepo.countByRateIdAndUsefulFalse(review.id)

                feed.add(
                    FeedItem(
                        type = FeedType.NEW_REVIEW,
                        gameId = review.gameId,
                        gameName = review.gameName,
                        message = "New review: ${review.note}/5 \nCommentaire: ${review.comment}",
                        createdAt = review.ratedAt,

                        // On injecte les deux scores
                        usefulCount = useful,
                        uselessCount = useless
                    )
                )
            }
        }
        return feed.sortedByDescending { it.createdAt }
    }

    @GetMapping("/rates/game/{gameId}")
    fun getGameReviews(@PathVariable gameId: String): List<Rate> {
        // Cette méthode existe déjà dans votre RateRepo
        return rateRepo.findByGameId(gameId)
    }

    @PutMapping("/{userId}/library/{gameId}/playtime")
    fun addPlayTime(
        @PathVariable userId: Long, // Attention au type Long vs String selon votre repo
        @PathVariable gameId: String,
        @RequestBody req: UpdatePlayTimeRequest
    ): ResponseEntity<String> {

        // 1. Récupération
        val buy = buyRepo.findByUserIdAndGameId(userId, gameId)
            ?: return ResponseEntity.notFound().build()

        // 2. Addition (Fonctionne car c'est Int + Int)
        // Grâce au 'var' dans l'étape 1, on peut modifier directement
        buy.playTimeHours += req.hours

        // 3. Sauvegarde
        buyRepo.save(buy)

        return ResponseEntity.ok("Temps de jeu ajouté ! Total : ${buy.playTimeHours}h")
    }
}