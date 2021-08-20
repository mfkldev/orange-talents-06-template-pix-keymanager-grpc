package br.com.zupacademy.marciosouza.pixkey.model

enum class KeyType {

    TIPO_CHAVE_DESCONHECIDA{
        override fun valid(key: String?): Boolean {
            TODO("Not yet implemented")
        }
    },
    CPF{
        override fun valid(key: String?): Boolean {
            if(key.isNullOrBlank()){
                return false
            }
            return key.matches("^[0-9]{11}\$".toRegex())
        }
    },
    TELEFONE{
        override fun valid(key: String?): Boolean {
            if(key.isNullOrBlank()){
                return false
            }
            return key.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())
        }

    },
    EMAIL{
        override fun valid(key: String?): Boolean {
            if(key.isNullOrBlank()){
                return false
            }
            return key.matches("^[a-z0-9]+@[a-z0-9]+\\.[a-z]+\\.?([a-z]+)?\$".toRegex())
        }

    },
    ALEATORIA{
        override fun valid(key: String?) = key.isNullOrBlank()
    };

    abstract fun valid(key: String?): Boolean
}
