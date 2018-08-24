package ch.frankel.blog.fp

import arrow.core.Try
import arrow.effects.IO
import arrow.effects.fix
import arrow.effects.monad
import arrow.typeclasses.binding
import java.security.SecureRandom

private val random = SecureRandom()

fun main(args: Array<String>) {
    println("What is your name?")
    val name = readLine()
    println("Hello, $name, welcome to the game!")
    gameLoop(name).unsafeRunSync()
}

private fun gameLoop(name: String?): IO<Unit> = IO.monad().binding {
    val number = random.nextInt(5) + 1
    println("Dear $name, please guess a number from 1 to 5:")
    val input = readLine()
    parseInt(input).fold(
            { println("You did not enter a number!") },
            {
                if (it == number) println("You guessed right, $name!")
                else println("You guessed wrong, $name! The number was $number")
            }
    )
    val cont = checkContinue(name).bind()
    if (cont) gameLoop(name).bind()
    else Unit
}
.fix()


private fun checkContinue(name: String?): IO<Boolean> = IO.monad().binding {
    println("Do you want to continue, $name?")
    val input = readLine()?.trueMap { it.toLowerCase() }
    when (input) {
        "y" -> true
        "n" -> false
        else -> checkContinue(name).bind()
    }
}
.fix()

private fun String.trueMap(f: (String) -> String) = f(this)

private fun parseInt(input: String?) = Try { input?.toInt() }