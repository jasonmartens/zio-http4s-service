package com.zendesk.endpoints

import java.util.UUID.randomUUID

import com.zendesk.models.{User, UserCreateRequest}
import com.zendesk.repository.{DeleteFailure, DeleteSuccess, UserRepository}
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import scalaz.zio.TaskR
import scalaz.zio.interop.catz._


final case class UserEndpoint[R <: UserRepository](rootUri: String) extends JsonSupport[R] {
  import com.zendesk.services.UserServiceImpl._

  type UserTask[A] = TaskR[R, A]

  val dsl: Http4sDsl[UserTask] = Http4sDsl[UserTask]
  import dsl._

  def endpoints: HttpRoutes[UserTask] = {
    HttpRoutes.of[UserTask] {
      case GET -> Root / `rootUri` =>
        Ok(getAll)

      case GET -> Root / `rootUri` / UUIDVar(id) =>
        for {
          user <- getById(id)
          response <- user.fold(NotFound())(u => Ok(u))
        } yield response

      case req @ POST -> Root / `rootUri` =>
        req.decode[UserCreateRequest] { newUser =>
          val user = User(id = randomUUID(), newUser.name, newUser.email)
          Created(create(user))
        }

      case DELETE -> Root / `rootUri` / UUIDVar(id) =>
        delete(id).flatMap {
          case DeleteSuccess => Ok()
          case DeleteFailure => NotFound()
        }
    }
  }
}

