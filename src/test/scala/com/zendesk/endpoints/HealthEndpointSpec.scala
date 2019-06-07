package com.zendesk.endpoints

import com.zendesk.HTTPSpec
import com.zendesk.models.{DiagnosticResult, PingResponse}
import io.circe.generic.auto._
import org.http4s.implicits._
import org.http4s.{Status, _}
import scalaz.zio._
import scalaz.zio.interop.catz._

class HealthEndpointSpec extends HTTPSpec {
  import HealthEndpointSpec._
  import HealthEndpointSpec.zendeskService._

  private val app = zendeskService.endpoints().orNotFound

  describe("ZendeskService") {
    it("should respond to /z/ping") {
      val req = request[HealthTask](Method.GET, "/z/ping")
      runWithEnv(check(app.run(req), Status.Ok, Some(PingResponse("Ok"))))
    }

    it("should respond to /z/diagnostics") {
      val req = request[HealthTask](Method.GET, "/z/diagnostics")
      runWithEnv(check(app.run(req), Status.Ok, Some(DiagnosticResult(success = true, "everything A-OK"))))
    }
  }
}

object HealthEndpointSpec extends DefaultRuntime {
  val zendeskService: HealthEndpoint[Serializable] = HealthEndpoint("/")
  case object TestEnv extends Serializable

  val mkEnv: UIO[Serializable] = UIO(TestEnv)

  def runWithEnv[E, A](task: ZIO[Serializable, E, A]): A = {
    unsafeRun[E, A](mkEnv.flatMap(env => task.provide(env)))
  }
}
