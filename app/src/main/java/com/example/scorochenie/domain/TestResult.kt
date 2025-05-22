package com.example.scorochenie.domain

data class TestResult(
    val techniqueName: String,
    val durationPerWord: Long,
    val score: Int,
    val totalQuestions: Int,
    val timestamp: Long
)