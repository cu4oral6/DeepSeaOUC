package com.github.deepseaouc.backend.listener

import com.github.deepseaouc.backend.entity.vo.request.ChatRequestVO
import com.github.deepseaouc.backend.service.ChatHistoryService
import com.github.deepseaouc.backend.service.SseEmitterBrokerService
import com.github.deepseaouc.backend.utils.Const
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component


@Component
class StRequestListener(
    private val chatService: ChatHistoryService,
    private val brokerService: SseEmitterBrokerService // 注入 SseEmitter Broker
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @RabbitListener(queues = [Const.ST_QUEUE_NAME])
    fun handleChatRequest(request: ChatRequestVO) {
        logger.info("Listener starting async stream for session ${request.uuid}")

        val stream = chatService.sendRequestAndHandleStream(request)

        stream
            .doOnNext { content ->
                brokerService.pushMessage(request.uuid, content)
            }
            .doOnComplete {
                brokerService.pushMessage(request.uuid, "[DONE]")
                brokerService.completeSession(request.uuid)
                logger.info("Session ${request.uuid} completed and SSE closed.")
            }
            .doOnError { e ->
                logger.error("ST API stream failed for session ${request.uuid}", e)
                brokerService.pushMessage(request.uuid, "[ERROR]: ${e.message}")
                brokerService.completeSession(request.uuid)
            }
            .subscribe()
    }
}