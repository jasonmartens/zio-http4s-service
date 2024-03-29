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
import zio.interop.catz._
import zio.{DefaultRuntime, Ref, UIO, ZIO}

class UserEndpointSpec extends HTTPSpec with Matchers {
  import UserEndpointSpec._
  import UserEndpointSpec.userEndpoint._

  private val app = userEndpoint.endpoints.orNotFound


  describe("UserService") {
    it("Should allow creating new users and fetching them") {
      val req = request(Method.POST, rootUri).withEntity(UserCreateRequest("jason", "jmartens@zendesk.com"))
      val result = runWithEnv(
        for {
          createResponse <- app.run(req)
          _ = createResponse.status shouldBe Status.Created
          createUser <- createResponse.as[User]
          _ = {
            createUser.email shouldBe "jmartens@zendesk.com"
            createUser.id.toString should fullyMatch regex """\b[0-9a-f]{8}\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\b[0-9a-f]{12}\b"""
            createUser.email shouldBe "jmartens@zendesk.com"
          }

          getResponse <- app.run(request(Method.GET, uri = s"$rootUri/${createUser.id.toString}"))
          _ = createResponse.status shouldBe Status.Created
          getUser <- getResponse.as[User]
          _ = {
            getUser.id shouldBe createUser.id
            getUser.name shouldBe createUser.name
            getUser.email shouldBe createUser.email
          }
        } yield true
      )
      result shouldBe true
    }
  }
}

object UserEndpointSpec extends DefaultRuntime {
  val userEndpoint: UserEndpoint[UserRepository] = UserEndpoint[UserRepository]("users")

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
