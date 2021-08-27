package br.com.zupacademy.marciosouza.pixkey.grpc.deletepixkey

import br.com.zupacademy.marciosouza.*
import br.com.zupacademy.marciosouza.pixkey.client.bcbapi.BcbApiClient
import br.com.zupacademy.marciosouza.pixkey.client.bcbapi.dto.DeletePixKeyRequest
import br.com.zupacademy.marciosouza.pixkey.client.bcbapi.dto.DeletePixKeyResponse
import br.com.zupacademy.marciosouza.pixkey.client.itauapi.ItauApiClient
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
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
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

    val genericCpf: String = "84141550060"
    val genericDeletedAt: String = "2021-08-25T18:39:03.052Z"

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
        fun clientPix(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): DeletePixKeyServiceGrpc.DeletePixKeyServiceBlockingStub? {
            return DeletePixKeyServiceGrpc.newBlockingStub(channel)
        }
    }

    @Test
    fun Deve_excluir_uma_chave_pix(){
        val clientId = UUID.randomUUID()
        val associatedAccount = AssociatedAccount(
            TipoConta.CONTA_CORRENTE.toString(),
            "ITAÚ UNIBANCO S.A.", "60701190",
            "0001", "291900",
            clientId.toString(), "Rafael M C Ponte", "02467781054"
        )

        val pixKey = PixKeyModel(clientId, TipoChave.CPF, genericCpf, TipoConta.CONTA_CORRENTE, associatedAccount)
        repository.save(pixKey)

        val request = DelKeyRequest.newBuilder()
            .setClienteId(pixKey.clientId.toString())
            .setPixId(pixKey.pixId.toString())
            .build()

        `when`(bcbApi.deletePixKey(pixKey.key, DeletePixKeyRequest(pixKey.key, pixKey.associatedAccount.bankIspb)))
            .thenReturn(HttpResponse.ok(DeletePixKeyResponse(pixKey.key, pixKey.associatedAccount.bankIspb, genericDeletedAt)))

        val response = delPixGrpc.delete(request)

        with(response){
            assertEquals(pixKey.clientId.toString(), clienteId)
            assertEquals(pixKey.pixId.toString(), pixId)
            assertFalse(repository.existsByKey(pixKey.key))
        }
    }

    @Test
    fun Deve_dar_erro_ao_tentar_excluir_uma_chave_pix_que_nao_existe(){

        val request = DelKeyRequest.newBuilder()
            .setClienteId(UUID.randomUUID().toString())
            .setPixId(UUID.randomUUID().toString())
            .build()

        val error = assertThrows<StatusRuntimeException>{
            delPixGrpc.delete(request)
        }

        with(error){
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals(messageApi.pixkeyNotfoundThis, status.description)
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