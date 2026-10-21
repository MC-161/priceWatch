package dev.pricewatch.poll;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class PollRepository {

    private static final String COLUMNS = "id, target_id, polled_at, status, price, http_status, error";

    private final JdbcClient jdbc;

    public PollRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public PollRecord insert(long targetId, PollOutcome outcome) {
        var stmt = jdbc.sql("""
                        INSERT INTO poll (target_id, status, price, http_status, error, raw_response)
                        VALUES (:targetId, :status, :price, :httpStatus, :error, :raw)
                        RETURNING %s
                        """.formatted(COLUMNS))
                .param("targetId", targetId)
                .param("raw", outcome.rawResponse());
        stmt = switch (outcome) {
            case PollOutcome.Success s -> stmt.param("status", "OK")
                    .param("price", s.price().amount())
                    .param("httpStatus", s.httpStatus())
                    .param("error", null);
            case PollOutcome.ParseFailure f -> stmt.param("status", "PARSE_ERROR")
                    .param("price", null)
                    .param("httpStatus", f.httpStatus())
                    .param("error", f.reason());
            case PollOutcome.FetchFailure f -> stmt.param("status", "FETCH_ERROR")
                    .param("price", null)
                    .param("httpStatus", f.httpStatus())
                    .param("error", f.reason());
        };
        return stmt.query(PollRepository::map).single();
    }

    public List<PollRecord> history(long targetId, int limit) {
        return jdbc.sql("SELECT " + COLUMNS + " FROM poll WHERE target_id = :id ORDER BY polled_at DESC, id DESC LIMIT :limit")
                .param("id", targetId)
                .param("limit", limit)
                .query(PollRepository::map)
                .list();
    }

    private static PollRecord map(ResultSet rs, int row) throws SQLException {
        return new PollRecord(
                rs.getLong("id"),
                rs.getLong("target_id"),
                rs.getTimestamp("polled_at").toInstant(),
                rs.getString("status"),
                rs.getBigDecimal("price"),
                rs.getObject("http_status", Integer.class),
                rs.getString("error"));
    }
}
