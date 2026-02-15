package com.github.deepseaouc.backend.entity.vo.request

import com.github.deepseaouc.backend.entity.DataCopy
import com.github.deepseaouc.backend.entity.bo.StMessage

data class ChatRequestVO(
    val modelId: Int,
    val characterId: Int,
    val messages: List<StMessage>,
    var uuid: String
) : DataCopy