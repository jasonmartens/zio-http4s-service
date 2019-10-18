package com.zendesk

import cats.effect.Blocker
import doobie.hikari._
import doobie.util.transactor.Transactor
import org.flywaydb.core.Flyway
import zio._
import zio.interop.catz._

import scala.concurrent.ExecutionContext

object ServiceConfig {

  final case class Config(
    appConfig: AppConfig,
    dbConfig: DBConfig
  )

  final case class AppConfig(
    port: Int,
    baseUrl: String
  )

  final case class DBConfig(
    url: String,
    driver: String,
    user: String,
    password: String
  )

  def initDb(cfg: DBConfig): Task[Unit] =
    ZIO.effect {
      val fw = Flyway
        .configure()
        .dataSource(cfg.url, cfg.user, cfg.password)
        .load()
      fw.migrate()
    }.unit

  def mkTransactor(
    cfg: DBConfig,
    connectEC: ExecutionContext,
    blocker: Blocker
  ): Managed[Throwable, Transactor[Task]] = {
    val xa = HikariTransactor.newHikariTransactor[Task](
      cfg.driver,
      cfg.url,
      cfg.user,
      cfg.password,
      connectEC,
      blocker
    )
    ZIO.runtime[Any].toManaged_.flatMap { implicit rt =>
      xa.toManaged
    }
  }
}
