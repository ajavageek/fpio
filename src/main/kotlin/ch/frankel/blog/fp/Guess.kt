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
    mainIO(args).unsafeRunSync()
}

private fun mainIO(args: Array<String>): IO<Unit> = IO.monad().binding {
    putStrLn("What is your name?").bind()
    val name = getStrLn().bind()
    putStrLn("Hello, $name, welcome to the game!").bind()
    gameLoop(name).bind()
}.fix()

private fun gameLoop(name: String?): IO<Unit> = IO.monad().binding {
    putStrLn("Dear $name, please guess a number from 1 to 5:").bind()
    getStrLn().safeToInt().fold(
            { putStrLn("You did not enter a number!").bind() },
            {
                val number = nextInt(5).map { it + 1 }.bind()
                if (it.bind() == number) println("You guessed right, $name!")
                else putStrLn("You guessed wrong, $name! The number was $number").bind()
            }
    )
    checkContinue(name).map {
        (if (it) gameLoop(name)
        else Unit.liftIO())
    }.flatten()
     .bind()
}.fix()


private fun checkContinue(name: String?): IO<Boolean> = IO.monad().binding {
    putStrLn("Do you want to continue, $name?").bind()
    (getStrLn()).map { it.toLowerCase() }.map {
        when (it) {
            "y" -> true.liftIO()
            "n" -> false.liftIO()
            else -> checkContinue(name)
        }
    }.flatten()
    .bind()
}.fix()

private fun putStrLn(line: String): IO<Unit> = IO { println(line) }
private fun getStrLn(): IO<String> = IO { readLine() as String }

private fun nextInt(upper: Int): IO<Int> = IO { random.nextInt(upper) }
private fun IO<String>.safeToInt() = Try { map { it.toInt() }}