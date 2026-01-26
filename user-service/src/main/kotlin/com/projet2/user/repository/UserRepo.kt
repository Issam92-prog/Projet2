package com.projet2.user.repository

import com.projet2.user.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepo : JpaRepository<User, Long>
