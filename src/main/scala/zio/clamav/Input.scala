package zio.clamav

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8

import zio.stream.{ ZStream, ZTransducer }
import zio.{ Chunk, ZIO, ZRef }

sealed trait Input[-A] {
  private[clamav] def encode(data: A): ZStream[Any, Throwable, Byte]
}

object Input {
  type CommandInput = ZStream[Any, Throwable, Byte]

  case object NoInput extends Input[Unit] {
    def encode(data: Unit): CommandInput = ZStream.empty
  }

  case object StringInput extends Input[String] {
    def encode(data: String): CommandInput = ZStream.fromChunk(Chunk.fromArray(data.getBytes(UTF_8)))
  }

  case object LengthDelimitedStream extends Input[ZStream[Any, Throwable, Byte]] {
    def encode(data: ZStream[Any, Throwable, Byte]): CommandInput =
      ZStream.fromChunk(Chunk.fromArray("\u0000".getBytes)) concat data.transduce(prefixLength)
  }

  private[clamav] def lengthChunk(length: Int) =
    Chunk.fromArray(ByteBuffer.allocate(4).putInt(length).array())

  private[clamav] val prefixLength: ZTransducer[Any, Nothing, Byte, Byte] = ZTransducer {
    ZRef.makeManaged[Chunk[Byte]](Chunk.empty).map { stateRef =>
      {
        case None        =>
          stateRef.getAndSet(Chunk.empty).flatMap { leftOvers =>
            ZIO.succeedNow(lengthChunk(leftOvers.length) ++ leftOvers)
          }

        case Some(bytes) =>
          stateRef.modify { leftOvers =>
            val concat = leftOvers ++ bytes
            (lengthChunk(concat.length) ++ concat, Chunk.empty)
          }
      }
    }
  }

}
