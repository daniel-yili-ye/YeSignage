package com.yesleaf.yesdigiboard

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.*
import java.time.Instant
import java.time.LocalTime

@Serializable
data class DeviceConfig(val code: String, val name: String, val lastUpdated:LocalDateTime , val tasks: List<DeviceTask>)

@Serializable
data class DeviceTask(val action: String, val time: LocalDateTime, val video: String="")