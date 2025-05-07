import core._
import circt.stage.ChiselStage

object Main extends App {
    ChiselStage.emitSystemVerilogFile(
        new Top(),
        args = Array("--target-dir", "output"),
        firtoolOpts = Array(
            "-disable-all-randomization",
            "-strip-debug-info"
        )
    )
}
