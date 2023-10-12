package com.example.assigntodo.models

import java.util.UUID

data class Works(
    val id : String = UUID.randomUUID().toString(),
    val workID : String ? = null,
    val workTitle : String? = null,
    val workDesc : String? = null,
    val bossId : String? = null,
    val workPriority : String? = null,
    val workLastDate : String? = null,
    var workStatus : String? = null,
    var expanded : Boolean = false,
)