package ch.frankel.blog.fp

import arrow.core.Try
import arrow.effects.IO
import arrow.effects.fix
import arrow.effects.liftIO
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
    println("Dear $name, please guess a number from 1 to 5:")
    (readLine() as String).safeToInt().fold(
            { println("You did not enter a number!") },
            {
                val number = random.nextInt(5) + 1
                if (it == number) println("You guessed right, $name!")
                else println("You guessed wrong, $name! The number was $number")
            }
    )
    checkContinue(name).map {
        (if (it) gameLoop(name)
        else Unit.liftIO())
    }.flatten()
     .bind()
}.fix()


private fun checkContinue(name: String?): IO<Boolean> = IO.monad().binding {
    println("Do you want to continue, $name?")
    (readLine() as String).trueMap { it.toLowerCase() }.trueMap {
        when (it) {
            "y" -> true.liftIO()
            "n" -> false.liftIO()
            else -> checkContinue(name)
        }
    }.bind()
}.fix()

private fun <T> String.trueMap(f: (String) -> T) = f(this)

private fun String.safeToInt() = Try { this.toInt() }