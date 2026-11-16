package com.example.finance.ping;

import org.springframework.stereotype.Service;

@Service
public class PingService {

    private final PingRepository pingRepository;

    public PingService(PingRepository pingRepository) {
        this.pingRepository = pingRepository;
    }

    public PingResponse ping() {
        return new PingResponse("OK", pingRepository.currentDbTime());
    }
}