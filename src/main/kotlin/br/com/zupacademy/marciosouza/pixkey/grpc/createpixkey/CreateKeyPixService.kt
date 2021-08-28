package br.com.zupacademy.marciosouza.pixkey.grpc.createpixkey

import br.com.zupacademy.marciosouza.KeyRequest
import br.com.zupacademy.marciosouza.KeyResponse
import br.com.zupacademy.marciosouza.pixkey.client.bcbapi.BcbApiClient
import br.com.zupacademy.marciosouza.pixkey.client.bcbapi.dto.CreatePixKeyRequest
import br.com.zupacademy.marciosouza.pixkey.exception.ExistingPixKeyException
import br.com.zupacademy.marciosouza.pixkey.client.itauapi.ItauApiClient
import br.com.zupacademy.marciosouza.pixkey.exception.InternalServerErrorException
import br.com.zupacademy.marciosouza.pixkey.messages.Messages
import br.com.zupacademy.marciosouza.pixkey.model.PixKeyModel
import br.com.zupacademy.marciosouza.pixkey.repository.PixKeyRepository
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.validation.Validated
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Validated
@Singleton
class CreateKeyPixService(
    @Inject val pixKeyRepository: PixKeyRepository,
    @Inject val itauApiClient: ItauApiClient,
    @Inject val bcbApiClient: BcbApiClient,
    val messageApi: Messages
) {
    val LOG: Logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun register(request: KeyRequest, responseObserver: StreamObserver<KeyResponse>): PixKeyModel {

        val pixKeyRequest = request.toModel()

        try {
            pixKeyRepository.existsByKey(pixKeyRequest.key)
                    && throw ExistingPixKeyException(messageApi.keyAlreadyRegistered)
        } catch (exception: ExistingPixKeyException) {
            responseObserver.onError(
                Status.ALREADY_EXISTS
                    .augmentDescription(messageApi.keyAlreadyRegistered)
                    .asRuntimeException()
            )
        }

        val accountsItauResponse =
            itauApiClient.getAccount(pixKeyRequest.clientId.toString(), pixKeyRequest.accountType.name)

        try {
            accountsItauResponse.body()
                ?: throw IllegalStateException(messageApi.clientNotFound)
        } catch (exception: IllegalStateException) {
            responseObserver.onError(
                Status.NOT_FOUND
                    .augmentDescription(messageApi.clientNotFound)
                    .asRuntimeException()
            )
        }

        val pixKeyModel = pixKeyRequest.toModel(accountsItauResponse.body()!!)
        val createPixKeyRequest = CreatePixKeyRequest.fromModel(pixKeyModel)

        try {
            val responseBcb = bcbApiClient.postPixKey(createPixKeyRequest)
            pixKeyModel.associateKeyFromBcb(responseBcb.body()!!.key)
        } catch (e: HttpClientResponseException) {

            when (e.status) {
                HttpStatus.UNPROCESSABLE_ENTITY -> {
                    throw ExistingPixKeyException(messageApi.keyAlreadyRegisteredBcb)
                }
                else -> {
                    "status = ${e.status}; mensagem = ${e.message} "
                    throw InternalServerErrorException(messageApi.unexpectedError)
                }
            }
        }

        pixKeyRepository.save(pixKeyModel)

        return pixKeyModel
    }
}