package com.example.passmanagersvc.pass.application.service

import com.example.passmanagersvc.pass.application.port.input.PassServiceInPort
import com.example.passmanagersvc.pass.application.port.output.TransferPassMessageProducerOutPort
import com.example.passmanagersvc.pass.application.port.output.TransferPassStatisticsMessageProducerOutPort
import com.example.passmanagersvc.passowner.application.port.input.PassOwnerServiceInPort
import com.example.passmanagersvc.passtype.application.port.input.PassTypeServiceInPort
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
    private lateinit var passOwnerServiceInPort: PassOwnerServiceInPort

    @MockK
    private lateinit var passServiceInPort: PassServiceInPort

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
        every { passOwnerServiceInPort.getById(any()) } returns passOwnerFromDb.toMono()
        every { passServiceInPort.deleteById(any()) } returns Unit.toMono()

        // WHEN
        val cancel = passManagementService.cancelPass(passOwnerIdFromDb, singlePassId)

        // THEN
        cancel.test()
            .expectNext(Unit)
            .verifyComplete()

        verify {
            passOwnerServiceInPort.getById(any())
            passServiceInPort.deleteById(any())
        }
    }

    @Test
    fun `transferring pass to another client should complete if all ids are valid`() {
        // GIVEN
        every { passServiceInPort.getById(any()) } returns passFromDb.toMono()
        every { passOwnerServiceInPort.getById(any()) } returns passOwnerFromDb.toMono()

        val updatedPass = passFromDb.copy(passOwnerId = passOwnerFromDb.id.toString())

        every { passServiceInPort.update(any()) } returns updatedPass.toMono()
        every { transferPassMessageProducerOutPort.sendTransferPassMessage(any(), any(), any()) } returns Unit.toMono()

        // WHEN
        val transfer = passManagementService.transferPass(singlePassId, passOwnerIdFromDb)

        // THEN
        transfer.test()
            .expectNext(Unit)
            .verifyComplete()

        verifyOrder {
            passServiceInPort.getById(any())
            passOwnerServiceInPort.getById(any())
            passServiceInPort.update(any())
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
