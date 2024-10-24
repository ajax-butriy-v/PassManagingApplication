package com.example.passmanagersvc.domain

import com.example.passmanagersvc.domain.MongoPassOwner.Companion.COLLECTION_NAME
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.mapping.Document

@TypeAlias("PassOwner")
@Document(collection = COLLECTION_NAME)
data class MongoPassOwner(
    @Id val id: ObjectId?,
    val firstName: String?,
    val lastName: String?,
    val phoneNumber: String?,
    val email: String?,
    @Version
    val version: Long = 0,
) {
    companion object {
        const val COLLECTION_NAME = "pass_owner"
    }
}
