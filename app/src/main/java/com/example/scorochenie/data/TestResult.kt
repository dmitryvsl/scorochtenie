package com.example.scorochenie.data

data class TestResult(
    val techniqueName: String,
    val durationPerWord: Long,
    val score: Int,
    val totalQuestions: Int,
    val timestamp: Long
)