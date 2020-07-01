package zio.clamav.api

import zio.clamav.Command
import zio.clamav.Input._
import zio.clamav.Output.{ResultOutput, StringOutput}

trait Ops {

  final val allMatchScan    = Command("ALLMATCHSCAN", StringInput, ResultOutput)
  final val contScan        = Command("CONTSCAN", StringInput, ResultOutput)
  final val inStream        = Command("INSTREAM", LengthDelimitedStream, ResultOutput)
  final val ping            = Command("PING", NoInput, StringOutput)
  final val reload          = Command("RELOAD", NoInput, StringOutput)
  final val shutdown        = Command("SHUTDOWN", NoInput, StringOutput)
  final val multiScan       = Command("MULTISCAN", StringInput, ResultOutput)
  final val scan            = Command("SCAN", StringInput, ResultOutput)
  final val stats           = Command("STATS", NoInput, StringOutput)
  final val version         = Command("VERSION", NoInput, StringOutput)
  final val versionCommands = Command("VERSIONCOMMANDS", NoInput, StringOutput)

}
