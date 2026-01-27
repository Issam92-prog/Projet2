package com.projet2.user.repository

import com.projet2.user.model.Dlc
import org.springframework.data.jpa.repository.JpaRepository

interface DlcRepo : JpaRepository<Dlc, String>
