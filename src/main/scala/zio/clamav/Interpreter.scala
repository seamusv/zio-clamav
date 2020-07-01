package zio.clamav

import java.io.IOException

import zio._
import zio.duration._
import zio.nio.channels.AsynchronousSocketChannel
import zio.nio.core.SocketAddress
import zio.stream.ZStream
import zio.stream.ZTransducer._

trait Interpreter {

  type ClamExecutor = Has[ClamExecutor.Service]

  object ClamExecutor {
    trait Service {
      def execute(input: ZStream[Any, Throwable, Byte]): ZIO[Any, ClamError, String]
    }

    def live(host: String, port: Int): Layer[IOException, ClamExecutor] = {
      val client = AsynchronousSocketChannel().mapM { client =>
        for {
          address <- SocketAddress.inetSocketAddress(host, port)
          _       <- client.connect(address)
        } yield client
      }.refineToOrDie[IOException]

      ZLayer.succeed(new Service {
        override def execute(input: ZStream[Any, Throwable, Byte]): ZIO[Any, ClamError, String] =
          client.use { channel =>
            ZStream
              .repeatEffectChunk(channel.read(4096, 30.seconds))
              .transduce(utf8Decode >>> splitOn("\u0000"))
              .take(1)
              .drainFork(ZStream.fromEffect(input.foreachChunk(channel.write) *> ZIO.never))
              .runHead
              .someOrFailException
          }.refineToOrDie[Exception]
            .refineOrDie(ClamError.asProtocolError)
      })
    }
  }

}
