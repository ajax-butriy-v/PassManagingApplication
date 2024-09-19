package com.example.passmanager.domain

import com.example.passmanager.domain.MongoPass.Companion.COLLECTION_NAME
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import java.math.BigDecimal
import java.time.Instant

@TypeAlias("Pass")
@Document(collection = COLLECTION_NAME)
@CompoundIndex(name = "pass_owner_id_pass_id_index", def = "{'passOwnerId': 1, '_id': 1}")
@CompoundIndex(name = "pass_owner_id_purchased_at_index", def = "{'passOwnerId': 1, 'purchasedAt': 1}")
data class MongoPass(
    @Id val id: ObjectId?,
    @Field(targetType = FieldType.DECIMAL128)
    val purchasedFor: BigDecimal?,
    val passOwnerId: ObjectId?,
    val passTypeId: ObjectId?,
    val purchasedAt: Instant? = Instant.now(),
    @Version
    val version: Long = 0,
) {
    companion object {
        const val COLLECTION_NAME = "pass"
    }
}
