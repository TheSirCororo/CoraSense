package ru.cororo.corasense.shared.service

import kotlinx.rpc.annotations.Rpc
import ru.cororo.corasense.shared.model.campaign.Campaign

@Rpc
interface CurrentDayService {
    suspend fun getCurrentDay(): Int

    suspend fun setCurrentDay(day: Int)
}

suspend fun Campaign.hasStarted(currentDayService: CurrentDayService) = currentDayService.getCurrentDay() >= startDate
