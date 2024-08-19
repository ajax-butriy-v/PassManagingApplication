package com.example.pass_manager.web.dto

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Field
import java.math.BigDecimal

data class PriceDistribution(@Id val typeName: String?, @Field("total") val spentForPassType: BigDecimal)