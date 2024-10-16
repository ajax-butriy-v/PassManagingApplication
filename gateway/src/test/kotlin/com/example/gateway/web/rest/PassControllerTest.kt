package com.example.gateway.web.rest

import com.example.gateway.configuration.NatsClient
import com.example.gateway.proto.PassDtoFixture.passDto
import com.example.gateway.proto.PassDtoFixture.passFromDto
import com.example.gateway.web.dto.PassDto
import com.example.gateway.web.mapper.proto.pass.CreatePassResponseMapper.toCreatePassRequest
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
import com.example.passmanagersvc.util.PassProtoFixture
import com.example.passmanagersvc.util.PassProtoFixture.cancelPassRequest
import com.example.passmanagersvc.util.PassProtoFixture.deletePassByIdRequest
import com.example.passmanagersvc.util.PassProtoFixture.succesfulCancelPassResponse
import com.example.passmanagersvc.util.PassProtoFixture.succesfulDeletePassByIdResponse
import com.example.passmanagersvc.util.PassProtoFixture.successfulCreatePassResponse
import com.example.passmanagersvc.util.PassProtoFixture.successfulTransferPassResponse
import com.example.passmanagersvc.util.PassProtoFixture.transferPassRequest
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
        val passId = passFromDto.id
        every {
            natsClient.request(
                FIND_BY_ID, PassProtoFixture.findPassByIdRequest(passId), FindPassByIdResponse.parser()
            )
        } returns PassProtoFixture.successfulFindPassByIdResponse(passFromDto).toMono()

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
        } returns successfulCreatePassResponse(passFromDto).toMono()

        // WHEN // THEN
        webTestClient.post().uri(URL).contentType(MediaType.APPLICATION_JSON).bodyValue(passDto).exchange()
            .expectStatus().isCreated.expectBody<PassDto>().isEqualTo(passDto)
    }

    @Test
    fun `canceling a pass should cancel a pass`() {
        // GIVEN
        every {
            natsClient.request(
                CANCEL, cancelPassRequest(passFromDto.id, passFromDto.passOwnerId), CancelPassResponse.parser()
            )
        } returns succesfulCancelPassResponse.toMono()

        // WHEN // THEN
        webTestClient.post().uri("$URL/${passFromDto.id}/cancel/${passFromDto.passOwnerId}").exchange()
            .expectStatus().isOk
    }

    @Test
    fun `transferring a pass should transfer a pass`() {
        // GIVEN
        every {
            natsClient.request(
                TRANSFER, transferPassRequest(passFromDto.id, passFromDto.passOwnerId), TransferPassResponse.parser()
            )
        } returns successfulTransferPassResponse.toMono()

        // WHEN // THEN
        webTestClient.post().uri("$URL/${passFromDto.id}/transfer/${passFromDto.passOwnerId}").exchange()
            .expectStatus().isOk
    }

    @Test
    fun `deleting pass by id should delete pass by id`() {
        // GIVEN
        every {
            natsClient.request(
                DELETE_BY_ID, deletePassByIdRequest(passFromDto.id), DeletePassByIdResponse.parser()
            )
        } returns succesfulDeletePassByIdResponse.toMono()

        // WHEN // THEN
        webTestClient.delete().uri("$URL/${passFromDto.id}").exchange().expectStatus().isNoContent
    }

    private companion object {
        const val URL = "/passes"
    }
}
