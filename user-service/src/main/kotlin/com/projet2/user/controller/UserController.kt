package com.projet2.user.controller

import com.projet2.user.dto.BuyRequest
import com.projet2.user.dto.CreateUserRequest
import com.projet2.user.dto.RateRequest
import com.projet2.user.model.Buy
import com.projet2.user.model.Rate
import com.projet2.user.model.User
import com.projet2.user.repository.BuyRepo
import com.projet2.user.repository.RateRepo
import com.projet2.user.repository.UserRepo
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(
    private val userRepo: UserRepo,
    private val buyRepo: BuyRepo,
    private val rateRepo: RateRepo
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
            note = req.note,
            comment = req.comment
        )

        return rateRepo.save(rate)
    }

}
