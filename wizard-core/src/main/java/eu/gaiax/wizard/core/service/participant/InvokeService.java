package eu.gaiax.wizard.core.service.participant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;

@Slf4j
public class InvokeService {
    public static String executeRequest(String url, HttpMethod method) {
        return executeRequest(url, method, null);
    }

    public static String executeRequest(String url, HttpMethod method, Object body) {
        return executeRequest(url, method, body, null);
    }

    public static String executeRequest(String url, HttpMethod method, Object body, MultiValueMap<String, String> queryParams) {
        WebClient.RequestBodyUriSpec webClient = WebClient.create().method(method);
        webClient.uri(url);
        if (!CollectionUtils.isEmpty(queryParams)) {
            webClient.uri(url, u -> u.queryParams(queryParams).build());
        }
        if (Objects.nonNull(body)) {
            webClient.bodyValue(body);
        }
        return webClient.retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
