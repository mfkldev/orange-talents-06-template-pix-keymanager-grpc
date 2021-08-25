package br.com.zupacademy.marciosouza.pixkey.client.bcbapi.dto

class Owner(
    val type : TypeOwner,
    val name : String,
    val taxIdNumber : String


) {
    override fun toString(): String {
        return "Owner(type=$type, name='$name', taxIdNumber='$taxIdNumber')"
    }
}