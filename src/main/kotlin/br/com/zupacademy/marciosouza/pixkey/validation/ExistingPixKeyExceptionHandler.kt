package br.com.zupacademy.marciosouza.pixkey.validation

import br.com.zupacademy.marciosouza.pixkey.exception.ExistingPixKeyException
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ExistingPixKeyExceptionHandler : ExceptionHandler<ExistingPixKeyException> {
    override fun handle(e: ExistingPixKeyException): ExceptionHandler.StatusWithDetails {
        return ExceptionHandler.StatusWithDetails(
            Status.ALREADY_EXISTS
                .withDescription(e.message)
                .withCause(e)
        )
    }

    override fun supports(e: Exception): Boolean {
        return e is ExistingPixKeyException
    }

}