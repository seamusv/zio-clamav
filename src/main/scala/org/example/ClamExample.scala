package org.example

import java.io.File

import zio._
import zio.clamav._
import zio.stream.ZStream
object ClamExample extends App {

  private val clam = ClamExecutor.live("localhost", 3310)

  val app =
    for {
      _ <- ping().map(_.contentEquals("PONG")).flatMap(r => console.putStrLn(s"ping: $r"))
      _ <- version().flatMap(r => console.putStrLn(s"version: $r"))
      _ <- versionCommands().flatMap(r => console.putStrLn(s"versioncommands: $r"))
      _ <- stats().flatMap(r => console.putStrLn(s"stats: $r"))
      _ <- inStream(
             ZStream.fromChunk(
               Chunk.fromArray("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*".getBytes)
             )
           ).either.flatMap(r => console.putStrLn(s"instream: $r"))
      _ <- inStream(
             ZStream
               .fromFile(new File("/Users/svenasse/Downloads/Lens-3.4.0.dmg").toPath, chunkSize = 16384)
               .provideLayer(zio.blocking.Blocking.live)
           ).either.flatMap(r => console.putStrLn(s"instream: $r"))
      _ <-
        inStream(
          ZStream
            .fromFile(new File("/Users/svenasse/Downloads/wonder-woman-vector-logo-64D9232D37-seeklogo.com.zip").toPath)
            .provideLayer(zio.blocking.Blocking.live)
        ).flatMap(r => console.putStrLn(s"instream: $r"))
      _ <- scan("/etc").either.flatMap(r => console.putStrLn(s"scan: $r"))
      _ <- allMatchScan("/etc").either.flatMap(r => console.putStrLn(s"scan: $r"))
      _ <- contScan("/etc").either.flatMap(r => console.putStrLn(s"scan: $r"))
      _ <- multiScan("/root").either.flatMap(r => console.putStrLn(s"scan: $r"))
    } yield ()

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] =
    app
      .provideCustomLayer(clam)
      .foldM(
        err => console.putStrLn(s"Error: $err").as(ExitCode.failure),
        _ => ZIO.succeed(ExitCode.success)
      )
}
