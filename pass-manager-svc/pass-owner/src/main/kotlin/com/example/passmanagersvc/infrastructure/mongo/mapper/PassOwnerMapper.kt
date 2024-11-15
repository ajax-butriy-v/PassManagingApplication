package com.example.passmanagersvc.infrastructure.mongo.mapper

import com.example.passmanagersvc.domain.PassOwner
import com.example.passmanagersvc.infrastructure.mongo.entity.MongoPassOwner
import org.bson.types.ObjectId

object PassOwnerMapper {
    fun PassOwner.toModel(): MongoPassOwner {
        return MongoPassOwner(
            id = id?.let { ObjectId(it) },
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phoneNumber,
            email = email,
            version = version ?: 0L
        )
    }

    fun MongoPassOwner.toDomain(): PassOwner {
        return PassOwner(
            id = id?.toString(),
            firstName = firstName.orEmpty(),
            lastName = lastName.orEmpty(),
            phoneNumber = phoneNumber.orEmpty(),
            email = email.orEmpty(),
            version = version
        )
    }
}
