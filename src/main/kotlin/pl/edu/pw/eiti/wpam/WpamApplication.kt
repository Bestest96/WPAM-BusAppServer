package pl.edu.pw.eiti.wpam

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WpamApplication

fun main(args: Array<String>) {
	runApplication<WpamApplication>(*args)
}
