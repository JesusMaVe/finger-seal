package com.dataforge.connection;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/connections")
public class ConnectionController {

    private final ConnectionService service;

    public ConnectionController(ConnectionService service) {
        this.service = service;
    }

    @GetMapping
    public List<ConnectionConfig> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public ConnectionConfig get(@PathVariable Long id) {
        return service.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Connection not found"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConnectionConfig create(@RequestBody ConnectionConfig config) {
        return service.save(config);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping("/test")
    public void test(@RequestBody ConnectionConfig config) {
        if (!service.test(config)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Connection failed");
        }
    }

    @PostMapping("/{id}/test")
    public void testExisting(@PathVariable Long id) {
        if (!service.test(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Connection failed");
        }
    }
}
