package com.example.passmanagersvc.service

import com.example.passmanagersvc.application.port.input.PassOwnerServiceInputPort
import com.example.passmanagersvc.application.port.input.PassServiceInputPort
import com.example.passmanagersvc.application.port.input.PassTypeServiceInPort
import com.example.passmanagersvc.application.port.output.TransferPassMessageProducerOutPort
import com.example.passmanagersvc.application.port.output.TransferPassStatisticsMessageProducerOutPort
import com.example.passmanagersvc.application.port.service.PassManagementService
import com.example.passmanagersvc.util.PassFixture.mongoPassFromDb
import com.example.passmanagersvc.util.PassFixture.passFromDb
import com.example.passmanagersvc.util.PassFixture.singlePassId
import com.example.passmanagersvc.util.PassFixture.singlePassType
import com.example.passmanagersvc.util.PassOwnerFixture.passOwnerFromDb
import com.example.passmanagersvc.util.PassOwnerFixture.passOwnerIdFromDb
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.extension.ExtendWith
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.test.test
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
internal class PassManagementServiceImplTest {
    @MockK
    private lateinit var passOwnerServiceInputPort: PassOwnerServiceInputPort

    @MockK
    private lateinit var passServiceInputPort: PassServiceInputPort

    @RelaxedMockK
    private lateinit var transferPassMessageProducerOutPort: TransferPassMessageProducerOutPort

    @MockK
    private lateinit var passTypeServiceInPort: PassTypeServiceInPort

    @RelaxedMockK
    private lateinit var transferPassStatisticsMessageProducerOutPort: TransferPassStatisticsMessageProducerOutPort

    @InjectMockKs
    private lateinit var passManagementService: PassManagementService

    @Test
    fun `cancel pass should delete pass from client list if client id and pass is are valid`() {
        // GIVEN
        every { passOwnerServiceInputPort.getById(any()) } returns passOwnerFromDb.toMono()
        every { passServiceInputPort.deleteById(any()) } returns Unit.toMono()

        // WHEN
        val cancel = passManagementService.cancelPass(passOwnerIdFromDb, singlePassId)

        // THEN
        cancel.test()
            .expectNext(Unit)
            .verifyComplete()

        verify {
            passOwnerServiceInputPort.getById(any())
            passServiceInputPort.deleteById(any())
        }
    }

    @Test
    fun `transferring pass to another client should complete if all ids are valid`() {
        // GIVEN
        every { passServiceInputPort.getById(any()) } returns passFromDb.toMono()
        every { passOwnerServiceInputPort.getById(any()) } returns passOwnerFromDb.toMono()

        val updatedPass = passFromDb.copy(passOwnerId = passOwnerFromDb.id.toString())

        every { passServiceInputPort.update(any()) } returns updatedPass.toMono()
        every { transferPassMessageProducerOutPort.sendTransferPassMessage(any(), any(), any()) } returns Unit.toMono()

        // WHEN
        val transfer = passManagementService.transferPass(singlePassId, passOwnerIdFromDb)

        // THEN
        transfer.test()
            .expectNext(Unit)
            .verifyComplete()

        verifyOrder {
            passServiceInputPort.getById(any())
            passOwnerServiceInputPort.getById(any())
            passServiceInputPort.update(any())
            transferPassMessageProducerOutPort.sendTransferPassMessage(any(), any(), any())
        }
    }

    @Test
    fun `publishing transfer pass stats message should result in calling kafka producer to publish on topic`() {
        // GIVEN
        val previousOwnerId = mongoPassFromDb.passOwnerId.toString()

        every { passTypeServiceInPort.getById(any()) } returns singlePassType.toMono()

        every {
            transferPassStatisticsMessageProducerOutPort.sendTransferPassStatisticsMessage(any(), any())
        } returns Unit.toMono()

        // WHEN
        val publishStatistics = passManagementService.publishTransferPassStatistics(
            passFromDb,
            previousOwnerId
        )

        // THEN
        publishStatistics.test()
            .expectNext(Unit)
            .verifyComplete()

        verifyOrder {
            passTypeServiceInPort.getById(any())
            transferPassStatisticsMessageProducerOutPort.sendTransferPassStatisticsMessage(any(), any())
        }
    }
}
