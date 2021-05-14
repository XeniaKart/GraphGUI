import javafx.stage.Stage
import styles.Styles
import tornadofx.App
import tornadofx.launch
import view.MainView

class MainApp : App(MainView:: class, Styles::class) {
    override fun start(stage: Stage) {
//        val btn = Button()
//        btn.text="hello"
        stage.width = 800.0
        stage.height = 800.0

        super.start(stage)

    }
}

fun main(args: Array<String>) {
    launch<MainApp>(args)
}