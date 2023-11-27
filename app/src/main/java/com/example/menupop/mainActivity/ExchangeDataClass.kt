package com.example.menupop.mainActivity

class ExchangeDataClass : ArrayList<ExchangeDataClass.ExchangeDataClassItem>(){
    data class ExchangeDataClassItem(
        var bkpr: String,
        var cur_nm: String,
        var cur_unit: String,
        var deal_bas_r: String,
        var kftc_bkpr: String,
        var kftc_deal_bas_r: String,
        var result: Int,
        var ten_dd_efee_r: String,
        var ttb: String,
        var tts: String,
        var yy_efee_r: String
    )
}