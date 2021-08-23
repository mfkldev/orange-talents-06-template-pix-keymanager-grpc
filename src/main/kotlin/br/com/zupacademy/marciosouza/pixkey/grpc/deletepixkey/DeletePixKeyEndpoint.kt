package br.com.zupacademy.marciosouza.pixkey.grpc.deletepixkey

import br.com.zupacademy.marciosouza.DelKeyRequest
import br.com.zupacademy.marciosouza.DelKeyResponse
import br.com.zupacademy.marciosouza.DeletePixKeyServiceGrpc
import br.com.zupacademy.marciosouza.pixkey.messages.Messages
import io.grpc.Status
import io.grpc.stub.StreamObserver
import org.hibernate.ObjectNotFoundException
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class DeletePixKeyEndpoint(
    @Inject private val deleteKeyPixService: DeletePixService,
    val messageApi : Messages) : DeletePixKeyServiceGrpc.DeletePixKeyServiceImplBase()
{
    override fun delete(
        request : DelKeyRequest,
        responseObserver: StreamObserver<DelKeyResponse>)
    {
        try{
            deleteKeyPixService.delete(request.clienteId, request.pixId)
            responseObserver.onNext(
                DelKeyResponse
                    .newBuilder()
                    .setClienteId(request.clienteId)
                    .setPixId(request.clienteId)
                    .build()
            )
            responseObserver.onCompleted()
        }catch(exception: Exception){
            responseObserver.onError(
                when(exception){
                    is ObjectNotFoundException -> Status.NOT_FOUND
                        .augmentDescription(exception.message)
                        .asRuntimeException()
                    is ConstraintViolationException -> Status.INVALID_ARGUMENT
                        .augmentDescription(exception.message)
                        .asRuntimeException()
                    else -> Status.INTERNAL.augmentDescription("Erro inesperado")
                        .asRuntimeException()
                }
            )
        }

    }
}