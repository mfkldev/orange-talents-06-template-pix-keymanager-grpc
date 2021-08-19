package br.com.zupacademy.marciosouza.pixkey.model

import br.com.zupacademy.marciosouza.TipoChave
import br.com.zupacademy.marciosouza.TipoConta
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class PixKeyModel(

    @field:NotNull
    val clientId: UUID,

    @field:NotNull
    @Enumerated
    val keyType: TipoChave,

    @field:NotBlank
    val key: String,

    @field:NotNull
    @Enumerated
    val accountType: TipoConta
) {
    @Id
    @GeneratedValue
    val id: Long? = null

    val uuid: UUID = UUID.randomUUID()
    override fun toString(): String {
        return "PixKeyModel(clientId=$clientId, keyType=${keyType.name}, key='$key', accountType=${accountType.name}, id=$id, uuid=$uuid)"
    }
}
