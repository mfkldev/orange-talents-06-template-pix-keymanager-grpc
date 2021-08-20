package br.com.zupacademy.marciosouza.pixkey.grpc.createpixkey

import br.com.zupacademy.marciosouza.*
import br.com.zupacademy.marciosouza.pixkey.exception.InvalidDataException
import br.com.zupacademy.marciosouza.pixkey.validation.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class CreatePixKeyEndpoint(
    @Inject private val newKeyPixService: NewKeyPixService) : KeymanagerCreateKeyServiceGrpc.KeymanagerCreateKeyServiceImplBase(){

    override fun create(
        request: CreateKeyRequest,
        responseObserver: StreamObserver<CreateKeyResponse>){

        checkDataEntry(request)

        val pixKeyModel = request.toModel()

        val createdKeyPix = newKeyPixService.register(pixKeyModel)

        responseObserver.onNext(
            CreateKeyResponse.newBuilder()
                .setClienteId(createdKeyPix.clientId.toString())
                .setPixId(createdKeyPix.id.toString())
                .build()
        )
        responseObserver.onCompleted()
    }

    fun checkDataEntry(request: CreateKeyRequest) {
        when {
            request.clienteId.isBlank() -> throw InvalidDataException("É obrigatório passar o id do cliente")
            request.tipoChave.equals(TipoChave.TIPO_CHAVE_DESCONHECIDA) -> throw InvalidDataException("É obrigatório passar um tipo de chave válido")
            request.tipoConta.equals(TipoConta.TIPO_CONTA_DESCONHECIDA) -> throw InvalidDataException("É obrigatório passar um tipo de conta válido")
        }
    }
}