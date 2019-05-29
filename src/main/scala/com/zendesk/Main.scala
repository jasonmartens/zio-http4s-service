package com.zendesk

import cats.effect.ExitCode
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.middleware.CORS
import org.http4s.server.blaze.BlazeServerBuilder
import scalaz.zio._
import scalaz.zio.interop.catz._
import scalaz.zio.blocking.Blocking
import scalaz.zio.clock.Clock
import scalaz.zio.console.Console
import scalaz.zio.console._
import scalaz.zio.scheduler.Scheduler

object Main extends App {

  type AppEnvironment = Clock with Console with Blocking
  type AppTask[A] = TaskR[AppEnvironment, A]

  override def run(args: List[String]): ZIO[Environment, Nothing, Int] =
    (for {
      blockingEC <- ZIO.environment[Blocking].flatMap(_.blocking.blockingExecutor).map(_.asEC)
      httpApp = Router[AppTask]("/" -> ZendeskService("/").service).orNotFound
      server = ZIO.runtime[AppEnvironment].flatMap{ implicit rts =>
        BlazeServerBuilder[AppTask]
          .bindHttp(8080, "0.0.0.0")
          .withHttpApp(CORS(httpApp))
          .serve
          .compile[AppTask, AppTask, ExitCode]
          .drain
      }
      program <- server.provideSome[Environment] { base =>
        new Clock with Console with Blocking {
          override val scheduler: Scheduler.Service[Any] = base.scheduler
          override val console: Console.Service[Any] = base.console
          override val clock: Clock.Service[Any] = base.clock
          override val blocking: Blocking.Service[Any] = base.blocking
        }
      }
    } yield program).foldM(
      failure = err => putStrLn(s"Execution failed with: $err") *> ZIO.succeed(1),
      success = _ => ZIO.succeed(0)
    )
}
