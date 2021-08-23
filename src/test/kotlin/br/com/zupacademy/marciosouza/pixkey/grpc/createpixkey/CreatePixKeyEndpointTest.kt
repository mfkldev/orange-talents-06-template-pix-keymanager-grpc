package br.com.zupacademy.marciosouza.pixkey.grpc.createpixkey

import br.com.zupacademy.marciosouza.*
import br.com.zupacademy.marciosouza.pixkey.itauapi.ItauApiClient
import br.com.zupacademy.marciosouza.pixkey.itauapi.dto.AccountItauResponse
import br.com.zupacademy.marciosouza.pixkey.itauapi.dto.InstituicaoReponse
import br.com.zupacademy.marciosouza.pixkey.itauapi.dto.TitularResponse
import br.com.zupacademy.marciosouza.pixkey.messages.Messages
import br.com.zupacademy.marciosouza.pixkey.model.PixKeyModel
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
internal class CreatePixKeyEndpointTest(val messageApi: Messages) {

    val genericEmail: String = "email@email.com.br"
    val genericCpf: String = "84141550060"
    val genericPhone: String = "+5598984000000"

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
        val request = KeyRequest
            .newBuilder()
            .setClienteId(UUID.randomUUID().toString())
            .setTipoChave(TipoChave.CPF)
            .setChave(genericCpf)
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

            `when`(itauApi.getAccount(request.clienteId, request.tipoConta.name))
            .thenReturn(HttpResponse.ok(accountItauResponse))

        val response: KeyResponse = pixGrpc.create(request)
        with(response) {
            assertNotNull(pixId)
            assertTrue(repository.existsByKey(genericCpf))
        }
    }

    @Test
    fun `Deve cadastrar uma chave pix do tipo Telefone`(){
        val request = KeyRequest
            .newBuilder()
            .setClienteId(UUID.randomUUID().toString())
            .setTipoChave(TipoChave.TELEFONE)
            .setChave(genericPhone)
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()


            `when`(itauApi.getAccount(request.clienteId, request.tipoConta.name))
                .thenReturn(HttpResponse.ok(accountItauResponse))

        val  response: KeyResponse = pixGrpc.create(request)
        with(response){
            assertNotNull(pixId)
            assertTrue(repository.existsByKey(genericPhone))
        }
    }

    @Test
    fun `Deve cadastrar uma chave pix do tipo Email`(){
        val request = KeyRequest
            .newBuilder()
            .setClienteId(UUID.randomUUID().toString())
            .setTipoChave(TipoChave.EMAIL)
            .setChave(genericEmail)
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()


            `when`(itauApi.getAccount(request.clienteId, request.tipoConta.name))
                .thenReturn(HttpResponse.ok(accountItauResponse))

        val  response: KeyResponse = pixGrpc.create(request)
        with(response){
            assertNotNull(pixId)
            assertTrue(repository.existsByKey(genericEmail))
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
    fun `Nao deve cadastrar uma chave pix quando o cliente informado não for encontrado no sistema do Itau`(){
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
            assertEquals(messageApi.clientNotFound, status.description)
            assertEquals(Status.NOT_FOUND.code, status.code)
        }
    }

    @Test
    fun `Deve dar erro ao enviar o clientId em branco`(){
        val request = KeyRequest
            .newBuilder()
            .setClienteId("")
            .setTipoChave(TipoChave.EMAIL)
            .setChave(genericEmail)
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        `when`(itauApi.getAccount(request.clienteId, request.tipoConta.name))
            .thenReturn(HttpResponse.ok(accountItauResponse))

        val error = assertThrows<StatusRuntimeException> {
            pixGrpc.create(request)
        }

        with(error){
            assertEquals(messageApi.requiredIdClient, status.description)
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    fun `Deve dar erro ao enviar um tipo de chave invalido`(){
        val request = KeyRequest
            .newBuilder()
            .setClienteId(UUID.randomUUID().toString())
            .setTipoChave(TipoChave.TIPO_CHAVE_DESCONHECIDA)
            .setChave(genericEmail)
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        `when`(itauApi.getAccount(request.clienteId, request.tipoConta.name))
            .thenReturn(HttpResponse.ok(accountItauResponse))

        val error = assertThrows<StatusRuntimeException> {
            pixGrpc.create(request)
        }

        with(error){
            assertEquals(messageApi.requiredValidTypeKey, status.description)
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    fun `Deve dar erro ao enviar um tipo de conta invalido`(){
        val request = KeyRequest
            .newBuilder()
            .setClienteId(UUID.randomUUID().toString())
            .setTipoChave(TipoChave.EMAIL)
            .setChave(genericEmail)
            .setTipoConta(TipoConta.TIPO_CONTA_DESCONHECIDA)
            .build()

        `when`(itauApi.getAccount(request.clienteId, request.tipoConta.name))
            .thenReturn(HttpResponse.ok(accountItauResponse))

        val error = assertThrows<StatusRuntimeException> {
            pixGrpc.create(request)
        }

        with(error){
            assertEquals(messageApi.requiredValidTypeAccount, status.description)
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    fun `Deve dar erro ao tentar cadastrar um chave que já foi cadastrada anteriormente`(){

        val pixKeyModel = PixKeyModel(UUID.randomUUID(), TipoChave.EMAIL, genericEmail, TipoConta.CONTA_CORRENTE)
        repository.save(pixKeyModel)

        val request = KeyRequest
            .newBuilder()
            .setClienteId(UUID.randomUUID().toString())
            .setTipoChave(TipoChave.EMAIL)
            .setChave(genericEmail)
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        `when`(itauApi.getAccount(request.clienteId, request.tipoConta.name))
            .thenReturn(HttpResponse.ok(accountItauResponse))

        val error = assertThrows<StatusRuntimeException> {
            pixGrpc.create(request)
        }

        with(error){
            assertEquals(messageApi.keyAlreadyRegistered, status.description)
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
        }
    }

    @Test
    fun `Deve dar erro ao enviar CPF mal formatado`(){
        val request = KeyRequest
            .newBuilder()
            .setClienteId(UUID.randomUUID().toString())
            .setTipoChave(TipoChave.CPF)
            .setChave("1234567890")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        `when`(itauApi.getAccount(request.clienteId, request.tipoConta.name))
            .thenReturn(HttpResponse.ok(accountItauResponse))

        val error = assertThrows<StatusRuntimeException> {
            pixGrpc.create(request)
        }

        with(error){
            assertEquals("save.entity: formato da chave Pix não é válido", status.description)
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }


    @Test
    fun `Deve dar erro ao enviar email mal formatado`(){
        val request = KeyRequest
            .newBuilder()
            .setClienteId(UUID.randomUUID().toString())
            .setTipoChave(TipoChave.EMAIL)
            .setChave("email.com.br")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        `when`(itauApi.getAccount(request.clienteId, request.tipoConta.name))
            .thenReturn(HttpResponse.ok(accountItauResponse))

        val error = assertThrows<StatusRuntimeException> {
            pixGrpc.create(request)
        }

        with(error){
            assertEquals("save.entity: formato da chave Pix não é válido", status.description)
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }


    @Test
    fun `Deve dar erro ao enviar telefone mal formatado`(){
        val request = KeyRequest
            .newBuilder()
            .setClienteId(UUID.randomUUID().toString())
            .setTipoChave(TipoChave.TELEFONE)
            .setChave("98984000000")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        `when`(itauApi.getAccount(request.clienteId, request.tipoConta.name))
            .thenReturn(HttpResponse.ok(accountItauResponse))

        val error = assertThrows<StatusRuntimeException> {
            pixGrpc.create(request)
        }

        with(error){
            assertEquals("save.entity: formato da chave Pix não é válido", status.description)
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }
}