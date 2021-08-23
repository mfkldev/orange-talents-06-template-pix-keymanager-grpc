package br.com.zupacademy.marciosouza.pixkey.grpc.createpixkey

import br.com.zupacademy.marciosouza.*
import br.com.zupacademy.marciosouza.pixkey.exception.InvalidDataException
import br.com.zupacademy.marciosouza.pixkey.validation.ErrorHandler
import io.grpc.Status
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class CreatePixKeyEndpoint(
    @Inject private val newKeyPixService: NewKeyPixService) : PixKeyServiceGrpc.PixKeyServiceImplBase(){

    override fun create(
        request: KeyRequest,
        responseObserver: StreamObserver<KeyResponse>){

        checkDataEntry(request, responseObserver)

        val pixKeyModel = request.toModel()

        val createdKeyPix = newKeyPixService.register(pixKeyModel)

        responseObserver.onNext(
            KeyResponse.newBuilder()
                .setClienteId(createdKeyPix.clientId.toString())
                .setPixId(createdKeyPix.id.toString())
                .build()
        )
        responseObserver.onCompleted()
    }

    fun checkDataEntry(request: KeyRequest, responseObserver: StreamObserver<KeyResponse>) {

        when {
            request.clienteId.isBlank() -> {
                responseObserver?.onError(Status.INVALID_ARGUMENT
                    .augmentDescription("\${required.id.client}")
                    .asRuntimeException())
                throw InvalidDataException("\${required.id.client}")
            }
            request.tipoChave.equals(TipoChave.TIPO_CHAVE_DESCONHECIDA) -> {
                responseObserver?.onError(Status.INVALID_ARGUMENT
                    .augmentDescription("\${required.valid.typekey}")
                    .asRuntimeException())
                throw InvalidDataException("\${required.valid.typekey}")
            }
            request.tipoConta.equals(TipoConta.TIPO_CONTA_DESCONHECIDA) -> {
                responseObserver?.onError(Status.INVALID_ARGUMENT
                    .augmentDescription("\${required.valid.typeaccount}")
                    .asRuntimeException())
                throw InvalidDataException("\${required.valid.typekey}")
            }
        }
    }
}