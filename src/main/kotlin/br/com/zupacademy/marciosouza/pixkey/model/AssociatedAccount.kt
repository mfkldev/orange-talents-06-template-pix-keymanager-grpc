package br.com.zupacademy.marciosouza.pixkey.model

import javax.persistence.Embeddable

@Embeddable
class AssociatedAccount(
    val typeAccount: String,
    val bankName: String,
    val bankIspb: String,
    val agency: String,
    val bumber: String,
    val ownerId: String,
    val ownerName: String,
    val ownerCpf: String
)
