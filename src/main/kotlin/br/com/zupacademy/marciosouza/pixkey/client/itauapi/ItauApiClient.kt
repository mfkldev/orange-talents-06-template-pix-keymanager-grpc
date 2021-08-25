package br.com.zupacademy.marciosouza.pixkey.client.itauapi

import br.com.zupacademy.marciosouza.pixkey.client.itauapi.dto.AccountItauResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${itau.api.url}")
interface ItauApiClient {

    @Get("/clientes/{clienteId}/contas")
    fun getAccount(@PathVariable clienteId: String, @QueryValue tipo: String): HttpResponse<AccountItauResponse>
}