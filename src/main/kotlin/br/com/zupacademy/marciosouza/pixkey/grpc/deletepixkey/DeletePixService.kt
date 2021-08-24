package br.com.zupacademy.marciosouza.pixkey.grpc.deletepixkey

import br.com.zupacademy.marciosouza.pixkey.exception.ObjectNotFoundException
import br.com.zupacademy.marciosouza.pixkey.messages.Messages
import br.com.zupacademy.marciosouza.pixkey.repository.PixKeyRepository
import br.com.zupacademy.marciosouza.pixkey.validation.ValidUUID
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Singleton
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class DeletePixService(val repository: PixKeyRepository, val messageApi : Messages) {

    fun delete(@NotBlank @ValidUUID clientId: String, @NotBlank @ValidUUID keyId: String)
    {
        val searchedPixKeyModel = repository.findByPixId(UUID.fromString(keyId))
            .orElseThrow{ ObjectNotFoundException(messageApi.pixkeyNotFound) }
        repository.delete(searchedPixKeyModel)
    }
}