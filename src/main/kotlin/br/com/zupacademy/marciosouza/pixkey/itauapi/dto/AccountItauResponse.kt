
package br.com.zupacademy.marciosouza.pixkey.itauapi.dto

data class AccountItauResponse(
    val tipo: String,
    val instituicao: InstituicaoReponse,
    val agencia: String,
    val numero: String,
    val titular: TitularResponse
)