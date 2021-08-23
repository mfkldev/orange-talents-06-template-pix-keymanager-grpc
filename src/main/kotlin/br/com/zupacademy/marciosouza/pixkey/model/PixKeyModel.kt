package br.com.zupacademy.marciosouza.pixkey.model

import br.com.zupacademy.marciosouza.TipoChave
import br.com.zupacademy.marciosouza.TipoConta
import br.com.zupacademy.marciosouza.pixkey.validation.ValidPixKey
import br.com.zupacademy.marciosouza.pixkey.validation.ValidUUID
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidPixKey
@Entity
class PixKeyModel(

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
    @Id
    @GeneratedValue
    val id: Long? = null

    val pixId: UUID = UUID.randomUUID()

    override fun toString(): String {
        return "PixKeyModel(clientId=$clientId, keyType=${keyType.name}, key='$key', accountType=${accountType.name}, id=$id, uuid=$pixId)"
    }
}
