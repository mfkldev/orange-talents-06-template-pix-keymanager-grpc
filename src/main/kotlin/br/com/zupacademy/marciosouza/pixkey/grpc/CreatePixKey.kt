package br.com.zupacademy.marciosouza.pixkey.grpc

import br.com.zupacademy.marciosouza.CreateKeyRequest
import br.com.zupacademy.marciosouza.CreateKeyResponse
import br.com.zupacademy.marciosouza.KeymanagerCreateKeyServiceGrpc
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreatePixKey(@Inject private val newKeyPixService: NewKeyPixService) : KeymanagerCreateKeyServiceGrpc.KeymanagerCreateKeyServiceImplBase(){

    override fun create(
        request: CreateKeyRequest,
        responseObserver: StreamObserver<CreateKeyResponse>)
    {
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
}