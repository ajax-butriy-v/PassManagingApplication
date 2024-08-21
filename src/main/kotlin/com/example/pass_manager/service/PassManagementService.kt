package com.example.pass_manager.service

import com.example.pass_manager.domain.MongoPass
import org.bson.types.ObjectId

interface PassManagementService {
    fun cancelPass(passOwnerId: ObjectId, passId: ObjectId): Boolean
    fun transferPassToAnotherClient(passId: ObjectId, targetPassOwnerId: ObjectId): MongoPass
}

