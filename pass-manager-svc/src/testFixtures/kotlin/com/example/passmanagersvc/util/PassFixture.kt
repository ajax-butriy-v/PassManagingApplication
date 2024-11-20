package com.example.passmanagersvc.util

import com.example.passmanagersvc.pass.infrastructure.mongo.entity.MongoPass
import com.example.passmanagersvc.pass.infrastructure.mongo.mapper.PassMapper.toDomain
import com.example.passmanagersvc.passtype.infrastructure.mongo.entity.MongoPassType
import com.example.passmanagersvc.passtype.infrastructure.mongo.mapper.PassTypeMapper.toDomain
import com.example.passmanagersvc.util.PassOwnerFixture.mongoPassOwnerFromDb
import org.bson.types.ObjectId
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit


object PassFixture {
    private val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    private val instant = Instant.now(clock).truncatedTo(ChronoUnit.MILLIS)

    val mongoPassTypesToCreate = listOf("First", "Second", "Third")
        .map {
            MongoPassType(
                id = null,
                activeFrom = instant,
                activeTo = instant.plus(10, ChronoUnit.DAYS),
                name = it,
                price = BigDecimal.TEN
            )
        }
    val mongoPassTypeToCreate = mongoPassTypesToCreate.first()
    val passTypeToCreate = mongoPassTypeToCreate.toDomain()
    val passTypes = mongoPassTypesToCreate.map { it.copy(id = ObjectId.get()) }
    val singleMongoPassType = passTypes.first()
    val singlePassType = singleMongoPassType.toDomain()
    val singlePassTypeId = singleMongoPassType.id!!.toString()

    val passesToCreate = passTypes.map {
        MongoPass(
            id = null,
            purchasedFor = BigDecimal.TEN,
            passOwnerId = mongoPassOwnerFromDb.id,
            passTypeId = it.id,
            purchasedAt = instant,
        )
    }
    val mongoPassesFromDb = passesToCreate.map { it.copy(id = ObjectId.get()) }
    val passesFromDb = mongoPassesFromDb.map { it.toDomain() }
    val mongoPassToCreate = passesToCreate.first()
    val passToCreate = mongoPassToCreate.toDomain()
    val mongoPassFromDb = mongoPassesFromDb.first()
    val passFromDb = mongoPassFromDb.toDomain()
    val singlePassId = mongoPassFromDb.id.toString()

    fun passesToCreate(passOwnerId: ObjectId): List<MongoPass> {
        return passTypes.map {
            MongoPass(
                id = null,
                purchasedFor = BigDecimal.TEN,
                passOwnerId = passOwnerId,
                passTypeId = it.id,
                purchasedAt = instant,
            )
        }
    }

    fun passesToCreate(passOwnerId: ObjectId, passTypes: List<MongoPassType>): List<MongoPass> {
        return passTypes.map {
            MongoPass(
                id = null,
                purchasedFor = BigDecimal.TEN,
                passOwnerId = passOwnerId,
                passTypeId = it.id,
                purchasedAt = instant,
            )
        }
    }
}

