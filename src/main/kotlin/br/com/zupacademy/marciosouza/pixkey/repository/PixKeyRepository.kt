package br.com.zupacademy.marciosouza.pixkey.repository

import br.com.zupacademy.marciosouza.pixkey.model.PixKeyModel
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface PixKeyRepository : JpaRepository<PixKeyModel, Long> {
    fun existsByKey(key: String): Boolean
    fun findByPixId(pixId: UUID): Optional<PixKeyModel>
}