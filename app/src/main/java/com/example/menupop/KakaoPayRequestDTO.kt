package com.example.menupop

data class KakaoPayRequestDTO(
    val cid : String,
    val partner_order_id : String,
    val partner_user_id : String,
    val item_name : String,
    val quantity : String,
    val total_amount : String,
    val tax_free_amount : String,
    val approval_url : String,
    val cancel_url : String,
    val fail_url : String
)
