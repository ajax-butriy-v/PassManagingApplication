package com.example.passmanager.domain

import com.example.passmanager.domain.MongoPass.Companion.COLLECTION_NAME
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.time.Instant

@TypeAlias("Pass")
@Document(collection = COLLECTION_NAME)
data class MongoPass(
    @Id val id: ObjectId?,
    val purchasedFor: BigDecimal?,
    val passOwner: MongoPassOwner?,
    val passType: MongoPassType?,
    val purchasedAt: Instant? = Instant.now(),
) {
    companion object {
        const val COLLECTION_NAME = "pass"
    }
}

