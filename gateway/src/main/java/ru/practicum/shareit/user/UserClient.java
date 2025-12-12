package ru.practicum.shareit.user;

import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;

import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.UserDto;

@Service
public class UserClient extends BaseClient {

    private static final String API_PREFIX = "/users";

    @Autowired
    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(HttpClients.createDefault()))
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + "/server" + API_PREFIX))
                        .build()
        );
    }

    public ResponseEntity<Object> create(UserDto userDto) {
        return post("", null, userDto);
    }

    public ResponseEntity<Object> update(Long targetUserId, Long requesterId, UserDto userDto) {
        return patch("/" + targetUserId, requesterId, userDto);
    }

    public ResponseEntity<Object> getUser(Long userId) {
        return get("/" + userId, userId);  // исправлено: передаем userId как userId
    }

    public ResponseEntity<Object> getAllUsers() {
        return get("", null);
    }

    public ResponseEntity<Object> delete(Long targetUserId, Long requesterId) {
        return delete("/" + targetUserId, requesterId);
    }
}