package br.com.zupacademy.marciosouza.pixkey.client.bcbapi.dto

data class CreatePixKeyResponse(
    val keyType : KeyType,
    val key : String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt : String
)