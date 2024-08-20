package com.example.pass_manager.domain

import com.example.pass_manager.domain.Client.Companion.COLLECTION_NAME
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document

@TypeAlias("MongoClient")
@Document(collection = COLLECTION_NAME)
data class Client(
    @Id val id: ObjectId?,
    val firstName: String?,
    val lastName: String?,
    val phoneNumber: String?,
    val email: String?,
    val ownedPasses: List<MongoPass>?,
) {

    companion object {
        const val COLLECTION_NAME = "client"
    }
}
