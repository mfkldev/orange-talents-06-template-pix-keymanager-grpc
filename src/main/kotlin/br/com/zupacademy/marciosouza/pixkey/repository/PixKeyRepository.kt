package br.com.zupacademy.marciosouza.pixkey.repository

import br.com.zupacademy.marciosouza.pixkey.model.PixKeyModel
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface PixKeyRepository : JpaRepository<PixKeyModel, Long> {
    fun findByKey(key: String): PixKeyModel?
}