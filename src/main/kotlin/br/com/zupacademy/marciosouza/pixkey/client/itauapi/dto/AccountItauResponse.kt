
package br.com.zupacademy.marciosouza.pixkey.client.itauapi.dto

data class AccountItauResponse(
    val tipo: String,
    val instituicao: InstituicaoReponse,
    val agencia: String,
    val numero: String,
    val titular: TitularResponse
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AccountItauResponse

        if (tipo != other.tipo) return false
        if (instituicao != other.instituicao) return false
        if (agencia != other.agencia) return false
        if (numero != other.numero) return false
        if (titular != other.titular) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tipo.hashCode()
        result = 31 * result + instituicao.hashCode()
        result = 31 * result + agencia.hashCode()
        result = 31 * result + numero.hashCode()
        result = 31 * result + titular.hashCode()
        return result
    }
}