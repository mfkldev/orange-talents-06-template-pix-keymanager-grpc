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
)
