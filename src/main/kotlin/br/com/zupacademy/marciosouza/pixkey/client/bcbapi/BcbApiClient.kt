package br.com.zupacademy.marciosouza.pixkey.client.bcbapi

import br.com.zupacademy.marciosouza.pixkey.client.bcbapi.dto.CreatePixKeyRequest
import br.com.zupacademy.marciosouza.pixkey.client.bcbapi.dto.CreatePixKeyResponse
import br.com.zupacademy.marciosouza.pixkey.client.bcbapi.dto.DeletePixKeyRequest
import br.com.zupacademy.marciosouza.pixkey.client.bcbapi.dto.DeletePixKeyResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client

@Client("\${bcb.api.url}")
interface BcbApiClient {

    @Post(consumes = [MediaType.APPLICATION_XML], produces = [MediaType.APPLICATION_XML])
    fun postPixKey(@Body createPixKeyRequest : CreatePixKeyRequest) : HttpResponse<CreatePixKeyResponse>

    @Delete(value = "/{key}", consumes = [MediaType.APPLICATION_XML], produces = [MediaType.APPLICATION_XML])
    fun deletePixKey(@PathVariable key: String, @Body detetePixKeyRequest : DeletePixKeyRequest) : HttpResponse<DeletePixKeyResponse>
}