package com.example.pass_manager.domain

import com.example.pass_manager.domain.MongoPassType.Companion.COLLECTION_NAME
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import java.math.BigDecimal
import java.time.Instant

@TypeAlias("PassType")
@Document(collection = COLLECTION_NAME)
data class MongoPassType(
    @Id val id: ObjectId?,
    val activeFrom: Instant?,
    val activeTo: Instant?,
    val name: String?,
    @Field(targetType = FieldType.DECIMAL128)
    val price: BigDecimal?,
) {
    companion object {
        const val COLLECTION_NAME = "pass_type"
    }
}
