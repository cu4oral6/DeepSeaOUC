package com.github.deepseaouc.backend.entity.dto

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import com.github.deepseaouc.backend.entity.DataCopy
import lombok.AllArgsConstructor
import java.util.Date

@TableName("account")
@AllArgsConstructor
data class Account(
    @TableId(type = IdType.AUTO)
    val id: Int?,
    val username: String,
    val password: String,
    val email: String,
    val role: String,
    val registerTime: Date
) : DataCopy