package zio.clamav

import zio.ZIO

final case class Command[-In, +Out] private (name: String, input: Input[In], output: Output[Out]) {
  private[clamav] def run(in: In): ZIO[ClamExecutor, ClamError, Out] =
    ZIO
      .accessM[ClamExecutor](_.get.execute(Input.StringInput.encode(s"z$name") concat input.encode(in) concat Input.StringInput.encode("\u0000")))
      .map(output.decode)
      .absolve
}

object Command {
  private[clamav] def apply[In, Out](name: String, input: Input[In], output: Output[Out]): Command[In, Out] =
    new Command(name, input, output)

  implicit final class Arg0[+Out](private val command: Command[Unit, Out]) extends AnyVal {
    def apply(): ZIO[ClamExecutor, ClamError, Out] = command.run(())
  }

  implicit final class Arg1[-A, +Out](private val command: Command[A, Out]) extends AnyVal {
    def apply(a: A): ZIO[ClamExecutor, ClamError, Out] = command.run(a)
  }


}
