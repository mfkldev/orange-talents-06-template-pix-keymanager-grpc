package br.com.zupacademy.marciosouza

import io.micronaut.runtime.Micronaut.*
fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("br.com.zupacademy.marciosouza")
		.start()
}

