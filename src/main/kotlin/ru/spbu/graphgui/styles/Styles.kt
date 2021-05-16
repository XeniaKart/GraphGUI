package ru.spbu.graphgui.styles

import tornadofx.Stylesheet
import tornadofx.loadFont
import tornadofx.px

class Styles : Stylesheet() {
    companion object {
        private val jbmono = loadFont("/fonts/jb-mono-regular.ttf", 8)
    }

    init {
        root {
            jbmono?.let { font = it }
            fontSize = 14.px
        }
    }
}
