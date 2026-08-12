package com.originalgame.autoloot

data class Action(
    var type: String = "tap",
    var x: Float = 0f,
    var y: Float = 0f,
    var x2: Float = 0f,
    var y2: Float = 0f,
    var durationMs: Long = 80,
    var pauseMs: Long = 120,
    var enabled: Boolean = true
)
