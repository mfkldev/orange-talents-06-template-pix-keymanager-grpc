package br.com.zupacademy.marciosouza.pixkey.grpc.createpixkey.dto

data class ProblemBcb(val type : String,
                      val status : Int,
                      val title : String,
                      val detail : String,
                      val violations : Violation){
    override fun toString(): String {
        return "ProblemBcb(type='$type', status=$status, title='$title', detail='$detail', field=${violations.field}, message=${violations.message})"
    }
}