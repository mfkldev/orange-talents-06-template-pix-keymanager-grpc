package br.com.zupacademy.marciosouza.pixkey.client.bcbapi.dto

class Owner(
    val type : TypeOwner,
    val name : String,
    val taxIdNumber : String
) {
    override fun toString(): String {
        return "Owner(type=$type, name='$name', taxIdNumber='$taxIdNumber')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Owner

        if (type != other.type) return false
        if (name != other.name) return false
        if (taxIdNumber != other.taxIdNumber) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + taxIdNumber.hashCode()
        return result
    }
}