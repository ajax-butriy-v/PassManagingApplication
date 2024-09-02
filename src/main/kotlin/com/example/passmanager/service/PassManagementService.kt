package com.example.passmanager.service

import org.bson.types.ObjectId

interface PassManagementService {
    fun cancelPass(passOwnerId: ObjectId, passId: ObjectId): Boolean
    fun transferPass(passId: ObjectId, targetPassOwnerId: ObjectId)
}
