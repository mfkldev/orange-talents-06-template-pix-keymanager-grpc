package br.com.zupacademy.marciosouza.pixkey.grpc

import br.com.zupacademy.marciosouza.pixkey.exception.ExistingPixKey
import br.com.zupacademy.marciosouza.pixkey.itauapi.ItauApiClient
import br.com.zupacademy.marciosouza.pixkey.model.PixKeyModel
import br.com.zupacademy.marciosouza.pixkey.repository.PixKeyRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewKeyPixService(
    @Inject val pixKeyRepository: PixKeyRepository,
    @Inject val itauApiClient: ItauApiClient)
{
    fun register(pixKeyModel: PixKeyModel) : PixKeyModel{

        pixKeyRepository.existsByKey(pixKeyModel.key) && throw ExistingPixKey("Essa chave pix já está registrada")

        val accountsItauResponse = itauApiClient.getAccount(pixKeyModel.clientId.toString(), pixKeyModel.accountType.name)

        accountsItauResponse.body()?: throw IllegalStateException("Cliente não foi encontrado nos registros de clientes do Itau")

        pixKeyRepository.save(pixKeyModel)

        return pixKeyModel
    }
}
