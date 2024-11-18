package com.example.passmanagersvc.util


import com.example.passmanagersvc.passowner.infrastructure.mongo.entity.MongoPassOwner
import com.example.passmanagersvc.passowner.infrastructure.mongo.mapper.PassOwnerMapper.toDomain
import com.example.passmanagersvc.passowner.infrastructure.rest.dto.PassOwnerDto
import com.example.passmanagersvc.passowner.infrastructure.rest.dto.PassOwnerUpdateDto
import io.github.serpro69.kfaker.Faker
import org.bson.types.ObjectId

object PassOwnerFixture {
    val id: ObjectId = ObjectId.get()
    val mongoPassOwnerToCreate = MongoPassOwner(
        id = null,
        firstName = "First Name",
        lastName = "Last Name",
        phoneNumber = "1234567890",
        email = "example@gmail.com",
    )
    val passOwnerToCreate = mongoPassOwnerToCreate.toDomain()

    val mongoPassOwnerFromDb = mongoPassOwnerToCreate.copy(id = id)
    val passOwnerFromDb = mongoPassOwnerFromDb.toDomain()
    val passOwnerIdFromDb = id.toString()

    val passOwnerDto = PassOwnerDto(
        firstName = mongoPassOwnerFromDb.firstName,
        lastName = mongoPassOwnerFromDb.lastName,
        phoneNumber = mongoPassOwnerFromDb.phoneNumber,
        email = mongoPassOwnerFromDb.email
    )

    val passOwnerUpdateDto = PassOwnerUpdateDto(
        firstName = mongoPassOwnerFromDb.firstName,
        lastName = mongoPassOwnerFromDb.lastName,
        phoneNumber = mongoPassOwnerFromDb.phoneNumber,
        email = mongoPassOwnerFromDb.email
    )

    fun getOwnerWithUniqueFields(): MongoPassOwner {
        return Faker().run {
            mongoPassOwnerToCreate.copy(
                email = internet.safeEmail(),
                phoneNumber = string.numerify("#".repeat(10))
            )
        }
    }
}

