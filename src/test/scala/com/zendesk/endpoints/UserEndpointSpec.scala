package com.zendesk.endpoints

import com.zendesk.HTTPSpec
import com.zendesk.models.User.UserId
import com.zendesk.models.{User, UserCreateRequest}
import com.zendesk.repository.UserRepository
import com.zendesk.repository.UserRepository.InMemoryUserRepository
import io.circe.generic.auto._
import org.http4s._
import org.http4s.implicits._
import org.scalatest.Matchers
import scalaz.zio.interop.catz._
import scalaz.zio.{DefaultRuntime, Ref, UIO, ZIO}

class UserEndpointSpec extends HTTPSpec with Matchers {
  import UserEndpointSpec._
  import UserEndpointSpec.userEndpoint._

  private val app = userEndpoint.endpoints.orNotFound

  describe("UserService") {
    it("Should allow creating new users and fetching them") {
      val req = request(Method.POST, "/users").withEntity(UserCreateRequest("jason", "jmartens@zendesk.com"))
      runWithEnv(
        for {
          createResponse <- app.run(req)
          createUser <- createResponse.as[User]
          getResponse <- app.run(request(Method.GET, uri = s"/users/${createUser.id.toString}"))
          getUser <- getResponse.as[User]

        } yield {
          createResponse.status shouldBe Status.Created
          createUser.email shouldBe "jmartens@zendesk.com"
          createUser.id.toString should fullyMatch regex """\b[0-9a-f]{8}\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\b[0-9a-f]{12}\b"""
          createUser.email shouldBe "jmartens@zendesk.com"

          getResponse.status shouldBe Status.Ok
          getUser.id shouldBe createUser.id
          getUser.name shouldBe createUser.name
          getUser.email shouldBe createUser.email
        }
      )
    }
  }
}

object UserEndpointSpec extends DefaultRuntime {
  val userEndpoint: UserEndpoint[UserRepository] = UserEndpoint[UserRepository]("")

  val mkEnv: UIO[UserRepository] =
    for {
      store <- Ref.make(Map[UserId, User]())
      repo = InMemoryUserRepository(store)
      env = new UserRepository {
        override val userRepository: UserRepository.Service[Any] = repo
      }
    } yield env

  def runWithEnv[E, A](task: ZIO[UserRepository, E, A]): A = {
    unsafeRun[E, A](mkEnv.flatMap(env => task.provide(env)))
  }
}
