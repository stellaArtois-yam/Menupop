package com.example.menupop.mainActivity.profile

data class KakaoPayCancelResponseDTO(
    val aid : String, //요청 고유 번호
    val tid : String, //결제 고유 번호
    val cid : String, //가맹점 코드
    val status : String, //결제 상태
    val partner_order_id : String, // 가맹점 주문 번호
    val partner_user_id : String, // 가맹점 회원 id
    val payment_method_type : String, //결제 수단
    val amount : KakaoPayPriceDTO, //총 금액
    val item_name : String

)
