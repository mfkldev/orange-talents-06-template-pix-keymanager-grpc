package br.com.zupacademy.marciosouza.pixkey.grpc.deletepixkey

import br.com.zupacademy.marciosouza.*
import br.com.zupacademy.marciosouza.pixkey.client.bcbapi.BcbApiClient
import br.com.zupacademy.marciosouza.pixkey.client.bcbapi.dto.DeletePixKeyRequest
import br.com.zupacademy.marciosouza.pixkey.client.bcbapi.dto.DeletePixKeyResponse
import br.com.zupacademy.marciosouza.pixkey.messages.Messages
import br.com.zupacademy.marciosouza.pixkey.model.AssociatedAccount
import br.com.zupacademy.marciosouza.pixkey.model.PixKeyModel
import br.com.zupacademy.marciosouza.pixkey.repository.PixKeyRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpResponse.status
import io.micronaut.http.HttpResponseFactory
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class DeletePixKeyEndpointTest(val messageApi: Messages){

    @Inject
    lateinit var delPixGrpc: DeletePixKeyServiceGrpc.DeletePixKeyServiceBlockingStub

    @Inject
    lateinit var repository: PixKeyRepository

    @Inject
    lateinit var bcbApi: BcbApiClient

    val LOG: Logger = LoggerFactory.getLogger(this::class.java)

    val genericCpf: String = "84141550060"
    val genericDeletedAt: String = "2021-08-25T18:39:03.052Z"
    val genericClientId = UUID.randomUUID()
    val genericPixKey = UUID.randomUUID().toString()
    val genericAssociatedAccount = AssociatedAccount(
        TipoConta.CONTA_CORRENTE.toString(),
        "ITAÚ UNIBANCO S.A.", "60701190",
        "0001", "291900",
        genericClientId.toString(), "Rafael M C Ponte", "02467781054"
    )
    val genericRequest = DelKeyRequest.newBuilder()
        .setClienteId(genericClientId.toString())
        .setPixId(genericPixKey)
        .build()
    val genericPixKeyModel = PixKeyModel(
        genericClientId, TipoChave.CPF, genericCpf, TipoConta.CONTA_CORRENTE,
        genericAssociatedAccount
    )

    @BeforeEach
    fun setUp() {
        repository.deleteAll()
    }

    @MockBean(BcbApiClient::class)
    fun mockBcbApiClient(): BcbApiClient? {
        return Mockito.mock(BcbApiClient::class.java)
    }

    @Factory
    class clientGrpc {
        @Singleton
        fun clientPix(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                DeletePixKeyServiceGrpc.DeletePixKeyServiceBlockingStub?
        {
            return DeletePixKeyServiceGrpc.newBlockingStub(channel)
        }
    }

    @Test
    fun Deve_excluir_uma_chave_pix(){
        repository.save(genericPixKeyModel)

        val request = DelKeyRequest.newBuilder()
            .setClienteId(genericPixKeyModel.clientId.toString())
            .setPixId(genericPixKeyModel.pixId.toString())
            .build()

        `when`(bcbApi.deletePixKey(genericPixKeyModel.key,
            DeletePixKeyRequest(genericPixKeyModel.key, genericPixKeyModel.associatedAccount.bankIspb)))
            .thenReturn(HttpResponse.ok(
                DeletePixKeyResponse(genericPixKeyModel.key,
                    genericPixKeyModel.associatedAccount.bankIspb, genericDeletedAt)))

        val response = delPixGrpc.delete(request)

        with(response){
            assertEquals(genericPixKeyModel.clientId.toString(), clienteId)
            assertEquals(genericPixKeyModel.pixId.toString(), pixId)
            assertFalse(repository.existsByKey(genericPixKeyModel.key))
        }
    }

    @Test
    fun Deve_dar_erro_ao_tentar_excluir_uma_chave_pix_que_nao_existe_nesta_api(){

        val error = assertThrows<StatusRuntimeException>{
            delPixGrpc.delete(genericRequest)
        }

        with(error){
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals(messageApi.pixkeyNotfoundThis, status.description)
        }
    }

    @Test
    fun Deve_dar_erro_ao_tentar_excluir_uma_chave_pix_que_nao_existe_na_api_BCB(){
        repository.save(genericPixKeyModel)

        `when`(bcbApi.deletePixKey(genericPixKeyModel.key, DeletePixKeyRequest(genericPixKeyModel.key,
            genericPixKeyModel.associatedAccount.bankIspb)))
            .thenReturn(HttpResponseFactory.INSTANCE.status(HttpStatus.NOT_FOUND))

        val error = assertThrows<StatusRuntimeException>{
            delPixGrpc.delete(DelKeyRequest.newBuilder()
                .setClienteId(genericPixKeyModel.clientId.toString())
                .setPixId(genericPixKeyModel.pixId.toString())
                .build())
        }

        with(error){
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals(messageApi.pixkeyNotFoundBcb, status.description)
        }
    }

    @Test
    fun Deve_dar_erro_quando_o_acesso_a_BCB_Api_for_negado(){
        repository.save(genericPixKeyModel)

        `when`(bcbApi.deletePixKey(genericPixKeyModel.key, DeletePixKeyRequest(genericPixKeyModel.key,
            genericPixKeyModel.associatedAccount.bankIspb)))
            .thenThrow(HttpClientResponseException("erro", status<Any?>(HttpStatus.FORBIDDEN)))

        val error = assertThrows<StatusRuntimeException>{
            delPixGrpc.delete(DelKeyRequest.newBuilder()
                .setClienteId(genericPixKeyModel.clientId.toString())
                .setPixId(genericPixKeyModel.pixId.toString())
                .build())
        }

        with(error){
            assertEquals(Status.PERMISSION_DENIED.code, status.code)
            assertEquals(messageApi.forbiddenOperationBcb, status.description)
        }
    }

    @Test
    fun Deve_dar_erro_quando_a_api_BCB_lancar_exception_nao_esperada(){
        repository.save(genericPixKeyModel)

        `when`(bcbApi.deletePixKey(genericPixKeyModel.key, DeletePixKeyRequest(genericPixKeyModel.key,
            genericPixKeyModel.associatedAccount.bankIspb)))
            .thenThrow(HttpClientResponseException("erro", status<Any?>(HttpStatus.I_AM_A_TEAPOT)))

        val error = assertThrows<StatusRuntimeException>{
            delPixGrpc.delete(DelKeyRequest.newBuilder()
                .setClienteId(genericPixKeyModel.clientId.toString())
                .setPixId(genericPixKeyModel.pixId.toString())
                .build())
        }

        with(error){
            assertEquals(Status.INTERNAL.code, status.code)
            assertEquals(messageApi.unexpectedError, status.description)
        }
    }

    @Test
    fun Deve_dar_erro_ao_enviar_id_cliente_mal_formatado(){

        val request = DelKeyRequest.newBuilder()
            .setClienteId("NON-UUID")
            .setPixId(UUID.randomUUID().toString())
            .build()

        val error = assertThrows<StatusRuntimeException>{
            delPixGrpc.delete(request)
        }

        with(error){
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("delete.clientId: ${messageApi.uuidBadFomart}", status.description)
        }
    }

    @Test
    fun Deve_dar_erro_ao_enviar_id_pix_mal_formatado(){

        val request = DelKeyRequest.newBuilder()
            .setClienteId(UUID.randomUUID().toString())
            .setPixId("NON-UUID")
            .build()

        val error = assertThrows<StatusRuntimeException>{
            delPixGrpc.delete(request)
        }

        with(error){
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("delete.pixId: ${messageApi.uuidBadFomart}", status.description)
        }
    }
}