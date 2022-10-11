package com.rickinc.decibels.presentation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.rickinc.decibels.domain.UrlEncoder

sealed class Screen(
    private val id: String,
    private val argumentList: List<ScreenArg> = emptyList()
) {

    sealed class FullScreen(
        name: String,
        argumentList: List<ScreenArg> =
            emptyList()
    ) : Screen(name, argumentList) {

        object SongListScreen : FullScreen("SongsList")
    }

    init {
        values = values + this
    }

    fun getArguments(): List<NamedNavArgument> {
        return argumentList.map {
            navArgument(it.argName) {
                if (it.argType == ArgType.STRING)
                    type = NavType.StringType
            }
        }
    }

    fun route(): String {
        var route = this.id
        argumentList.forEach {
            route += "/" + "{" + it.argName + "}"
        }
        return route
    }

    fun buildRoute(arguments: List<String>): String {
        var route = id
        arguments.forEachIndexed { index, arg ->
            route += when (this.argumentList[index].argType) {
                ArgType.STRING -> {
                    "/$arg"
                }
                ArgType.URL -> "/${UrlEncoder().encode(arg)}"
            }
        }
        return route
    }

    companion object {
        var values = listOf<Screen>()
            private set

        fun getScreenFromRoute(route: String): Screen {
            val screen = values.find { it.route() == route }
            if (screen == null) throw IllegalArgumentException("couldn't find screen object for route $route")
            return screen
        }

        const val CONVERSATION_ID_ARG_KEY = "conversationID"
        const val CONVERSATION_NAME_ARG_KEY = "conversationName"
        const val CONVERSATION_PIC_URL_ARG_KEY = "conversationPic"
    }
}

data class ScreenArg(val argName: String, val argType: ArgType)
enum class ArgType {
    STRING, URL
}