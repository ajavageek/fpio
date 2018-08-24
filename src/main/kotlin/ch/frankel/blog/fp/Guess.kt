package ch.frankel.blog.fp

import arrow.core.*
import arrow.effects.*
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

private fun gameLoop(name: String?): IO<Unit> = ForIO extensions {
    binding {
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
        if (cont) gameLoop(name)
        else Unit.liftIO()
    }
    .flatten()
    .fix()
}

private fun checkContinue(name: String?): IO<Boolean> = ForIO extensions {
    binding {
        putStrLn("Do you want to continue, $name?").bind()
        val input = getStrLn().map { it?.toLowerCase() }.bind()
        when(input) {
            "y" -> true.liftIO()
            "n" -> false.liftIO()
            else -> checkContinue(name)
        }
    }
    .flatten()
    .fix()
}

private fun putStrLn(line: String): IO<Unit> = IO { println(line) }
private fun getStrLn(): IO<String?> = IO { readLine() }

private fun parseInt(input: String?) = Try {input?.toInt() }