package com.github.deepseaouc.backend.entity.vo.response

import java.util.Date

data class AuthorizeVO(
    val username: String = "",
    val role: String = "",
    val token: String = "",
    val expire: Date = Date(),
)