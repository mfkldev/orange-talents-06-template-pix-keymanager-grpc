package br.com.zupacademy.marciosouza.pixkey.grpc.createpixkey

import br.com.zupacademy.marciosouza.*
import br.com.zupacademy.marciosouza.pixkey.client.bcbapi.BcbApiClient
import br.com.zupacademy.marciosouza.pixkey.client.bcbapi.dto.CreatePixKeyRequest
import br.com.zupacademy.marciosouza.pixkey.client.bcbapi.dto.CreatePixKeyResponse
import br.com.zupacademy.marciosouza.pixkey.client.itauapi.ItauApiClient
import br.com.zupacademy.marciosouza.pixkey.client.itauapi.dto.AccountItauResponse
import br.com.zupacademy.marciosouza.pixkey.client.itauapi.dto.InstituicaoReponse
import br.com.zupacademy.marciosouza.pixkey.client.itauapi.dto.TitularResponse
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
import org.junit.Assert.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class CreatePixKeyEndpointTest(val messageApi: Messages) {

    val genericEmail: String = "email@email.com.br"
    val genericCpf: String = "84141550060"
    val genericPhone: String = "+5598984000000"
    val genericCreatedAt: String = "2021-08-25T18:39:03.052Z"

    val LOG: Logger = LoggerFactory.getLogger(this::class.java)

    @Inject
    lateinit var pixGrpc: PixKeyServiceGrpc.PixKeyServiceBlockingStub

    @Inject
    lateinit var repository: PixKeyRepository

    @Inject
    lateinit var itauApi: ItauApiClient

    @Inject
    lateinit var bcbApi: BcbApiClient

    @BeforeEach
    fun setUp() {
        repository.deleteAll()
    }

    @MockBean(ItauApiClient::class)
    fun mockItauApiClient(): ItauApiClient? {
        return mock(ItauApiClient::class.java)
    }

    @MockBean(BcbApiClient::class)
    fun mockBcbApiClient(): BcbApiClient? {
        return mock(BcbApiClient::class.java)
    }

    @Factory
    class clientGrpc {
        @Singleton
        fun clientPix(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): PixKeyServiceGrpc.PixKeyServiceBlockingStub? {
            return PixKeyServiceGrpc.newBlockingStub(channel)
        }
    }

    fun createAccountItauResponse(request: KeyRequest) : AccountItauResponse{
        return AccountItauResponse(
            request.tipoConta.toString(),
            InstituicaoReponse("ITAÚ UNIBANCO S.A.", "60701190"),
            "0001", "291900",
            TitularResponse("c56dfef4-7901-44fb-84e2-a2cefb157890", "Rafael M C Ponte", "84141550060")
        )
    }

    fun genericAssociatedAccount(accountItauResponse: AccountItauResponse) : AssociatedAccount{
        return AssociatedAccount(
            accountItauResponse.tipo,
            accountItauResponse.instituicao.nome,
            accountItauResponse.instituicao.ispb,
            accountItauResponse.agencia,
            accountItauResponse.numero,
            accountItauResponse.titular.id,
            accountItauResponse.titular.nome,
            accountItauResponse.titular.cpf
        )
    }

    fun createPixKeyRequest(request: KeyRequest, accountItauResponse : AccountItauResponse) : CreatePixKeyRequest {
        return CreatePixKeyRequest.fromModel(
            PixKeyModel(
                UUID.fromString(request.clienteId),
                request.tipoChave,
                request.chave,
                request.tipoConta,
                genericAssociatedAccount(accountItauResponse)
            )
        )
    }

    fun createPixKeyResponse(createPixKeyRequest: CreatePixKeyRequest): CreatePixKeyResponse{
        return CreatePixKeyResponse(
            createPixKeyRequest.convertKeyType(),
            createPixKeyRequest.key,
            createPixKeyRequest.bankAccount,
            createPixKeyRequest.owner,
            genericCreatedAt
        )
    }

    @Test
    fun `Deve cadastrar uma chave pix do tipo CPF`() {
        val request = KeyRequest
            .newBuilder()
            .setClienteId(UUID.randomUUID().toString())
            .setTipoChave(TipoChave.CPF)
            .setChave(genericCpf)
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        val createAccountItauResponse = createAccountItauResponse(request)

        `when`(itauApi.getAccount(request.clienteId, request.tipoConta.name))
            .thenReturn(HttpResponse.ok(createAccountItauResponse))

        val createPixKeyRequest = createPixKeyRequest(request, createAccountItauResponse)

        `when`(bcbApi.postPixKey(createPixKeyRequest))
            .thenReturn(HttpResponse.ok(createPixKeyResponse(createPixKeyRequest)))

        val response: KeyResponse = pixGrpc.create(request)

        with(response) {
            assertNotNull(pixId)
            assertTrue(repository.existsByKey(genericCpf))
        }
    }

    @Test
    fun `Deve cadastrar uma chave pix do tipo Telefone`() {
        val request = KeyRequest
            .newBuilder()
            .setClienteId(UUID.randomUUID().toString())
            .setTipoChave(TipoChave.TELEFONE)
            .setChave(genericPhone)
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        val createAccountItauResponse = createAccountItauResponse(request)

        `when`(itauApi.getAccount(request.clienteId, request.tipoConta.name))
            .thenReturn(HttpResponse.ok(createAccountItauResponse))

        val createPixKeyRequest = createPixKeyRequest(request, createAccountItauResponse)

        `when`(bcbApi.postPixKey(createPixKeyRequest))
            .thenReturn(HttpResponse.ok(createPixKeyResponse(createPixKeyRequest)))

        val response: KeyResponse = pixGrpc.create(request)

        with(response) {
            assertNotNull(pixId)
            assertTrue(repository.existsByKey(genericPhone))
        }
    }

    @Test
    fun `Deve cadastrar uma chave pix do tipo Email`() {
        val request = KeyRequest
            .newBuilder()
            .setClienteId(UUID.randomUUID().toString())
            .setTipoChave(TipoChave.EMAIL)
            .setChave(genericEmail)
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        val createAccountItauResponse = createAccountItauResponse(request)

        `when`(itauApi.getAccount(request.clienteId, request.tipoConta.name))
            .thenReturn(HttpResponse.ok(createAccountItauResponse))

        val createPixKeyRequest = createPixKeyRequest(request, createAccountItauResponse)

        `when`(bcbApi.postPixKey(createPixKeyRequest))
            .thenReturn(HttpResponse.ok(createPixKeyResponse(createPixKeyRequest)))

        val response: KeyResponse = pixGrpc.create(request)
        with(response) {
            assertNotNull(pixId)
            assertTrue(repository.existsByKey(genericEmail))
        }
    }

    @Test
    fun `Deve cadastrar uma chave pix do tipo Aleatoria`() {
        val request = KeyRequest
            .newBuilder()
            .setClienteId(UUID.randomUUID().toString())
            .setTipoChave(TipoChave.ALEATORIA)
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        val createAccountItauResponse = createAccountItauResponse(request)

        `when`(itauApi.getAccount(request.clienteId, request.tipoConta.name))
            .thenReturn(HttpResponse.ok(createAccountItauResponse))

        val createPixKeyRequest = createPixKeyRequest(request, createAccountItauResponse)

        LOG.warn("CREATE PIX KEY REQUEST DO TEST -> ${createPixKeyRequest.toString()}")

        `when`(bcbApi.postPixKey(createPixKeyRequest))
            .thenReturn(HttpResponse.ok(createPixKeyResponse(createPixKeyRequest)))

        val response: KeyResponse = pixGrpc.create(request)
        with(response) {
            assertNotNull(pixId)
            assertTrue(repository.findAll().isNotEmpty())
        }
    }

    @Test
    fun `Nao deve cadastrar uma chave pix quando o cliente informado não for encontrado no sistema do Itau`() {
        val request = KeyRequest
            .newBuilder()
            .setClienteId(UUID.randomUUID().toString())
            .setTipoChave(TipoChave.ALEATORIA)
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        `when`(itauApi.getAccount(request.clienteId, request.tipoConta.name))
            .thenReturn(HttpResponse.notFound())

        val error = assertThrows<StatusRuntimeException> {
            pixGrpc.create(request)
        }

        with(error) {
            assertEquals(messageApi.clientNotFound, status.description)
            assertEquals(Status.NOT_FOUND.code, status.code)
        }
    }

    @Test
    fun `Deve dar erro ao enviar o clientId em branco`() {
        val request = KeyRequest
            .newBuilder()
            .setClienteId("")
            .setTipoChave(TipoChave.EMAIL)
            .setChave(genericEmail)
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        val createAccountItauResponse = createAccountItauResponse(request)

        `when`(itauApi.getAccount(request.clienteId, request.tipoConta.name))
            .thenReturn(HttpResponse.ok(createAccountItauResponse))

        val error = assertThrows<StatusRuntimeException> {
            pixGrpc.create(request)
        }

        with(error) {
            assertEquals(messageApi.requiredIdClient, status.description)
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    fun `Deve dar erro ao enviar um tipo de chave invalido`() {
        val request = KeyRequest
            .newBuilder()
            .setClienteId(UUID.randomUUID().toString())
            .setTipoChave(TipoChave.TIPO_CHAVE_DESCONHECIDA)
            .setChave(genericEmail)
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        val createAccountItauResponse = createAccountItauResponse(request)

        `when`(itauApi.getAccount(request.clienteId, request.tipoConta.name))
            .thenReturn(HttpResponse.ok(createAccountItauResponse))

        val error = assertThrows<StatusRuntimeException> {
            pixGrpc.create(request)
        }

        with(error) {
            assertEquals(messageApi.requiredValidTypeKey, status.description)
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    fun `Deve dar erro ao enviar um tipo de conta invalido`() {
        val request = KeyRequest
            .newBuilder()
            .setClienteId(UUID.randomUUID().toString())
            .setTipoChave(TipoChave.EMAIL)
            .setChave(genericEmail)
            .setTipoConta(TipoConta.TIPO_CONTA_DESCONHECIDA)
            .build()

        val createAccountItauResponse = createAccountItauResponse(request)

        `when`(itauApi.getAccount(request.clienteId, request.tipoConta.name))
            .thenReturn(HttpResponse.ok(createAccountItauResponse))

        val error = assertThrows<StatusRuntimeException> {
            pixGrpc.create(request)
        }

        with(error) {
            assertEquals(messageApi.requiredValidTypeAccount, status.description)
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    fun `Deve dar erro ao tentar cadastrar um chave que já foi cadastrada anteriormente`() {
        val clientId = UUID.randomUUID()
        val associatedAccount = AssociatedAccount(
            TipoConta.CONTA_CORRENTE.toString(),
            "ITAÚ UNIBANCO S.A.", "60701190",
            "0001", "291900",
            clientId.toString(), "Rafael M C Ponte", "02467781054"
        )

        val pixKeyModel =
            PixKeyModel(clientId, TipoChave.EMAIL, genericEmail, TipoConta.CONTA_CORRENTE, associatedAccount)
        repository.save(pixKeyModel)

        val request = KeyRequest
            .newBuilder()
            .setClienteId(UUID.randomUUID().toString())
            .setTipoChave(TipoChave.EMAIL)
            .setChave(genericEmail)
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        val createAccountItauResponse = createAccountItauResponse(request)

        `when`(itauApi.getAccount(request.clienteId, request.tipoConta.name))
            .thenReturn(HttpResponse.ok(createAccountItauResponse))

        val error = assertThrows<StatusRuntimeException> {
            pixGrpc.create(request)
        }

        with(error) {
            assertEquals(messageApi.keyAlreadyRegistered, status.description)
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
        }
    }

    @Test
    fun `Deve dar erro ao enviar CPF mal formatado`() {
        val request = KeyRequest
            .newBuilder()
            .setClienteId(UUID.randomUUID().toString())
            .setTipoChave(TipoChave.CPF)
            .setChave("1234567890")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        val createAccountItauResponse = createAccountItauResponse(request)

        `when`(itauApi.getAccount(request.clienteId, request.tipoConta.name))
            .thenReturn(HttpResponse.ok(createAccountItauResponse))

        val error = assertThrows<StatusRuntimeException> {
            pixGrpc.create(request)
        }

        with(error) {
            assertEquals(messageApi.invalidPixFormat, status.description)
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }


    @Test
    fun `Deve dar erro ao enviar email mal formatado`() {
        val request = KeyRequest
            .newBuilder()
            .setClienteId(UUID.randomUUID().toString())
            .setTipoChave(TipoChave.EMAIL)
            .setChave("email.com.br")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        val createAccountItauResponse = createAccountItauResponse(request)

        `when`(itauApi.getAccount(request.clienteId, request.tipoConta.name))
            .thenReturn(HttpResponse.ok(createAccountItauResponse))

        val error = assertThrows<StatusRuntimeException> {
            pixGrpc.create(request)
        }

        with(error) {
            assertEquals(messageApi.invalidPixFormat, status.description)
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }


    @Test
    fun `Deve dar erro ao enviar telefone mal formatado`() {
        val request = KeyRequest
            .newBuilder()
            .setClienteId(UUID.randomUUID().toString())
            .setTipoChave(TipoChave.TELEFONE)
            .setChave("98984000000")
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        val createAccountItauResponse = createAccountItauResponse(request)

        `when`(itauApi.getAccount(request.clienteId, request.tipoConta.name))
            .thenReturn(HttpResponse.ok(createAccountItauResponse))

        val error = assertThrows<StatusRuntimeException> {
            pixGrpc.create(request)
        }

        with(error) {
            assertEquals(messageApi.invalidPixFormat, status.description)
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }
}