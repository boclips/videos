package com.boclips.contentpartner.service.infrastructure.contract

import com.boclips.contentpartner.service.common.PageRequest
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractRepository
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContentPartnerContractUpdateCommand
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContractDates
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContractFilter
import com.boclips.contentpartner.service.domain.model.contentpartnercontract.ContractRoyaltySplit
import com.boclips.contentpartner.service.testsupport.AbstractSpringIntegrationTest
import com.boclips.videos.service.testsupport.ContentPartnerContractFactory
import com.boclips.videos.service.testsupport.ContractCostsFactory
import com.boclips.videos.service.testsupport.ContractRestrictionsFactory
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.util.Currency

class MongoContentPartnerContractRepositoryIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var contentPartnerContractRepository: ContentPartnerContractRepository

    @Test
    fun `can create a content partner contract`() {
        val original = ContentPartnerContractFactory.sample()
        val created = contentPartnerContractRepository.create(original)
        assertThat(original.id.value).isEqualTo(created.id.value)
    }

    @Test
    fun `can find a content partner contract`() {
        val original = ContentPartnerContractFactory.sample()
        contentPartnerContractRepository.create(original)
        val found = contentPartnerContractRepository.findById(original.id)
        assertThat(found).isEqualTo(original)
    }

    @Test
    fun `can replace contract content partner name `() {
        val original = ContentPartnerContractFactory.sample()
        val contract = contentPartnerContractRepository.create(original)

        contentPartnerContractRepository.update(
            listOf(
                ContentPartnerContractUpdateCommand.ReplaceContentPartnerName(
                    contractContentPartnerId = contract.id,
                    contentPartnerName = "Changed name"
                )
            )
        )

        val updatedContract = contentPartnerContractRepository.findById(contract.id)
        assertThat(updatedContract?.contentPartnerName).isEqualTo("Changed name")
    }

    @Test
    fun `can replace contract is rolling`() {
        val original = ContentPartnerContractFactory.sample(contractIsRolling = false)
        val contract = contentPartnerContractRepository.create(original)

        contentPartnerContractRepository.update(
            listOf(
                ContentPartnerContractUpdateCommand.ReplaceContractIsRolling(
                    contractContentPartnerId = contract.id,
                    contractIsRolling = true
                )
            )
        )

        val updatedContract = contentPartnerContractRepository.findById(contract.id)
        assertThat(updatedContract?.contractIsRolling).isEqualTo(true)
    }

    @Test
    fun `can replace contract dates`() {
        val original = ContentPartnerContractFactory.sample()
        val contract = contentPartnerContractRepository.create(original)
        val start = LocalDate.of(2016, 10, 10)
        val end = LocalDate.of(2019, 10, 31)

        contentPartnerContractRepository.update(
            listOf(
                ContentPartnerContractUpdateCommand.ReplaceContractDates(
                    contractContentPartnerId = contract.id,
                    contractDates = ContractDates(start, end)
                )
            )
        )

        val updatedContract = contentPartnerContractRepository.findById(contract.id)
        assertThat(updatedContract?.contractDates?.start).isEqualTo(start)
        assertThat(updatedContract?.contractDates?.end).isEqualTo(end)
    }

    @Test
    fun `can replace days before termination warning`() {
        val original = ContentPartnerContractFactory.sample()
        val contract = contentPartnerContractRepository.create(original)


        contentPartnerContractRepository.update(
            listOf(
                ContentPartnerContractUpdateCommand.ReplaceDaysBeforeTerminationWarning(
                    contractContentPartnerId = contract.id,
                    daysBeforeTerminationWarning = 99
                )
            )
        )

        val updatedContract = contentPartnerContractRepository.findById(contract.id)
        assertThat(updatedContract?.daysBeforeTerminationWarning).isEqualTo(99)
    }

    @Test
    fun `can replace document`() {
        val original = ContentPartnerContractFactory.sample()
        val contract = contentPartnerContractRepository.create(original)

        contentPartnerContractRepository.update(
            listOf(
                ContentPartnerContractUpdateCommand.ReplaceContractDocument(
                    contractContentPartnerId = contract.id,
                    contractDocument = "http://www.google.com"
                )
            )
        )

        val updatedContract = contentPartnerContractRepository.findById(contract.id)
        assertThat(updatedContract?.contractDocument.toString()).isEqualTo("http://www.google.com")
    }

    @Test
    fun `can replace years for maximum license`() {
        val original = ContentPartnerContractFactory.sample()
        val contract = contentPartnerContractRepository.create(original)


        contentPartnerContractRepository.update(
            listOf(
                ContentPartnerContractUpdateCommand.ReplaceYearsForMaximumLicense(
                    contractContentPartnerId = contract.id,
                    yearsForMaximumLicense = 55
                )
            )
        )

        val updatedContract = contentPartnerContractRepository.findById(contract.id)
        assertThat(updatedContract?.yearsForMaximumLicense).isEqualTo(55)
    }

    @Test
    fun `can replace days for sell off period`() {
        val original = ContentPartnerContractFactory.sample()
        val contract = contentPartnerContractRepository.create(original)


        contentPartnerContractRepository.update(
            listOf(
                ContentPartnerContractUpdateCommand.ReplaceDaysForSellOffPeriod(
                    contractContentPartnerId = contract.id,
                    daysForSellOffPeriod = 7
                )
            )
        )

        val updatedContract = contentPartnerContractRepository.findById(contract.id)
        assertThat(updatedContract?.daysForSellOffPeriod).isEqualTo(7)
    }

    @Test
    fun `can replace royalty split`() {
        val original = ContentPartnerContractFactory.sample()
        val contract = contentPartnerContractRepository.create(original)


        contentPartnerContractRepository.update(
            listOf(
                ContentPartnerContractUpdateCommand.ReplaceRoyaltySplit(
                    contractContentPartnerId = contract.id,
                    royaltySplit = ContractRoyaltySplit(download = 0.7.toFloat(), streaming = 0.5.toFloat())
                )
            )
        )

        val updatedContract = contentPartnerContractRepository.findById(contract.id)
        assertThat(updatedContract?.royaltySplit?.streaming).isEqualTo(0.5.toFloat())
        assertThat(updatedContract?.royaltySplit?.download).isEqualTo(0.7.toFloat())
    }

    @Test
    fun `can replace minimum price description`() {
        val original = ContentPartnerContractFactory.sample()
        val contract = contentPartnerContractRepository.create(original)


        contentPartnerContractRepository.update(
            listOf(
                ContentPartnerContractUpdateCommand.ReplaceMinimumPriceDescription(
                    contractContentPartnerId = contract.id,
                    minimumPriceDescription = "this is a minimum price - $99"
                )
            )
        )

        val updatedContract = contentPartnerContractRepository.findById(contract.id)
        assertThat(updatedContract?.minimumPriceDescription).isEqualTo("this is a minimum price - $99")
    }

    @Test
    fun `can replace currency`() {
        val original = ContentPartnerContractFactory.sample()
        val contract = contentPartnerContractRepository.create(original)
        val currency = Currency.getInstance("AUD")


        contentPartnerContractRepository.update(
            listOf(
                ContentPartnerContractUpdateCommand.ReplaceRemittanceCurrency(
                    contractContentPartnerId = contract.id,
                    remittanceCurrency = currency
                )
            )
        )

        val updatedContract = contentPartnerContractRepository.findById(contract.id)
        assertThat(updatedContract?.remittanceCurrency).isEqualTo(currency)
    }

    @Test
    fun `can replace restrictions`() {
        val original = ContentPartnerContractFactory.sample()
        val contract = contentPartnerContractRepository.create(original)
        val restrictions = ContractRestrictionsFactory.sample(clientFacing = listOf("updated restriction"))


        contentPartnerContractRepository.update(
            listOf(
                ContentPartnerContractUpdateCommand.ReplaceRestrictions(
                    contractContentPartnerId = contract.id,
                    restrictions = restrictions
                )
            )
        )

        val updatedContract = contentPartnerContractRepository.findById(contract.id)
        assertThat(updatedContract?.restrictions?.clientFacing).contains("updated restriction")
    }

    @Test
    fun `can replace costs`() {
        val original = ContentPartnerContractFactory.sample()
        val contract = contentPartnerContractRepository.create(original)
        val costs = ContractCostsFactory.sample(recoupable = false)


        contentPartnerContractRepository.update(
            listOf(
                ContentPartnerContractUpdateCommand.ReplaceCost(
                    contractContentPartnerId = contract.id,
                    costs = costs
                )
            )
        )

        val updatedContract = contentPartnerContractRepository.findById(contract.id)
        assertThat(updatedContract?.costs?.recoupable).isEqualTo(false)
    }

    @Test
    fun `returns result of update`() {
        val original = ContentPartnerContractFactory.sample(contentPartnerName = "old")
        val contract = contentPartnerContractRepository.create(original)

        val updateNameCommand = ContentPartnerContractUpdateCommand.ReplaceContentPartnerName(
            contractContentPartnerId = contract.id,
            contentPartnerName = "new"
        )
        val result = contentPartnerContractRepository.update(
            listOf(
                updateNameCommand
            )
        )

        assertThat(result).hasSize(1)
        assertThat(result.first().contract).isEqualTo(contract.copy(contentPartnerName = "new"))
        assertThat(result.first().commands).containsExactly(updateNameCommand)
    }

    @Test
    fun `group update result by contract`() {
        val firstContract = ContentPartnerContractFactory.sample(contentPartnerName = "first", id = ObjectId().toHexString())
        val secondContract = ContentPartnerContractFactory.sample(contentPartnerName = "second", id = ObjectId().toHexString())

        contentPartnerContractRepository.create(firstContract)
        contentPartnerContractRepository.create(secondContract)

        val updateFirstContract = ContentPartnerContractUpdateCommand.ReplaceContentPartnerName(
            contractContentPartnerId = firstContract.id,
            contentPartnerName = "first new"
        )

        val updateFirstContractAgain = ContentPartnerContractUpdateCommand.ReplaceYearsForMaximumLicense(
            contractContentPartnerId = firstContract.id,
            yearsForMaximumLicense = 10
        )

        val updateSecondContract = ContentPartnerContractUpdateCommand.ReplaceContentPartnerName(
            contractContentPartnerId = secondContract.id,
            contentPartnerName = "second new"
        )

        val updateResults = contentPartnerContractRepository.update(
            listOf(
                updateFirstContract,
                updateFirstContractAgain,
                updateSecondContract
            )
        )

        assertThat(updateResults).hasSize(2)

        assertThat(updateResults[0].contract.id).isEqualTo(firstContract.id)
        assertThat(updateResults[0].commands).hasSize(2)

        assertThat(updateResults[1].contract.id).isEqualTo(secondContract.id)
        assertThat(updateResults[1].commands).hasSize(1)
    }

    @Nested
    inner class FindAll {
        @Test
        fun `can find all contracts`() {
            val contracts = listOf(
                ContentPartnerContractFactory.sample(id = ObjectId().toHexString()),
                ContentPartnerContractFactory.sample(id = ObjectId().toHexString())
            )

            contracts.map { contentPartnerContractRepository.create(it) }

            val retrievedContracts = contentPartnerContractRepository.findAll(PageRequest(size = 10, page = 0))
            assertThat(retrievedContracts.elements.map { it.id }).containsExactlyInAnyOrder(*contracts.map { it.id }
                .toTypedArray())
            assertThat(retrievedContracts.pageInfo.hasMoreElements).isFalse()
        }

        @Test
        fun `returns correct page information`() {
            val contracts = listOf(
                ContentPartnerContractFactory.sample(id = ObjectId().toHexString()),
                ContentPartnerContractFactory.sample(id = ObjectId().toHexString())
            )

            contracts.map { contentPartnerContractRepository.create(it) }

            val retrievedContracts = contentPartnerContractRepository.findAll(PageRequest(size = 1, page = 0))
            assertThat(retrievedContracts.pageInfo.hasMoreElements).isTrue()
            assertThat(retrievedContracts.pageInfo.totalElements).isEqualTo(2)
            assertThat(retrievedContracts.pageInfo.pageRequest.page).isEqualTo(0)
            assertThat(retrievedContracts.pageInfo.pageRequest.size).isEqualTo(1)
        }

        @Test
        fun `fetching the last page returns the correct size`() {
            val contracts = listOf(
                ContentPartnerContractFactory.sample(id = ObjectId().toHexString()),
                ContentPartnerContractFactory.sample(id = ObjectId().toHexString()),
                ContentPartnerContractFactory.sample(id = ObjectId().toHexString())
            )

            contracts.map { contentPartnerContractRepository.create(it) }

            val retrievedContracts = contentPartnerContractRepository.findAll(PageRequest(size = 2, page = 1))
            assertThat(retrievedContracts.pageInfo.hasMoreElements).isFalse()
            assertThat(retrievedContracts.pageInfo.totalElements).isEqualTo(3)
            assertThat(retrievedContracts.pageInfo.pageRequest.page).isEqualTo(1)
            assertThat(retrievedContracts.pageInfo.pageRequest.size).isEqualTo(2)
        }

        @Test
        fun `find all by name filter`() {
            val contracts = listOf(
                contentPartnerContractRepository.create(ContentPartnerContractFactory.sample(contentPartnerName = "a")),
                contentPartnerContractRepository.create(ContentPartnerContractFactory.sample(contentPartnerName = "a"))
            )
                contentPartnerContractRepository.create(ContentPartnerContractFactory.sample(contentPartnerName = "b"))

            val retrievedContracts = contentPartnerContractRepository.findAll(listOf(ContractFilter.NameFilter("a")))

            assertThat(retrievedContracts).containsExactly(*contracts.toTypedArray())
        }
    }

    @Nested
    inner class FindAllByIds {
        @Test
        fun `can fetch all ids`() {
            val contracts = listOf(
                ContentPartnerContractFactory.sample(id = ObjectId().toHexString(), contentPartnerName = "contract 1"),
                ContentPartnerContractFactory.sample(id = ObjectId().toHexString(), contentPartnerName = "contract 2")
            )

            contracts.map { contentPartnerContractRepository.create(it) }

            val retrievedContracts = contentPartnerContractRepository.findAllByIds(contracts.map { it.id })
            assertThat(retrievedContracts).hasSize(2)
            assertThat(retrievedContracts.map { it.contentPartnerName }).containsExactlyInAnyOrder(
                "contract 1",
                "contract 2"
            )
        }

        @Test
        fun `ignores missing ids`() {
            val contracts = listOf(
                ContentPartnerContractFactory.sample(id = ObjectId().toHexString(), contentPartnerName = "contract 1"),
                ContentPartnerContractFactory.sample(id = ObjectId().toHexString(), contentPartnerName = "contract 2")
            )

            contentPartnerContractRepository.create(contracts.first())

            val retrievedContracts = contentPartnerContractRepository.findAllByIds(contracts.map { it.id })
            assertThat(retrievedContracts).containsExactly(contracts.first())
        }
    }
}
