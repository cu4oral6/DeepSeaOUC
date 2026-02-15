package com.github.deepseaouc.backend.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.github.deepseaouc.backend.entity.dto.Account
import org.apache.ibatis.annotations.Mapper

@Mapper
interface AccountMapper : BaseMapper<Account>