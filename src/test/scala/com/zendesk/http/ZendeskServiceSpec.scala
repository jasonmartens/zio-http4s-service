package com.zendesk.http

import com.zendesk.ZendeskService.{DiagnosticResult, PingResponse}
import com.zendesk.{HTTPSpec, ZendeskService}
import io.circe.generic.auto._
import org.http4s.implicits._
import org.http4s.{Status, _}
import scalaz.zio._
import scalaz.zio.interop.catz._

class ZendeskServiceSpec extends HTTPSpec {
  import ZendeskServiceSpec._
  import ZendeskServiceSpec.zendeskService._

  val app = zendeskService.service.orNotFound

  describe("ZendeskService") {
    it("should respond to /z/ping") {
      val req = request[ZendeskTask](Method.GET, "/z/ping")
      runWithEnv(check(app.run(req), Status.Ok, Some(PingResponse("Ok"))))
    }

    it("should respond to /z/diagnostics") {
      val req = request[ZendeskTask](Method.GET, "/z/diagnostics")
      runWithEnv(check(app.run(req), Status.Ok, Some(DiagnosticResult(success = true, "everything A-OK"))))
    }
  }
}

object ZendeskServiceSpec extends DefaultRuntime {
  val zendeskService: ZendeskService[Serializable] = ZendeskService("/")
  case object TestEnv extends Serializable

  val mkEnv: UIO[Serializable] = UIO(TestEnv)

  def runWithEnv[E, A](task: ZIO[Serializable, E, A]): A = {
    unsafeRun[E, A](mkEnv.flatMap(env => task.provide(env)))
  }
}
