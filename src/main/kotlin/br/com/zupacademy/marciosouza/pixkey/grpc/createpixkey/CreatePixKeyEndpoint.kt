package br.com.zupacademy.marciosouza.pixkey.grpc.createpixkey

import br.com.zupacademy.marciosouza.*
import br.com.zupacademy.marciosouza.pixkey.exception.InvalidDataException
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
    @Inject private val newKeyPixService: NewKeyPixService,
    val messageApi: Messages)
    : PixKeyServiceGrpc.PixKeyServiceImplBase()
{
    override fun create(
        request: KeyRequest,
        responseObserver: StreamObserver<KeyResponse>){

        checkDataEntry(request, responseObserver)

        val createdKeyPix : PixKeyModel = newKeyPixService.register(request, responseObserver)

        responseObserver.onNext(
            KeyResponse.newBuilder()
                .setClienteId(createdKeyPix.clientId.toString())
                .setPixId(createdKeyPix.pixId.toString())
                .build()
        )
        responseObserver.onCompleted()
    }

    fun checkDataEntry(request: KeyRequest, responseObserver: StreamObserver<KeyResponse>) {

        when {
            request.clienteId.isBlank() -> {
                responseObserver.onError(Status.INVALID_ARGUMENT
                    .augmentDescription(messageApi.requiredIdClient)
                    .asRuntimeException())
                throw InvalidDataException(messageApi.requiredIdClient)
            }
            request.tipoChave.equals(TipoChave.TIPO_CHAVE_DESCONHECIDA) -> {
                responseObserver.onError(Status.INVALID_ARGUMENT
                    .augmentDescription(messageApi.requiredValidTypeKey)
                    .asRuntimeException())
                throw InvalidDataException(messageApi.requiredValidTypeKey)
            }
            request.tipoConta.equals(TipoConta.TIPO_CONTA_DESCONHECIDA) -> {
                responseObserver.onError(Status.INVALID_ARGUMENT
                    .augmentDescription(messageApi.requiredValidTypeAccount)
                    .asRuntimeException())
                throw InvalidDataException(messageApi.requiredValidTypeAccount)
            }
        }
    }
}