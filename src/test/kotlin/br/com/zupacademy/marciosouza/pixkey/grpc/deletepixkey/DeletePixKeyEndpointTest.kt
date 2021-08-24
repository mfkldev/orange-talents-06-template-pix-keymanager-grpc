package br.com.zupacademy.marciosouza.pixkey.grpc.deletepixkey

import br.com.zupacademy.marciosouza.*
import br.com.zupacademy.marciosouza.pixkey.itauapi.ItauApiClient
import br.com.zupacademy.marciosouza.pixkey.messages.Messages
import br.com.zupacademy.marciosouza.pixkey.model.PixKeyModel
import br.com.zupacademy.marciosouza.pixkey.repository.PixKeyRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class DeletePixKeyEndpointTest(val messageApi: Messages){

    @Inject
    lateinit var delPixGrpc: DeletePixKeyServiceGrpc.DeletePixKeyServiceBlockingStub

    @Inject
    lateinit var repository: PixKeyRepository

    val genericCpf: String = "84141550060"

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
        fun clientPix(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): DeletePixKeyServiceGrpc.DeletePixKeyServiceBlockingStub? {
            return DeletePixKeyServiceGrpc.newBlockingStub(channel)
        }
    }

    @Test
    fun `Deve excluir uma chave pix`(){

        val pixKey = PixKeyModel(UUID.randomUUID(), TipoChave.CPF, genericCpf, TipoConta.CONTA_CORRENTE)
        repository.save(pixKey)

        val request = DelKeyRequest.newBuilder()
            .setClienteId(pixKey.clientId.toString())
            .setPixId(pixKey.pixId.toString())
            .build()

        val response = delPixGrpc.delete(request)

        with(response){
            assertEquals(pixKey.clientId.toString(), clienteId)
            assertEquals(pixKey.pixId.toString(), pixId)
            assertFalse(repository.existsByKey(pixKey.key))
        }
    }

    @Test
    fun `Deve dar erro ao tentar excluir uma chave pix que não existe`(){

        val request = DelKeyRequest.newBuilder()
            .setClienteId(UUID.randomUUID().toString())
            .setPixId(UUID.randomUUID().toString())
            .build()

        val error = assertThrows<StatusRuntimeException>{
            delPixGrpc.delete(request)
        }

        with(error){
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals(messageApi.pixkeyNotFound, status.description)
        }
    }

    @Test
    fun `Deve dar erro ao enviar id cliente mal formatado`(){

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
    fun `Deve dar erro ao enviar id pix mal formatado`(){

        val request = DelKeyRequest.newBuilder()
            .setClienteId(UUID.randomUUID().toString())
            .setPixId("NON-UUID")
            .build()

        val error = assertThrows<StatusRuntimeException>{
            delPixGrpc.delete(request)
        }

        with(error){
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("delete.keyId: ${messageApi.uuidBadFomart}", status.description)
        }
    }
}