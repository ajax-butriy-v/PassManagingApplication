package com.example.passmanagersvc.util


import com.example.passmanagersvc.domain.MongoPassOwner
import com.example.passmanagersvc.web.dto.PassOwnerDto
import com.example.passmanagersvc.web.dto.PassOwnerUpdateDto
import io.github.serpro69.kfaker.Faker
import org.bson.types.ObjectId

object PassOwnerFixture {
    val id: ObjectId = ObjectId.get()
    val passOwnerToCreate = MongoPassOwner(
        id = null,
        firstName = "First Name",
        lastName = "Last Name",
        phoneNumber = "1234567890",
        email = "example@gmail.com",
    )

    val passOwnerFromDb = passOwnerToCreate.copy(id = id)
    val passOwnerIdFromDb = id.toString()

    val passOwnerDto = PassOwnerDto(
        firstName = passOwnerFromDb.firstName,
        lastName = passOwnerFromDb.lastName,
        phoneNumber = passOwnerFromDb.phoneNumber,
        email = passOwnerFromDb.email
    )

    val passOwnerUpdateDto = PassOwnerUpdateDto(
        firstName = passOwnerFromDb.firstName,
        lastName = passOwnerFromDb.lastName,
        phoneNumber = passOwnerFromDb.phoneNumber,
        email = passOwnerFromDb.email
    )

    fun getOwnerWithUniqueFields(): MongoPassOwner {
        return Faker().run {
            passOwnerToCreate.copy(
                email = internet.safeEmail(),
                phoneNumber = string.numerify("#".repeat(10))
            )
        }
    }
}

