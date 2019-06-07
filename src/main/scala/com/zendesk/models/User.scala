package com.zendesk.models

import java.util.UUID

import com.zendesk.models.User.UserId

object User {
  type UserId = UUID
  implicit def toUid(uuid: UUID): UserId = {
    uuid.asInstanceOf[UserId]
  }
}

final case class User(id: UserId, name: String, email: String)

final case class UserCreateRequest(name: String, email: String)

