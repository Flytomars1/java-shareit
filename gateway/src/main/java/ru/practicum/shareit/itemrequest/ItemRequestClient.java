package ru.practicum.shareit.itemrequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;

import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.itemrequest.dto.ItemRequestCreateDto;

import java.util.Map;

@Service
public class ItemRequestClient extends BaseClient {

    private static final String API_PREFIX = "/requests";

    @Autowired
    public ItemRequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + "/server" + API_PREFIX))
                        .build()
        );
    }

    public ResponseEntity<Object> create(Long userId, ItemRequestCreateDto requestDto) {
        return post("", userId, requestDto);
    }

    public ResponseEntity<Object> getOwnRequests(Long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> getAllRequests(Long userId, Integer from, Integer size) {
        Map<String, Object> params = Map.of("from", from, "size", size);
        return get("/all?from={from}&size={size}", userId, params);
    }

    public ResponseEntity<Object> getRequest(Long userId, Long requestId) {
        return get("/" + requestId, userId);
    }
}