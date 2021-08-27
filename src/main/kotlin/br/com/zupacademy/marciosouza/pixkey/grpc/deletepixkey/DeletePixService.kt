package br.com.zupacademy.marciosouza.pixkey.grpc.deletepixkey

import br.com.zupacademy.marciosouza.pixkey.client.bcbapi.BcbApiClient
import br.com.zupacademy.marciosouza.pixkey.client.bcbapi.dto.DeletePixKeyRequest
import br.com.zupacademy.marciosouza.pixkey.exception.ExistingPixKeyException
import br.com.zupacademy.marciosouza.pixkey.exception.ForbiddenOperationBcbException
import br.com.zupacademy.marciosouza.pixkey.exception.InternalServerErrorException
import br.com.zupacademy.marciosouza.pixkey.exception.ObjectNotFoundException

import br.com.zupacademy.marciosouza.pixkey.messages.Messages
import br.com.zupacademy.marciosouza.pixkey.repository.PixKeyRepository
import br.com.zupacademy.marciosouza.pixkey.validation.ValidUUID
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.validation.Validated
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class DeletePixService(
    @Inject val repository: PixKeyRepository,
    @Inject val bcbApiClient: BcbApiClient,
    val messageApi : Messages
    ) {

    val LOG: Logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun delete(@NotBlank @ValidUUID clientId: String, @NotBlank @ValidUUID pixId: String)
    {
        val searchedPixKeyModel = repository.findByPixId(UUID.fromString(pixId))
            .orElseThrow{ ObjectNotFoundException(messageApi.pixkeyNotfoundThis) }

        try {
            bcbApiClient.deletePixKey(
                searchedPixKeyModel.key,
                DeletePixKeyRequest(searchedPixKeyModel.key, searchedPixKeyModel.associatedAccount.bankIspb)
            ).run {
                if(status.equals(HttpStatus.NOT_FOUND)) throw ObjectNotFoundException(messageApi.pixkeyNotFoundBcb) //DANDO NULLPOINTER NO TESTE AQUI
            }
        }catch (e: HttpClientResponseException){
            when (e.status) {
                HttpStatus.FORBIDDEN -> {
                    throw ForbiddenOperationBcbException(messageApi.forbiddenOperationBcb)
                }
                else -> {
                    "status = ${e.status}; mensagem = ${e.message} "
                    throw InternalServerErrorException(messageApi.unexpectedError)
                }
            }
        }
        repository.delete(searchedPixKeyModel)
    }
}