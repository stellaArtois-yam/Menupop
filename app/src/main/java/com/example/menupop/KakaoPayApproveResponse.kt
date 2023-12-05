package com.example.menupop

data class KakaoPayApproveResponse(
    val tid : String, //결제 고유번호
    val cid : String, //가맹점 코드
    val partner_order_id : String,
    val partner_user_id : String,
    val payment_method_type : String,
    val amount : KakaoPayPrice,
    val item_name : String,
    val quantity : String,
    val approved_at : String) //결제 승인 시간
