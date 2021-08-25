package br.com.zupacademy.marciosouza.pixkey.grpc.createpixkey.dto

import br.com.zupacademy.marciosouza.TipoChave
import br.com.zupacademy.marciosouza.TipoConta
import br.com.zupacademy.marciosouza.pixkey.client.itauapi.dto.AccountItauResponse
import br.com.zupacademy.marciosouza.pixkey.model.AssociatedAccount
import br.com.zupacademy.marciosouza.pixkey.model.PixKeyModel
import br.com.zupacademy.marciosouza.pixkey.validation.ValidPixKey
import br.com.zupacademy.marciosouza.pixkey.validation.ValidUUID
import java.util.*
import javax.persistence.Enumerated
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidPixKey
class PixKeyRequest(

    @ValidUUID
    @field:NotNull
    val clientId: UUID,

    @field:NotNull
    @Enumerated
    val keyType: TipoChave,

    @field:Size(max = 77)
    @field:NotBlank
    val key: String,

    @field:NotNull
    @Enumerated
    val accountType: TipoConta
) {
    fun toModel(accountItau: AccountItauResponse): PixKeyModel {
        return PixKeyModel(
            clientId, keyType, key, accountType,
            AssociatedAccount(
                accountItau.tipo,
                accountItau.instituicao.nome, accountItau.instituicao.ispb,
                accountItau.agencia, accountItau.numero,
                accountItau.titular.id, accountItau.titular.nome, accountItau.titular.cpf
            )
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PixKeyRequest

        if (clientId != other.clientId) return false
        if (keyType != other.keyType) return false
        if (key != other.key) return false
        if (accountType != other.accountType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = clientId.hashCode()
        result = 31 * result + keyType.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + accountType.hashCode()
        return result
    }
}
