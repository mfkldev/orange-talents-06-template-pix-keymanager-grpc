package br.com.zupacademy.marciosouza.pixkey.validation

import java.util.*

fun String.stringIsNotUUID() : Boolean{
    return try{
        UUID.fromString(this)
        false
    }catch (exception : IllegalArgumentException){
        true
    }
}