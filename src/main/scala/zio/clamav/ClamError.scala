package zio.clamav

import java.nio.channels.InterruptedByTimeoutException

sealed trait ClamError

object ClamError {
  final case class ProtocolError(message: String)     extends ClamError
  final case class SizeLimitExceeded(message: String) extends ClamError
  final case class WrongType(message: String)         extends ClamError
  final case class VirusFound(result: String)         extends ClamError

  val asProtocolError: PartialFunction[Throwable, ClamError] = {
    case _: InterruptedByTimeoutException => ProtocolError("Network timeout")
    case t: Throwable                     => ProtocolError(t.getMessage)
  }
}
