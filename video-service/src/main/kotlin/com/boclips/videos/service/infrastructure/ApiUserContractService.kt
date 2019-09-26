package com.boclips.videos.service.infrastructure

import com.boclips.users.client.UserServiceClient
import com.boclips.users.client.model.contract.Contract
import com.boclips.videos.service.domain.service.UserContractService
import mu.KLogging
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service

@Service
class ApiUserContractService(
    private val userServiceClient: UserServiceClient
) : UserContractService {
    companion object : KLogging()

    @Retryable(
        maxAttempts = 3,
        backoff = Backoff(
            multiplier = 1.5
        )
    )
    override fun getContracts(userId: String): List<Contract> {
        return userServiceClient
            .getContracts(userId)
            .also {
                logger.info { "Found ${it.size} contracts for user $userId" }
            }
    }

    @Recover
    fun getContractsRecoveryMethod(e: Exception): List<Contract> {
        logger.warn { "Unable to retrieve user contracts, defaulting to an empty list. Cause: $e" }
        return emptyList()
    }
}
