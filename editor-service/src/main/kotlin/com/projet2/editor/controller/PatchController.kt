package com.projet2.editor.controller

import com.projet2.editor.domain.PatchType
import com.projet2.editor.dto.request.CreatePatchRequest
import com.projet2.editor.service.PatchService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/patches")
class PatchController(
    private val patchService: PatchService
) {

    @PostMapping
    fun createPatch(@RequestBody request: CreatePatchRequest): ResponseEntity<String> {
        return try {
            patchService.releasePatch(
                gameId = request.gameId,
                newVersion = request.newVersion,
                description = request.description,
                changes = request.changes,
                type = PatchType.valueOf(request.type.uppercase())
            )
            ResponseEntity.ok("✅ Patch ${request.newVersion} créé avec succès")
        } catch (e: Exception) {
            ResponseEntity.badRequest().body("❌ Erreur: ${e.message}")
        }
    }
}