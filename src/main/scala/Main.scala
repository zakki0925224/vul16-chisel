import core._
import circt.stage.ChiselStage

object Main extends App {
    ChiselStage.emitSystemVerilogFile(
        new Blinky(1000),
        args = Array("--target-dir", "output"),
        firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info")
    )
}
