package br.com.zupacademy.marciosouza.pixkey.client.bcbapi.dto

class DeletePixKeyResponse(
    val key : String,
    val participant : String,
    val deletedAt : String
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DeletePixKeyResponse

        if (key != other.key) return false
        if (participant != other.participant) return false
        if (deletedAt != other.deletedAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + participant.hashCode()
        result = 31 * result + deletedAt.hashCode()
        return result
    }
}