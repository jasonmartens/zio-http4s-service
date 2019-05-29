package com.zendesk

import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}
import scalaz.zio.TaskR
import scalaz.zio.interop.catz._

final case class ZendeskService[R <: Serializable](rootUri: String) {
  type ZendeskTask[A] = TaskR[R, A]

  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[ZendeskTask, A] = jsonOf[ZendeskTask, A]
  implicit def circeJsonEncoder[A](implicit encoder: Encoder[A]): EntityEncoder[ZendeskTask, A] = jsonEncoderOf[ZendeskTask, A]

  val dsl: Http4sDsl[ZendeskTask] = Http4sDsl[ZendeskTask]
  import dsl._

  def service: HttpRoutes[ZendeskTask] = {
    HttpRoutes.of[ZendeskTask]{
      case GET -> Root =>
        Ok("Hello World")
    }
  }
}
