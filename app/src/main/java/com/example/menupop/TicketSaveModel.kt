package com.example.menupop

import kotlinx.serialization.Serializable

@Serializable
data class TicketSaveModel(
    val identifier : Int,
    val tid : String,
    val paymentType: String,
    val item : String,
    val price : Int,
    val approvedAt : String,
    val translationTicket : Int,
    val foodTicket : Int
)
