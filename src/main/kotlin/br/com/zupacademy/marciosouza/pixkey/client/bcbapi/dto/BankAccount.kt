package br.com.zupacademy.marciosouza.pixkey.client.bcbapi.dto

class BankAccount(
    val participant : String,
    val branch : String,
    val accountNumber : String,
    val accountType : AccountType

) {
    override fun toString(): String {
        return "BankAccount(participant='$participant', branch='$branch', accountNumber='$accountNumber', accountType=$accountType)"
    }
}