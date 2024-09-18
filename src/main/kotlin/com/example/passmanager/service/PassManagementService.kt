package com.example.passmanager.service

interface PassManagementService {
    fun cancelPass(passOwnerId: String, passId: String): Boolean
    fun transferPass(passId: String, targetPassOwnerId: String)
}
