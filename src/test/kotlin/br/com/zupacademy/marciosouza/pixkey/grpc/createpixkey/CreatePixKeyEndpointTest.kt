package br.com.zupacademy.marciosouza.pixkey.grpc.createpixkey

import br.com.zupacademy.marciosouza.*
import br.com.zupacademy.marciosouza.pixkey.itauapi.ItauApiClient
import br.com.zupacademy.marciosouza.pixkey.itauapi.dto.AccountItauResponse
import br.com.zupacademy.marciosouza.pixkey.itauapi.dto.InstituicaoReponse
import br.com.zupacademy.marciosouza.pixkey.itauapi.dto.TitularResponse
import br.com.zupacademy.marciosouza.pixkey.repository.PixKeyRepository
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class CreatePixKeyEndpointTest {

    @Inject
    lateinit var pixGrpc: PixKeyServiceGrpc.PixKeyServiceBlockingStub

    @Inject
    lateinit var repository: PixKeyRepository

    @Inject
    lateinit var itauApi: ItauApiClient

    @BeforeEach
    fun setUp() {
        repository.deleteAll()
    }

    @MockBean(ItauApiClient::class)
    fun mockItauApiClient(): ItauApiClient?{
        return Mockito.mock(ItauApiClient::class.java)
    }

    @Factory
    class clientGrpc {
        @Singleton
        fun clientPix(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): PixKeyServiceGrpc.PixKeyServiceBlockingStub? {
            return PixKeyServiceGrpc.newBlockingStub(channel)
        }
    }

    val accountItauResponse = AccountItauResponse(
        "CONTA_CORRENTE",
        InstituicaoReponse("ITAÚ UNIBANCO S.A.", "60701190"),
        "0001", "291900",
        TitularResponse("c56dfef4-7901-44fb-84e2-a2cefb157890", "Rafael M C Ponte","02467781054"))

    @Test
    fun `Deve cadastrar uma chave pix do tipo CPF`() {
        val cpf = "04998330314"
        val request = KeyRequest
            .newBuilder()
            .setClienteId("0d1bb194-3c52-4e67-8c35-a93c0af9284f")
            .setTipoChave(TipoChave.CPF)
            .setChave(cpf)
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        Mockito
            .`when`(itauApi.getAccount(request.clienteId, request.tipoConta.name))
            .thenReturn(HttpResponse.ok(accountItauResponse))

        val response: KeyResponse = pixGrpc.create(request)
        with(response) {
            assertNotNull(pixId)
            assertTrue(repository.existsByKey(cpf))
        }
    }

    @Test
    fun `Deve cadastrar uma chave pix do tipo Telefone`(){
        val phone = "+5598984769646"
        val request = KeyRequest
            .newBuilder()
            .setClienteId("0d1bb194-3c52-4e67-8c35-a93c0af9284f")
            .setTipoChave(TipoChave.TELEFONE)
            .setChave(phone)
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        Mockito
            .`when`(itauApi.getAccount(request.clienteId, request.tipoConta.name))
            .thenReturn(HttpResponse.ok(accountItauResponse))

        val  response: KeyResponse = pixGrpc.create(request)
        with(response){
            assertNotNull(pixId)
            assertTrue(repository.existsByKey(phone))
        }
    }

    @Test
    fun `Deve cadastrar uma chave pix do tipo Email`(){
        val email = "marcio.souza@zup.com.br"
        val request = KeyRequest
            .newBuilder()
            .setClienteId("0d1bb194-3c52-4e67-8c35-a93c0af9284f")
            .setTipoChave(TipoChave.EMAIL)
            .setChave(email)
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        Mockito
            .`when`(itauApi.getAccount(request.clienteId, request.tipoConta.name))
            .thenReturn(HttpResponse.ok(accountItauResponse))

        val  response: KeyResponse = pixGrpc.create(request)
        with(response){
            assertNotNull(pixId)
            assertTrue(repository.existsByKey(email))
        }
    }

    @Test
    fun `Deve cadastrar uma chave pix do tipo Aleatoria`(){
        val request = KeyRequest
            .newBuilder()
            .setClienteId("0d1bb194-3c52-4e67-8c35-a93c0af9284f")
            .setTipoChave(TipoChave.ALEATORIA)
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        Mockito
            .`when`(itauApi.getAccount(request.clienteId, request.tipoConta.name))
            .thenReturn(HttpResponse.ok(accountItauResponse))

        val  response: KeyResponse = pixGrpc.create(request)
        with(response){
            assertNotNull(pixId)
        }
    }

}