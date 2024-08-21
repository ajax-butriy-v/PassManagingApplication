package com.example.pass_manager.util

import com.example.pass_manager.domain.MongoPass
import com.example.pass_manager.domain.MongoPassType
import com.example.pass_manager.util.PassOwnerFixture.passOwnerFromDb
import com.example.pass_manager.util.PassOwnerFixture.updatedOwner
import org.bson.types.ObjectId
import java.math.BigDecimal
import java.time.Instant

object PassFixture {
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
    val passToCreate = MongoPass(
        id = null,
        purchasedFor = BigDecimal.TEN,
        passOwner = passOwnerFromDb,
        passType = null,
        purchasedAt = Instant.now(),
    )

    val passes = passTypes.map {
        MongoPass(
            id = ObjectId.get(),
            purchasedFor = BigDecimal.TEN,
            passOwner = passOwnerFromDb,
            passType = it,
            purchasedAt = Instant.now(),
        )
    }

    val passFromDb = passes.first()
    val singlePassId: ObjectId = passFromDb.id!!
    val updatedPass = passFromDb.copy(passOwner = updatedOwner)

}

