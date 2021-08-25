package br.com.zupacademy.marciosouza.pixkey.client.bcbapi.dto

import br.com.zupacademy.marciosouza.TipoConta

enum class AccountType {
    CACC,
    SVGS;

    companion object {
        fun convert(tipoConta: TipoConta): AccountType {
            return when (tipoConta) {
                TipoConta.CONTA_CORRENTE -> CACC
                else -> SVGS
            }
        }
    }
}