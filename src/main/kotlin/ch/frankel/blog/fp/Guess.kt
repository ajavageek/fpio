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

private fun nextInt(upper: Int): IO<Int> = IO { random.nextInt(upper) }

private fun gameLoop(name: String?): IO<Unit> = IO.monad().binding {
    val number = nextInt(5).map { it + 1 }.bind()
    putStrLn("Dear $name, please guess a number from 1 to 5:").bind()
    val input = getStrLn().bind()
    parseInt(input).fold(
            { putStrLn("You did not enter a number!").bind() },
            {
                if (it == number) putStrLn("You guessed right, $name!").bind()
                else putStrLn("You guessed wrong, $name! The number was $number").bind()
            }
    )
    val cont = checkContinue(name).bind()
    if (cont) gameLoop(name).bind()
    else Unit
}
.fix()


private fun checkContinue(name: String?): IO<Boolean> = IO.monad().binding {
    putStrLn("Do you want to continue, $name?").bind()
    val input = getStrLn().map { it?.toLowerCase() }.bind()
    when (input) {
        "y" -> true
        "n" -> false
        else -> checkContinue(name).bind()
    }
}
.fix()

private fun putStrLn(line: String): IO<Unit> = IO { println(line) }
private fun getStrLn(): IO<String?> = IO { readLine() }

private fun parseInt(input: String?) = Try {input?.toInt() }