package com.example.menupop.mainActivity.profile

import com.google.gson.annotations.SerializedName

data class KakaoPayApproveResponseDTO(
    val tid : String?, //결제 고유번호
    val cid : String?, //가맹점 코드
    @SerializedName("partner_order_id")
    val partnerOrderId : String?,
    @SerializedName("partner_user_id")
    val partnerUserId : String?,
    @SerializedName("payment_method_type")
    val paymentMethodType : String?,
    val amount : KakaoPayPriceDTO?,
    @SerializedName("item_name")
    val itemName : String?,
    val quantity : String?,
    @SerializedName("approved_at")
    val approvedAt : String?) //결제 승인 시간
