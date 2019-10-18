package com.zendesk.repository

import java.util.UUID

import com.zendesk.models.User
import com.zendesk.models.User.UserId
import com.zendesk.repository.DoobieUserRepository.SQL
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor
import zio.interop.catz._
import zio.{Task, ZIO}

trait DoobieUserRepository extends UserRepository {

  protected def xa: Transactor[Task]

  override val userRepository: UserRepository.Service[Any] =
    new UserRepository.Service[Any] {
      override def getAll: ZIO[Any, Nothing, List[User]] =
        SQL.getAll.to[List].transact(xa).orDie

      override def getById(id: UserId): ZIO[Any, Nothing, Option[User]] =
        SQL.getOne(id).option.transact(xa).orDie

      override def delete(id: UserId): ZIO[Any, Nothing, DeleteResult] =
        SQL.delete(id).run.map {
          case 0 => DeleteFailure
          case _ => DeleteSuccess
        }.transact(xa).orDie

      override def create(user: User): ZIO[Any, Nothing, User] =
        SQL.create(user).run.map(_ => user).transact(xa).orDie
    }
}


object DoobieUserRepository {
  object SQL {

    // Teach doobie how to convert a UserId to a UUID
    implicit val uuidMeta: Meta[UserId] =
      Meta.Advanced.other[UUID]("uuid").timap[UserId](a => a)(a => a)

    def create(user: User): Update0 =
      sql"""
        INSERT INTO USERS (id, name, email)
        VALUES(${user.id}, ${user.name}, ${user.email})
      """.update

    def getAll: Query0[User] =
      sql"""SELECT * FROM USERS""".query[User]

    def getOne(id: UserId): Query0[User] =
      sql"""SELECT id, name, email FROM USERS WHERE id=$id """.query[User]

    def delete(id: UserId): Update0 =
      sql"""DELETE FROM USERS WHERE id=$id""".update
  }
}
