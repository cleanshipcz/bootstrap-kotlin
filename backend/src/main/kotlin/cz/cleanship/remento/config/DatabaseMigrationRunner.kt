package cz.cleanship.remento.config

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

/**
 * Minimal, idempotent migration runner for local/dev environments.
 *
 * We intentionally keep this small (instead of introducing Flyway/Liquibase) to avoid extra tooling.
 * In production, prefer Flyway/Liquibase.
 */
@Component
class DatabaseMigrationRunner(
    private val jdbcTemplate: JdbcTemplate,
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(DatabaseMigrationRunner::class.java)

    override fun run(args: ApplicationArguments) {
        val product = runCatching {
            jdbcTemplate.dataSource?.connection?.use { it.metaData.databaseProductName }
        }.getOrNull()

        if (product?.contains("PostgreSQL", ignoreCase = true) != true) {
            log.debug("Skipping DB migrations (dbProduct={})", product)
            return
        }

        ensureFlashcardsSubjectId()
    }

    private fun ensureFlashcardsSubjectId() {
        val exists: Boolean = jdbcTemplate.queryForObject(
            """
            select exists (
              select 1
              from information_schema.columns
              where table_schema = 'public'
                and table_name = 'flashcards'
                and column_name = 'subject_id'
            )
            """.trimIndent(),
            Boolean::class.java,
        ) ?: false

        if (exists) {
            return
        }

        log.warn("Applying local DB migration: add flashcards.subject_id + backfill + constraints")

        // 1) Add column (nullable for backfill)
        jdbcTemplate.execute("alter table flashcards add column if not exists subject_id bigint")

        // 2) Backfill from topics
        jdbcTemplate.execute(
            """
            update flashcards f
            set subject_id = t.subject_id
            from topics t
            where f.topic_id = t.id
              and f.subject_id is null
            """.trimIndent(),
        )

        // 3) Add FK (idempotent)
        jdbcTemplate.execute(
            """
            do $$
            begin
              if not exists (select 1 from pg_constraint where conname = 'fk_flashcards_subject') then
                alter table flashcards
                  add constraint fk_flashcards_subject
                  foreign key (subject_id) references subjects(id);
              end if;
            end $$;
            """.trimIndent(),
        )

        // 4) Enforce not-null (will fail fast if backfill wasn't possible)
        jdbcTemplate.execute("alter table flashcards alter column subject_id set not null")
    }
}


