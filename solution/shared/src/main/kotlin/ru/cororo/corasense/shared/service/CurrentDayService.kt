package ru.cororo.corasense.shared.service

import kotlinx.rpc.RemoteService
import kotlinx.rpc.annotations.Rpc
import ru.cororo.corasense.shared.model.campaign.Campaign

@Rpc
interface CurrentDayService : RemoteService {
    suspend fun getCurrentDay(): Int

    suspend fun setCurrentDay(day: Int)
}

suspend fun Campaign.hasStarted(currentDayService: CurrentDayService) = currentDayService.getCurrentDay() >= startDate
