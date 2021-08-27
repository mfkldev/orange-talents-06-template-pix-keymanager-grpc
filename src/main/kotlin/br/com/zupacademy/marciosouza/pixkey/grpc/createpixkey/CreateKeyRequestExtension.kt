package br.com.zupacademy.marciosouza.pixkey.grpc.createpixkey

import br.com.zupacademy.marciosouza.TipoConta
import br.com.zupacademy.marciosouza.KeyRequest
import br.com.zupacademy.marciosouza.TipoChave
import br.com.zupacademy.marciosouza.pixkey.grpc.createpixkey.dto.PixKeyRequest
import java.util.*

fun KeyRequest.toModel(): PixKeyRequest {
    return PixKeyRequest(
        clientId = UUID.fromString(clienteId),
        keyType = TipoChave.valueOf(tipoChave.name),
        key = chave,
        accountType = TipoConta.valueOf(tipoConta.name)
    )
}