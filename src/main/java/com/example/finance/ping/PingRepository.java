package com.example.finance.ping;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PingRepository {

    private final JdbcTemplate jdbc;

    public PingRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public String currentDbTime() {
        return jdbc.queryForObject(
                "SELECT TO_CHAR(SYSTIMESTAMP, 'YYYY-MM-DD HH24:MI:SS') FROM dual",
                String.class);
    }
}