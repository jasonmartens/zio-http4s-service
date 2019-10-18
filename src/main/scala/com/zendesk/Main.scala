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
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import zio.{ZEnv, _}
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.{Console, _}
import zio.interop.catz._
import zio.random.Random
import zio.system.System

object Main extends App {

  type AppEnvironment = Clock with Console with System with Random with Blocking with DoobieUserRepository
  type AppTask[A] = RIO[AppEnvironment, A]


  def createRoutes(basePath: String): Kleisli[AppTask, Request[AppTask], Response[AppTask]] = {
    val userEndpoints = new UserEndpoint[AppEnvironment]("users").endpoints
    val healthEndpoints = new HealthEndpoint[AppEnvironment]("z").endpoints
    val endpoints = userEndpoints <+> healthEndpoints
    Router[AppTask](basePath -> endpoints).orNotFound
  }

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] = {
    val p = for {
      cfg <- ZIO.fromEither(ConfigSource.default.load[Config])
      _ <- ServiceConfig.initDb(cfg.dbConfig)
      blockingEC <- ZIO.environment[Blocking].flatMap(_.blocking.blockingExecutor).map(_.asEC)
      transactorR = ServiceConfig.mkTransactor(cfg.dbConfig, Platform.executor.asEC, Blocker.liftExecutionContext(blockingEC))
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
        server.provideSome[ZEnv] { base =>
          new Clock with Console with System with Random with Blocking with DoobieUserRepository {
            override val clock: Clock.Service[Any] = base.clock
            override val console: Console.Service[Any] = base.console
            override val system: System.Service[Any] = base.system
            override val random: Random.Service[Any] = base.random
            override val blocking: Blocking.Service[Any] = base.blocking

            override protected def xa: doobie.Transactor[Task] = transactor
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
