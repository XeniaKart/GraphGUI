package ru.spbu.graphgui.styles

//import javafx.scene.paint.Color
import tornadofx.Stylesheet
//import tornadofx.cssclass
import tornadofx.loadFont
import tornadofx.px
//import tornadofx.box

class Styles : Stylesheet() {
    companion object {
        private val jbmono = loadFont("/fonts/jb-mono-regular.ttf", 8)
//        val critical by cssclass()
    }

    init {
        root {
            jbmono?.let { font = it }
            fontSize = 14.px
        }
//        critical {
//            borderColor += box(Color.ORANGE)
//            padding = box(5.px)
//            button {
//                backgroundColor += Color.RED
//                textFill = Color.WHITE
//            }
//        }
    }
}
