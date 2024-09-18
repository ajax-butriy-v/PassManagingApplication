package com.example.passmanager.util


import com.example.passmanager.domain.MongoPassOwner
import org.bson.types.ObjectId

object PassOwnerFixture {
    val id: ObjectId = ObjectId.get()
    val passOwnerToCreate = MongoPassOwner(
        id = null,
        firstName = "First Name",
        lastName = "Last Name",
        phoneNumber = "+123456789",
        email = "example@gmail.com",
    )

    val passOwnerFromDb = passOwnerToCreate.copy(id = id)
    val passOwnerIdFromDb = id.toString()
}

