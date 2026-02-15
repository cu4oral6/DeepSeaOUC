package com.github.deepseaouc.backend.entity.vo.response

import com.github.deepseaouc.backend.entity.DataCopy

data class ChatHistoryResponseVO(
    val role: String = "",
    val content: String = "",
    val id: String? = ""
): DataCopy