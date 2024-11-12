package com.example.gateway.rest

import com.example.gateway.dto.PassDto
import com.example.gateway.mapper.rest.CreatePassResponseMapper.toCreatePassRequest
import com.example.gateway.util.PassDtoFixture.passDto
import com.example.gateway.util.PassDtoFixture.passId
import com.example.gateway.util.PassProtoFixture
import com.example.gateway.util.PassProtoFixture.cancelPassRequest
import com.example.gateway.util.PassProtoFixture.deletePassByIdRequest
import com.example.gateway.util.PassProtoFixture.protoPass
import com.example.gateway.util.PassProtoFixture.succesfulCancelPassResponse
import com.example.gateway.util.PassProtoFixture.succesfulDeletePassByIdResponse
import com.example.gateway.util.PassProtoFixture.successfulCreatePassResponse
import com.example.gateway.util.PassProtoFixture.successfulFindPassByIdResponse
import com.example.gateway.util.PassProtoFixture.successfulTransferPassResponse
import com.example.gateway.util.PassProtoFixture.transferPassRequest
import com.example.internal.NatsSubject.Pass.CANCEL
import com.example.internal.NatsSubject.Pass.CREATE
import com.example.internal.NatsSubject.Pass.DELETE_BY_ID
import com.example.internal.NatsSubject.Pass.FIND_BY_ID
import com.example.internal.NatsSubject.Pass.TRANSFER
import com.example.internal.input.reqreply.CancelPassResponse
import com.example.internal.input.reqreply.CreatePassResponse
import com.example.internal.input.reqreply.DeletePassByIdResponse
import com.example.internal.input.reqreply.FindPassByIdResponse
import com.example.internal.input.reqreply.TransferPassResponse
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import reactor.kotlin.core.publisher.toMono

@WebFluxTest(PassController::class)
internal class PassControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    private lateinit var natsClient: NatsClient

    @Test
    fun `find by id should return pass by id`() {
        // GIVEN
        every {
            natsClient.request(
                FIND_BY_ID, PassProtoFixture.findPassByIdRequest(passId), FindPassByIdResponse.parser()
            )
        } returns successfulFindPassByIdResponse(protoPass).toMono()

        // WHEN // THEN
        webTestClient.get().uri("$URL/$passId").exchange().expectStatus().isOk.expectHeader()
            .contentType(MediaType.APPLICATION_JSON).expectBody<PassDto>().isEqualTo(passDto)
    }

    @Test
    fun `creating pass should create a new pass`() {
        // GIVEN
        every {
            natsClient.request(
                CREATE, passDto.toCreatePassRequest(), CreatePassResponse.parser()
            )
        } returns successfulCreatePassResponse(protoPass).toMono()

        // WHEN // THEN
        webTestClient.post().uri(URL).contentType(MediaType.APPLICATION_JSON).bodyValue(passDto).exchange()
            .expectStatus().isCreated.expectBody<PassDto>().isEqualTo(passDto)
    }

    @Test
    fun `canceling a pass should cancel a pass`() {
        // GIVEN

        every {
            natsClient.request(
                CANCEL, cancelPassRequest(passId, protoPass.passOwnerId), CancelPassResponse.parser()
            )
        } returns succesfulCancelPassResponse.toMono()

        // WHEN // THEN
        webTestClient.post().uri("$URL/$passId/cancel/${protoPass.passOwnerId}").exchange()
            .expectStatus().isOk
    }

    @Test
    fun `transferring a pass should transfer a pass`() {
        // GIVEN
        every {
            natsClient.request(
                TRANSFER, transferPassRequest(passId, protoPass.passOwnerId), TransferPassResponse.parser()
            )
        } returns successfulTransferPassResponse.toMono()

        // WHEN // THEN
        webTestClient.post().uri("$URL/$passId/transfer/${protoPass.passOwnerId}").exchange()
            .expectStatus().isOk
    }

    @Test
    fun `deleting pass by id should delete pass by id`() {
        // GIVEN
        every {
            natsClient.request(
                DELETE_BY_ID, deletePassByIdRequest(passId), DeletePassByIdResponse.parser()
            )
        } returns succesfulDeletePassByIdResponse.toMono()

        // WHEN // THEN
        webTestClient.delete().uri("$URL/$passId").exchange().expectStatus().isNoContent
    }

    private companion object {
        const val URL = "/passes"
    }
}
