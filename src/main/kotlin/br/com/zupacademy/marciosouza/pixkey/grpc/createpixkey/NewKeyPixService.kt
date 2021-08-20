package br.com.zupacademy.marciosouza.pixkey.grpc.createpixkey

import br.com.zupacademy.marciosouza.pixkey.exception.ExistingPixKeyException
import br.com.zupacademy.marciosouza.pixkey.itauapi.ItauApiClient
import br.com.zupacademy.marciosouza.pixkey.model.PixKeyModel
import br.com.zupacademy.marciosouza.pixkey.repository.PixKeyRepository
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton

@Validated
@Singleton
class NewKeyPixService(
    @Inject val pixKeyRepository: PixKeyRepository,
    @Inject val itauApiClient: ItauApiClient)
{
    fun register(pixKeyModel: PixKeyModel) : PixKeyModel{

        pixKeyRepository.existsByKey(pixKeyModel.key) && throw ExistingPixKeyException("Essa chave pix já está registrada")

        val accountsItauResponse = itauApiClient.getAccount(pixKeyModel.clientId.toString(), pixKeyModel.accountType.name)

        accountsItauResponse.body()?: throw IllegalStateException("Cliente não foi encontrado nos registros de clientes do Itau")

        pixKeyRepository.save(pixKeyModel)

        return pixKeyModel
    }
}
