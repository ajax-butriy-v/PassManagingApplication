package com.example.pass_manager.data

import com.example.pass_manager.domain.MongoClient
import com.example.pass_manager.domain.MongoPass
import com.example.pass_manager.domain.MongoPassType
import org.bson.types.ObjectId
import java.math.BigDecimal
import java.time.Instant

class TestDataFixture {
    val clientId: ObjectId = ObjectId.get()
    val passId: ObjectId = ObjectId.get()
    val clientToCreate = MongoClient(
        id = null,
        firstName = "First Name",
        lastName = "Last Name",
        phoneNumber = "+123456789",
        email = "example@gmail.com",
        ownedPasses = listOf(
            MongoPass(
                id = passId,
                purchasedFor = BigDecimal.TEN,
                client = null,
                passType = null,
                purchasedAt = Instant.MAX
            )
        )
    )
    val clientFromDb = clientToCreate.copy(id = clientId)
    private val passTypes = listOf("First type", "Second type", "Third type")
        .map {
            MongoPassType(
                id = ObjectId.get(),
                activeFrom = Instant.MIN,
                activeTo = Instant.MAX,
                name = it,
                price = BigDecimal.TEN
            )
        }

    val passes = passTypes.map {
        MongoPass(
            id = ObjectId.get(),
            purchasedFor = BigDecimal.TEN,
            client = clientFromDb,
            passType = it,
            purchasedAt = Instant.now(),
        )
    }

    val singlePass = passes.first()
    val singlePassId: ObjectId = singlePass.id ?: ObjectId("")
}
