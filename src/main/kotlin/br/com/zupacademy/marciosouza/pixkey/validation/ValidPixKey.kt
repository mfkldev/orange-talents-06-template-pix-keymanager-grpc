package br.com.zupacademy.marciosouza.pixkey.validation

import br.com.zupacademy.marciosouza.TipoChave
import br.com.zupacademy.marciosouza.pixkey.model.PixKeyModel
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import javax.inject.Singleton
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.TYPE
import kotlin.reflect.KClass

@MustBeDocumented
@Retention(RUNTIME)
@Target(CLASS, TYPE)
@Constraint(validatedBy = [ValidPixKeyValidator::class])
annotation class ValidPixKey(
    val message: String = "formato da chave Pix não é válido",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = []
)

@Singleton
class ValidPixKeyValidator : ConstraintValidator<ValidPixKey, PixKeyModel> {

    override fun isValid(
        value: PixKeyModel?,
        annotationMetadata: AnnotationValue<ValidPixKey>,
        context: ConstraintValidatorContext?
    ): Boolean {

        when(value?.keyType){
            TipoChave.TELEFONE -> return value.key.matches("^\\+[1-9][0-9]\\d{1,14}$".toRegex())
            TipoChave.CPF -> return value.key.matches("^[0-9]{11}\$".toRegex())
            TipoChave.EMAIL -> return value.key.matches("^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+.[a-zA-Z0-9-.]+$".toRegex())

            else -> return true
        }
    }
}