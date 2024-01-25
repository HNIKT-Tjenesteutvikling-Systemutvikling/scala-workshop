package no.hnikt
package extensions

import cats.effect.Concurrent
import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.core.*
import org.http4s.Charset
import org.http4s.EntityDecoder
import org.http4s.EntityEncoder
import org.http4s.MediaType
import org.http4s.headers.`Content-Type`

object JsonCodec:
  given jsonEncoder[F[_], T](using JsonValueCodec[T]): EntityEncoder[F, T] =
    EntityEncoder
      .byteArrayEncoder[F]
      .contramap[T](writeToArray(_))
      .withContentType(
        `Content-Type`(MediaType.application.json, Some(Charset.`UTF-8`))
      )

  given jsonDecoder[F[_]: Concurrent, T](using
      JsonValueCodec[T]
  ): EntityDecoder[F, T] =
    EntityDecoder.byteArrayDecoder.map(bs => readFromArray(bs))
