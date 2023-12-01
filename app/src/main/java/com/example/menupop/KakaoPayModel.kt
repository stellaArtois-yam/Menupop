package com.example.menupop

data class KakaoPayModel(
    val cid : String,
    val partner_order_id : String,
    val partner_user_id : String,
    val pg_token : String,
    val item_name : String, //상품명(결제 할 때 띄워지는거)
    val total_amount : String, //결제 금액
    val approval_url : String, //성공 시 redirect url
    val cancel_url : String, //취소 시 redirect url
    val fail_url : String //실패 시 redirect url
)
