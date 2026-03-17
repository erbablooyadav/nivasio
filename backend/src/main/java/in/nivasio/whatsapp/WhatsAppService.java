package in.nivasio.whatsapp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class WhatsAppService {

    @Value("${app.whatsapp.phone-number-id}")
    private String phoneNumberId;

    @Value("${app.whatsapp.access-token}")
    private String accessToken;

    @Value("${app.whatsapp.mock-mode:true}")
    private boolean mockMode;

    private final WebClient webClient;

    public WhatsAppService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://graph.facebook.com/v22.0")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public Mono<Void> sendOtp(String phone, String otp) {
        if (mockMode) {
            log.info("[MOCK WA OTP] To: {}, OTP: {}", phone, otp);
            return Mono.empty();
        }

        String body = """
                {
                    "messaging_product": "whatsapp",
                    "to": "%s",
                    "type": "template",
                    "template": {
                        "name": "nivasio_otp",
                        "language": { "code": "en_US" },
                        "components": [{
                            "type": "body",
                            "parameters": [{ "type": "text", "text": "%s" }]
                        }]
                    }
                }
                """.formatted(phone, otp);

        return webClient.post()
                .uri("/" + phoneNumberId + "/messages")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(error -> {
                                    log.error("[WA OTP FAILED] To: {}, Error: {}", phone, error);
                                    return Mono.error(new RuntimeException("Failed to send WhatsApp OTP: " + error));
                                }))
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.info("[WA OTP SENT] To: {}", phone))
                .doOnError(e -> log.error("[WA OTP ERROR] {}", e.getMessage()));
    }
}
