package com.example.library_backend.config

import com.example.library_backend.reservations.ReservationService
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@EnableScheduling
@Configuration
class SchedulingConfig

@Component
class ReservationExpiryJob(private val svc: ReservationService) {
    // 00:05 every day
    @Scheduled(cron = "0 5 0 * * *")
    fun run() { svc.expireOverdueWindows() }
}