package zio.clamav

sealed trait Output[+A] {
  private[clamav] def decode(text: String): Either[ClamError, A]
}

object Output {
  case object ResultOutput extends Output[Unit] {
    override private[clamav] def decode(text: String) =
      text match {
        case s if s.endsWith(" FOUND") =>
          Left(ClamError.VirusFound(s.replaceFirst("^stream: ", "").replaceFirst(" FOUND$", "")))
        case s if s.endsWith(" ERROR") => Left(ClamError.ProtocolError(s))
        case _                         => Right(())
      }
  }

  case object StringOutput extends Output[String] {
    def decode(text: String): Either[ClamError, String] =
      text match {
        case s if s.startsWith("INSTREAM size limit exceeded.") => Left(ClamError.SizeLimitExceeded(s))
        case s                                                  => Right(s)
      }
  }

  case object UnitOutput extends Output[Unit] {
    def decode(text: String): Either[ClamError, Unit] = Right(())
  }

}
