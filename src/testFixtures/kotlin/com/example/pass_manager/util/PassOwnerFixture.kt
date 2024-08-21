package com.example.pass_manager.util


import com.example.pass_manager.domain.MongoPassOwner
import org.bson.types.ObjectId

object PassOwnerFixture {
    val passOwnerId: ObjectId = ObjectId.get()
    val passOwnerToCreate = MongoPassOwner(
        id = null,
        firstName = "First Name",
        lastName = "Last Name",
        phoneNumber = "+123456789",
        email = "example@gmail.com",
        ownedPasses = null
    )

    val passOwnerFromDb = passOwnerToCreate.copy(id = passOwnerId)
    val updatedOwner = passOwnerFromDb.copy(id = ObjectId.get(), firstName = "Another one")
}

