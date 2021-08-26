package br.com.zupacademy.marciosouza.pixkey.grpc.createpixkey

import br.com.zupacademy.marciosouza.KeyRequest
import br.com.zupacademy.marciosouza.KeyResponse
import br.com.zupacademy.marciosouza.pixkey.client.bcbapi.BcbApiClient
import br.com.zupacademy.marciosouza.pixkey.client.bcbapi.dto.CreatePixKeyRequest
import br.com.zupacademy.marciosouza.pixkey.exception.ExistingPixKeyException
import br.com.zupacademy.marciosouza.pixkey.client.itauapi.ItauApiClient
import br.com.zupacademy.marciosouza.pixkey.messages.Messages
import br.com.zupacademy.marciosouza.pixkey.model.PixKeyModel
import br.com.zupacademy.marciosouza.pixkey.repository.PixKeyRepository
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Validated
@Singleton
class NewKeyPixService(
    @Inject val pixKeyRepository: PixKeyRepository,
    @Inject val itauApiClient: ItauApiClient,
    @Inject val bcbApiClient: BcbApiClient,
    val messageApi: Messages)
{
    @Transactional
    fun register(request: KeyRequest, responseObserver: StreamObserver<KeyResponse>) : PixKeyModel{

        val pixKeyRequest = request.toModel()

        try {
            pixKeyRepository.existsByKey(pixKeyRequest.key)
                    && throw ExistingPixKeyException(messageApi.keyAlreadyRegistered)
        }catch (exeption: Exception){
            when(exeption) {
                is ExistingPixKeyException -> {
                    responseObserver.onError(
                        Status.ALREADY_EXISTS
                            .augmentDescription(messageApi.keyAlreadyRegistered)
                            .asRuntimeException()
                    )
                }
            }
        }

        val accountsItauResponse = itauApiClient.getAccount(pixKeyRequest.clientId.toString(), pixKeyRequest.accountType.name)

        try{
            accountsItauResponse.body()
                ?: throw IllegalStateException(messageApi.clientNotFound)
        }catch (exeption: Exception){
            when(exeption) {
                is IllegalStateException -> {
                    responseObserver.onError(
                        Status.NOT_FOUND
                            .augmentDescription(messageApi.clientNotFound)
                            .asRuntimeException()
                    )
                }
            }
        }

        val pixKeyModel = pixKeyRequest.toModel(accountsItauResponse.body()!!)

        pixKeyRepository.save(pixKeyModel)

        bcbApiClient.postPixKey(CreatePixKeyRequest.fromModel(pixKeyModel)).run{
            pixKeyModel.associateKeyFromBcb(body()!!.key)
        }

        return pixKeyModel
    }
}