package br.com.zupacademy.marciosouza.pixkey.grpc.createpixkey

import br.com.zupacademy.marciosouza.*
import br.com.zupacademy.marciosouza.pixkey.itauapi.ItauApiClient
import br.com.zupacademy.marciosouza.pixkey.itauapi.dto.AccountItauResponse
import br.com.zupacademy.marciosouza.pixkey.itauapi.dto.InstituicaoReponse
import br.com.zupacademy.marciosouza.pixkey.itauapi.dto.TitularResponse
import br.com.zupacademy.marciosouza.pixkey.repository.PixKeyRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.Assert.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.util.*
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
        return mock(ItauApiClient::class.java)
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
            .setClienteId(UUID.randomUUID().toString())
            .setTipoChave(TipoChave.CPF)
            .setChave(cpf)
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

            `when`(itauApi.getAccount(request.clienteId, request.tipoConta.name))
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
            .setClienteId(UUID.randomUUID().toString())
            .setTipoChave(TipoChave.TELEFONE)
            .setChave(phone)
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()


            `when`(itauApi.getAccount(request.clienteId, request.tipoConta.name))
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
            .setClienteId(UUID.randomUUID().toString())
            .setTipoChave(TipoChave.EMAIL)
            .setChave(email)
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()


            `when`(itauApi.getAccount(request.clienteId, request.tipoConta.name))
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
            .setClienteId(UUID.randomUUID().toString())
            .setTipoChave(TipoChave.ALEATORIA)
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

            `when`(itauApi.getAccount(request.clienteId, request.tipoConta.name))
                .thenReturn(HttpResponse.ok(accountItauResponse))

        val  response: KeyResponse = pixGrpc.create(request)
        with(response){
            assertNotNull(pixId)
            assertTrue(repository.findAll().isNotEmpty())
        }
    }

    @Test
    fun `Não deve cadastrar uma chave pix quando o cliente informado não for encontrado no sistema do Itau`(){
        val request = KeyRequest
            .newBuilder()
            .setClienteId(UUID.randomUUID().toString())
            .setTipoChave(TipoChave.ALEATORIA)
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        `when`(itauApi.getAccount(request.clienteId, request.tipoConta.name))
            .thenReturn(HttpResponse.notFound())

        val error = assertThrows<StatusRuntimeException>{
            pixGrpc.create(request)
        }

        with(error){
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("\${client.not.found}", status.description)
        }
    }

    @Test
    fun `Deve dar erro ao enviar o clientId em branco`(){
        val request = KeyRequest
            .newBuilder()
            .setClienteId("")
            .setTipoChave(TipoChave.EMAIL)
            .setChave("email@email.com.br")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        `when`(itauApi.getAccount(request.clienteId, request.tipoConta.name))
            .thenReturn(HttpResponse.ok(accountItauResponse))

        val error = assertThrows<StatusRuntimeException> {
            pixGrpc.create(request)
        }

        with(error){
            assertEquals("\${required.id.client}", status.description)
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    fun `Deve dar erro ao enviar um tipo de chave invalido`(){
        val request = KeyRequest
            .newBuilder()
            .setClienteId(UUID.randomUUID().toString())
            .setTipoChave(TipoChave.TIPO_CHAVE_DESCONHECIDA)
            .setChave("email@email.com.br")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        `when`(itauApi.getAccount(request.clienteId, request.tipoConta.name))
            .thenReturn(HttpResponse.ok(accountItauResponse))

        val error = assertThrows<StatusRuntimeException> {
            pixGrpc.create(request)
        }

        with(error){
            assertEquals("\${required.valid.typekey}", status.description)
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    fun `Deve dar erro ao enviar um tipo de conta invalido`(){
        val request = KeyRequest
            .newBuilder()
            .setClienteId(UUID.randomUUID().toString())
            .setTipoChave(TipoChave.EMAIL)
            .setChave("email@email.com.br")
            .setTipoConta(TipoConta.TIPO_CONTA_DESCONHECIDA)
            .build()

        `when`(itauApi.getAccount(request.clienteId, request.tipoConta.name))
            .thenReturn(HttpResponse.ok(accountItauResponse))

        val error = assertThrows<StatusRuntimeException> {
            pixGrpc.create(request)
        }

        with(error){
            assertEquals("\${required.valid.typeaccount}", status.description)
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }
}