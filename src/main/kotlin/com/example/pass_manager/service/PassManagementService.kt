package com.example.pass_manager.service

import org.bson.types.ObjectId

interface PassManagementService {
    fun cancelPass(passOwnerId: ObjectId, passId: ObjectId): Boolean
    fun transferPass(passId: ObjectId, targetPassOwnerId: ObjectId)
}

