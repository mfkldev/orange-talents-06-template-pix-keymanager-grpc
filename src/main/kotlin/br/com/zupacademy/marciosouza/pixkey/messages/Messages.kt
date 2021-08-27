package br.com.zupacademy.marciosouza.pixkey.messages

import io.micronaut.context.annotation.Value

class Messages {

    @Value("\${required.id.client}")
    lateinit var requiredIdClient: String

    @Value("\${required.valid.typekey}")
    lateinit var requiredValidTypeKey: String

    @Value("\${required.valid.typeaccount}")
    lateinit var requiredValidTypeAccount: String

    @Value("\${client.not.found}")
    lateinit var clientNotFound: String

    @Value("\${key.already.registered}")
    lateinit var keyAlreadyRegistered: String

    @Value("\${key.already.registered.bcb}")
    lateinit var keyAlreadyRegisteredBcb: String

    @Value("\${pixkey.notfound.this}")
    lateinit var pixkeyNotfoundThis: String

    @Value("\${pixkey.notfound.bcb}")
    lateinit var pixkeyNotFoundBcb: String

    @Value("\${uuid.bad.format}")
    lateinit var uuidBadFomart: String

    @Value("\${key.bigger.77}")
    lateinit var keyBigger77: String

    @Value("\${invalid.pix.format}")
    lateinit var invalidPixFormat: String

    @Value("\${unexpected.error}")
    lateinit var unexpectedError: String

    @Value("\${forbidden.operation.bcb}")
    lateinit var forbiddenOperationBcb: String
}