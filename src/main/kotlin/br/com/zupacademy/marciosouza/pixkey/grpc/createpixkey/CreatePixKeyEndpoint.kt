package br.com.zupacademy.marciosouza.pixkey.grpc.createpixkey

import br.com.zupacademy.marciosouza.*
import br.com.zupacademy.marciosouza.pixkey.exception.ExistingPixKeyException
import br.com.zupacademy.marciosouza.pixkey.messages.Messages
import br.com.zupacademy.marciosouza.pixkey.model.PixKeyModel
import br.com.zupacademy.marciosouza.pixkey.validation.ErrorHandler
import io.grpc.Status
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class CreatePixKeyEndpoint(
    @Inject private val createKeyPixService: CreateKeyPixService,
    val messageApi: Messages)
    : PixKeyServiceGrpc.PixKeyServiceImplBase()
{
    override fun create(
        request: KeyRequest,
        responseObserver: StreamObserver<KeyResponse>){

        checkDataEntry(request, responseObserver, messageApi)

        try {
            val createdKeyPix : PixKeyModel = createKeyPixService.register(request, responseObserver)
            responseObserver.onNext(
                KeyResponse.newBuilder()
                    .setClienteId(createdKeyPix.clientId.toString())
                    .setPixId(createdKeyPix.pixId.toString())
                    .build()
            )
            responseObserver.onCompleted()
        } catch (exception: Exception) {
            responseObserver.onError(
                when (exception) {
                    is ExistingPixKeyException -> Status.ALREADY_EXISTS
                        .augmentDescription(exception.message)
                        .asRuntimeException()
                    else -> Status.INTERNAL.augmentDescription("Erro inesperado")
                        .asRuntimeException()
                }
            )

        }
    }
}