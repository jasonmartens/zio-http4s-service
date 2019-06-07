package com.zendesk

import cats.data.Kleisli
import cats.effect._
import cats.implicits._
import com.zendesk.ServiceConfig.Config
import com.zendesk.endpoints.{HealthEndpoint, UserEndpoint}
import com.zendesk.repository.DoobieUserRepository
import org.http4s.{Request, Response}
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import scalaz.zio._
import scalaz.zio.blocking.Blocking
import scalaz.zio.clock.Clock
import scalaz.zio.console.{Console, _}
import scalaz.zio.interop.catz._
import pureconfig.generic.auto._

object Main extends App {

  type AppEnvironment = Clock with Console with Blocking with DoobieUserRepository
  type AppTask[A] = TaskR[AppEnvironment, A]


  def createRoutes(basePath: String): Kleisli[AppTask, Request[AppTask], Response[AppTask]] = {
    val userEndpoints = new UserEndpoint[AppEnvironment]("users").endpoints
    val healthEndpoints = new HealthEndpoint[AppEnvironment]("z").endpoints
    val endpoints = userEndpoints <+> healthEndpoints
    Router[AppTask](basePath -> endpoints).orNotFound
  }

  override def run(args: List[String]): ZIO[Environment, Nothing, Int] = {
    val p = for {
      cfg <- ZIO.fromEither(pureconfig.loadConfig[Config])
      _ <- ServiceConfig.initDb(cfg.dbConfig)
      blockingEC <- ZIO.environment[Blocking].flatMap(_.blocking.blockingExecutor).map(_.asEC)
      transactorR = ServiceConfig.mkTransactor(cfg.dbConfig, Platform.executor.asEC, blockingEC)
      httpApp = createRoutes("/")
      server = ZIO.runtime[AppEnvironment].flatMap { implicit rts =>
        BlazeServerBuilder[AppTask]
          .bindHttp(8080, "0.0.0.0")
          .withHttpApp(CORS(httpApp))
          .serve
          .compile[AppTask, AppTask, ExitCode]
          .drain
      }
      program <- transactorR.use { transactor =>
        server.provideSome[Environment] { base =>
          new Clock with Console with Blocking with DoobieUserRepository {
            override protected def xa: doobie.Transactor[Task] = transactor

            override val console: Console.Service[Any] = base.console
            override val clock: Clock.Service[Any] = base.clock
            override val blocking: Blocking.Service[Any] = base.blocking
          }
        }
      }
    } yield program

    p.foldM(
      failure = err => putStrLn(s"Execution failed with: $err") *> ZIO.succeed(1),
      success = _ => ZIO.succeed(0)
    )
  }
}
