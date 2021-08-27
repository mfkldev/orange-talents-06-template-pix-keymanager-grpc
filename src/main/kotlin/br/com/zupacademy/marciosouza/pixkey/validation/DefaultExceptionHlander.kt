package br.com.zupacademy.marciosouza.pixkey.validation

import br.com.zupacademy.marciosouza.pixkey.exception.ExistingPixKeyException
import br.com.zupacademy.marciosouza.pixkey.exception.InvalidDataException
import io.grpc.Status
import javax.validation.ConstraintViolationException

class DefaultExceptionHandler : ExceptionHandler<Exception> {

    override fun handle(e: Exception): ExceptionHandler.StatusWithDetails {
        val status = when(e) {
            is IllegalArgumentException -> Status.INVALID_ARGUMENT.withDescription(e.message)
            is IllegalStateException -> Status.FAILED_PRECONDITION.withDescription(e.message)
            is ConstraintViolationException -> Status.INVALID_ARGUMENT.withDescription(e.message)
            is ExistingPixKeyException -> Status.ALREADY_EXISTS.withDescription(e.message)
            is InvalidDataException -> Status.INVALID_ARGUMENT.withDescription(e.message)
            else -> Status.UNKNOWN.withDescription(e.message)
        }
        return ExceptionHandler.StatusWithDetails(status.withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return true
    }
}