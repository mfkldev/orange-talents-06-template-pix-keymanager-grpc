package br.com.zupacademy.marciosouza.pixkey.grpc.createpixkey

import br.com.zupacademy.marciosouza.KeyRequest
import br.com.zupacademy.marciosouza.KeyResponse
import br.com.zupacademy.marciosouza.pixkey.exception.ExistingPixKeyException
import br.com.zupacademy.marciosouza.pixkey.itauapi.ItauApiClient
import br.com.zupacademy.marciosouza.pixkey.messages.Messages
import br.com.zupacademy.marciosouza.pixkey.model.PixKeyModel
import br.com.zupacademy.marciosouza.pixkey.repository.PixKeyRepository
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.context.annotation.Value
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton

@Validated
@Singleton
class NewKeyPixService(
    @Inject val pixKeyRepository: PixKeyRepository,
    @Inject val itauApiClient: ItauApiClient,
    val messageApi: Messages)
{
    fun register(request: KeyRequest, responseObserver: StreamObserver<KeyResponse>) : PixKeyModel{

        val pixKeyModel = request.toModel()

        try {
            pixKeyRepository.existsByKey(pixKeyModel.key) && throw ExistingPixKeyException(messageApi.keyAlreadyRegistered)
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

        val accountsItauResponse = itauApiClient.getAccount(pixKeyModel.clientId.toString(), pixKeyModel.accountType.name)

        try{
            accountsItauResponse.body()?:
            throw IllegalStateException(messageApi.clientNotFound)
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

        pixKeyRepository.save(pixKeyModel)

        return pixKeyModel
    }
}
