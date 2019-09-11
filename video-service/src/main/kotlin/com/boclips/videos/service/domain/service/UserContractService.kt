package com.boclips.videos.service.domain.service

import com.boclips.users.client.model.contract.Contract

interface UserContractService {
    fun getContracts(userId: String): List<Contract>
}
