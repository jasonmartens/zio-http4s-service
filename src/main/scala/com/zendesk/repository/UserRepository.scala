package com.zendesk.repository

import com.zendesk.models.User
import com.zendesk.models.User.UserId
import zio.{Ref, ZIO}

sealed trait DeleteResult
case object DeleteSuccess extends DeleteResult
case object DeleteFailure extends DeleteResult

trait UserRepository extends Serializable {
  val userRepository: UserRepository.Service[Any]
}

object UserRepository extends Serializable {
  trait Service[R] extends Serializable {
    def getAll: ZIO[R, Nothing, List[User]]
    def getById(id: UserId): ZIO[R, Nothing, Option[User]]
    def delete(id: UserId): ZIO[R, Nothing, DeleteResult]
    def create(user: User): ZIO[R, Nothing, User]
  }

  final case class InMemoryUserRepository(ref: Ref[Map[UserId, User]]) extends Service[Any] {
    override def getAll: ZIO[Any, Nothing, List[User]] = {
      ref.get.map(_.values.toList)
    }

    override def getById(id: UserId): ZIO[Any, Nothing, Option[User]] = {
      ref.get.map(_.get(id))
    }

    override def delete(id: UserId): ZIO[Any, Nothing, DeleteResult] = {
      ref.modify{ m =>
        if (m.contains(id)) (DeleteSuccess, m - id) else (DeleteFailure, m)
      }
    }

    override def create(user: User): ZIO[Any, Nothing, User] = {
      for {
        _ <- ref.update(store => store + (user.id -> user))
      } yield user
    }
  }
}
