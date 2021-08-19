package br.com.zupacademy.marciosouza.pixkey.itauapi.dto

data class AccountsItauResponse(
    val tipo: String,
    val instituicao: InstituicaoReponse,
    val agencia: String,
    val numero: String,
    val titular: TitularResponse
)