package com.training.myplayer

import java.io.Serializable

/*
 *
 * Copyright (C) 2022 NIO Inc
 *
 * Ver   Date        Author    Desc
 *
 * V1.0  2022/6/11  hai.cui  Add for
 *
 */

class PlayerBean : Serializable {
    var url: String? = null
    var position: Long = 0
}