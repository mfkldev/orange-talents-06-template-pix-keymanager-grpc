package br.com.zupacademy.marciosouza.pixkey.client.bcbapi.dto

import br.com.zupacademy.marciosouza.TipoChave
import br.com.zupacademy.marciosouza.pixkey.model.PixKeyModel

data class CreatePixKeyRequest(
    val keyType: TipoChave,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner
) {
    companion object {
        fun fromModel(pixKeyModel: PixKeyModel): CreatePixKeyRequest {
            return CreatePixKeyRequest(
                pixKeyModel.keyType,
                pixKeyModel.key,
                BankAccount(
                    pixKeyModel.associatedAccount.bankIspb,
                    pixKeyModel.associatedAccount.branch,
                    pixKeyModel.associatedAccount.numberAccount,
                    AccountType.convert(pixKeyModel.accountType)
                ),
                Owner(
                    TypeOwner.NATURAL_PERSON,
                    pixKeyModel.associatedAccount.ownerName,
                    pixKeyModel.associatedAccount.ownerCpf
                ))
        }
    }


    fun convertKeyType(): KeyType {
        return when (this.keyType) {
            TipoChave.CPF -> KeyType.CPF
            TipoChave.TELEFONE -> KeyType.PHONE
            TipoChave.EMAIL -> KeyType.EMAIL
            TipoChave.ALEATORIA -> KeyType.RANDOM
            else -> KeyType.CNPJ
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CreatePixKeyRequest

        if (keyType != other.keyType) return false
        if (bankAccount != other.bankAccount) return false
        if (owner != other.owner) return false

        return true
    }

    override fun hashCode(): Int {
        var result = keyType.hashCode()
        result = 31 * result + bankAccount.hashCode()
        result = 31 * result + owner.hashCode()
        return result
    }
}