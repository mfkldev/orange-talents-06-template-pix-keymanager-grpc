package br.com.zupacademy.marciosouza.pixkey.grpc

import br.com.zupacademy.marciosouza.TipoConta
import br.com.zupacademy.marciosouza.CreateKeyRequest
import br.com.zupacademy.marciosouza.TipoChave
import br.com.zupacademy.marciosouza.pixkey.model.PixKeyModel
import java.util.*

fun CreateKeyRequest.toModel(): PixKeyModel {
    return PixKeyModel(
        clientId = UUID.fromString(clienteId),
        keyType = TipoChave.valueOf(tipoChave.name),
        key = if(tipoChave == TipoChave.ALEATORIA) UUID.randomUUID().toString() else chave,
        accountType = TipoConta.valueOf(tipoConta.name)
    )
}