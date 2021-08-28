package br.com.zupacademy.marciosouza.pixkey.model

import javax.persistence.Embeddable

@Embeddable
class AssociatedAccount(
    val typeAccount: String,
    val bankName: String,
    val bankIspb: String,
    val branch: String,
    val numberAccount: String,
    val ownerId: String,
    val ownerName: String,
    val ownerCpf: String
){
    override fun toString(): String {
        return "AssociatedAccount(typeAccount='$typeAccount', bankName='$bankName', bankIspb='$bankIspb', branch='$branch', numberAccount='$numberAccount', ownerId='$ownerId', ownerName='$ownerName', ownerCpf='$ownerCpf')"
    }
}
