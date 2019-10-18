package com.zendesk.endpoints

import com.zendesk.models.{DiagnosticResult, PingResponse}
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import zio.RIO
import zio.interop.catz._

final case class HealthEndpoint[R <: Serializable](rootUri: String) extends JsonSupport[R] {
  type HealthTask[A] = RIO[R, A]

  val dsl: Http4sDsl[HealthTask] = Http4sDsl[HealthTask]
  import dsl._

  def endpoints: HttpRoutes[HealthTask] = {
    HttpRoutes.of[HealthTask]{
      case GET -> Root / "z" / "ping" =>
        Ok(PingResponse("Ok"))
      case GET -> Root / "z" / "diagnostics" =>
        Ok(DiagnosticResult(success = true, "everything A-OK"))
    }
  }
}

