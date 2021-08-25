package br.com.zupacademy.marciosouza.pixkey.client.bcbapi

import br.com.zupacademy.marciosouza.pixkey.client.bcbapi.dto.CreatePixKeyRequest
import br.com.zupacademy.marciosouza.pixkey.client.bcbapi.dto.CreatePixKeyResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client

@Client("\${bcb.api.url}")
interface BcbApiClient {

    @Post(consumes = [MediaType.APPLICATION_XML], produces = [MediaType.APPLICATION_XML])
    fun postPixKey(@Body createPixKeyRequest : CreatePixKeyRequest) : HttpResponse<CreatePixKeyResponse>
}