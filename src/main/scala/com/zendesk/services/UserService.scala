package com.zendesk.services

import com.zendesk.models.User
import com.zendesk.services.UserService.{Service, UserServiceEnvironment}
import com.zendesk.models.User._
import com.zendesk.repository.{DeleteResult, UserRepository}
import scalaz.zio.ZIO

/**
  * This is where the ZIO environment magic happens. At runtime either the test or
  * production UserRepository will be in the environment.
  */
object UserService {
  type UserServiceEnvironment = UserRepository

  trait Service[R] {
    def getAll: ZIO[R, Throwable, List[User]]
    def getById(id: UserId): ZIO[R, Throwable, Option[User]]
    def create(user: User): ZIO[R, Throwable, User]
    def delete(id: UserId): ZIO[R, Throwable, DeleteResult]
  }
}

object UserServiceImpl extends Service[UserServiceEnvironment] {

  override def getAll: ZIO[UserServiceEnvironment, Throwable, List[User]] = {
    ZIO.accessM[UserRepository] { env =>
      env.userRepository.getAll()
    }
  }

  override def getById(id: UserId): ZIO[UserServiceEnvironment, Throwable, Option[User]] = {
    ZIO.accessM[UserRepository] { env =>
      env.userRepository.getById(id)
    }
  }

  override def create(user: User): ZIO[UserServiceEnvironment, Throwable, User] = {
    ZIO.accessM[UserRepository] { env =>
      env.userRepository.create(user)
    }
  }

  override def delete(id: UserId): ZIO[UserServiceEnvironment, Throwable, DeleteResult] = {
    ZIO.accessM[UserRepository] { env =>
      env.userRepository.delete(id)
    }
  }
}
