package com.perfumeria.rabbitadmin.api;

import com.perfumeria.rabbitadmin.service.RabbitAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RabbitAdminControllerTest {

    private MockMvc mockMvc;
    private RecordingRabbitAdminOperations rabbitAdminService;

    @BeforeEach
    void setUp() {
        rabbitAdminService = new RecordingRabbitAdminOperations();
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new RabbitAdminController(rabbitAdminService))
            .setControllerAdvice(new ApiExceptionHandler())
            .setValidator(validator)
            .build();
    }

    @Test
    void createsQueueWhenRequestIsValid() throws Exception {
        mockMvc.perform(post("/api/rabbitmq/queues")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"orders.demo","durable":true,"exclusive":false,"autoDelete":false}
                    """))
            .andExpect(status().isCreated());

        assertEquals("orders.demo", rabbitAdminService.createdQueue.name());
    }

    @Test
    void rejectsBlankQueueNameBeforeCallingService() throws Exception {
        mockMvc.perform(post("/api/rabbitmq/queues")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":" ","durable":true,"exclusive":false,"autoDelete":false}
                    """))
            .andExpect(status().isBadRequest());

        assertEquals(null, rabbitAdminService.createdQueue);
    }

    @Test
    void rejectsIncompleteDeadLetterConfiguration() throws Exception {
        mockMvc.perform(post("/api/rabbitmq/queues")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"orders.demo","durable":true,"exclusive":false,"autoDelete":false,
                     "deadLetterExchange":"perfumeria.dead-letter"}
                    """))
            .andExpect(status().isBadRequest());

        assertEquals(null, rabbitAdminService.createdQueue);
    }

    private static class RecordingRabbitAdminOperations implements com.perfumeria.rabbitadmin.service.RabbitAdminOperations {
        private QueueRequest createdQueue;

        @Override
        public void createQueue(QueueRequest request) {
            createdQueue = request;
        }

        @Override
        public void deleteQueue(String name) {}

        @Override
        public void createExchange(ExchangeRequest request) {}

        @Override
        public void deleteExchange(String name) {}

        @Override
        public void createBinding(BindingRequest request) {}

        @Override
        public void deleteBinding(BindingRequest request) {}
    }
}
