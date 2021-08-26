package br.com.zupacademy.marciosouza.pixkey.grpc.createpixkey

import br.com.zupacademy.marciosouza.KeyRequest
import br.com.zupacademy.marciosouza.KeyResponse
import br.com.zupacademy.marciosouza.TipoChave
import br.com.zupacademy.marciosouza.TipoConta
import br.com.zupacademy.marciosouza.pixkey.exception.InvalidDataException
import br.com.zupacademy.marciosouza.pixkey.messages.Messages
import br.com.zupacademy.marciosouza.pixkey.validation.stringIsNotUUID
import io.grpc.Status
import io.grpc.stub.StreamObserver

fun checkDataEntry(request: KeyRequest, responseObserver: StreamObserver<KeyResponse>, messageApi: Messages) {
    when {
        request.clienteId.isBlank() -> {
            responseObserver.onError(
                Status.INVALID_ARGUMENT
                .augmentDescription(messageApi.requiredIdClient)
                .asRuntimeException())
            throw InvalidDataException(messageApi.requiredIdClient)
        }
        request.clienteId.stringIsNotUUID() -> {
            responseObserver.onError(
                Status.INVALID_ARGUMENT
                .augmentDescription(messageApi.uuidBadFomart)
                .asRuntimeException())
            throw InvalidDataException(messageApi.uuidBadFomart)
        }
        request.tipoChave.equals(TipoChave.TIPO_CHAVE_DESCONHECIDA) -> {
            responseObserver.onError(
                Status.INVALID_ARGUMENT
                .augmentDescription(messageApi.requiredValidTypeKey)
                .asRuntimeException())
            throw InvalidDataException(messageApi.requiredValidTypeKey)
        }
        request.tipoConta.equals(TipoConta.TIPO_CONTA_DESCONHECIDA) -> {
            responseObserver.onError(
                Status.INVALID_ARGUMENT
                .augmentDescription(messageApi.requiredValidTypeAccount)
                .asRuntimeException())
            throw InvalidDataException(messageApi.requiredValidTypeAccount)
        }
        request.chave.length > 77 -> {
            responseObserver.onError(
                Status.INVALID_ARGUMENT
                .augmentDescription(messageApi.keyBigger77)
                .asRuntimeException())
            throw InvalidDataException(messageApi.keyBigger77)
        }
        !request.chave.matches(stringToMatches(request.tipoChave).toRegex()) -> {
            responseObserver.onError(
                Status.INVALID_ARGUMENT
                    .augmentDescription(messageApi.invalidPixFormat)
                    .asRuntimeException())
            throw InvalidDataException(messageApi.invalidPixFormat)
        }
    }
}

fun stringToMatches(tipoChave: TipoChave): String {
    return when{
        tipoChave.equals(TipoChave.TELEFONE) -> "^\\+[1-9][0-9]\\d{1,14}$"
        tipoChave.equals(TipoChave.CPF) -> "^[0-9]{11}\$"
        tipoChave.equals(TipoChave.EMAIL) -> "^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+.[a-zA-Z0-9-.]+$"
        else -> ""
    }
}
