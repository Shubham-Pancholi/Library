package com.project.library.patron.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.project.library.TestcontainersConfiguration;

@SpringBootTest 
@Import (TestcontainersConfiguration.class)
public class PatronBlockConcurrencyTest {

    @Autowired 
    private PatronService patronService;

    @Autowired 
    private PatronRepository patronRepository;

    @Autowired 
    private AdministrativeBlockHistoryRepository historyRepository;

    @Autowired 
    private PlatformTransactionManager transactionManager;

    @Autowired 
    private JdbcTemplate jdbcTemplate;

    @Test 
    public void test() throws Exception {
        Patron patron = new Patron(PatronType.MEMBER, "FName", "LName", "0");

        UUID patronId = patronRepository.saveAndFlush(patron).getId();

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try (var executor = Executors.newFixedThreadPool(2)) {
            Future<Boolean> future1 = executor.submit(
                () -> attemptBlock(patronId, "Reason A", ready, start)
            );

            Future<Boolean> future2 = executor.submit(
                () -> attemptBlock(patronId, "Reason B", ready, start)
            );

            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            boolean firstSucceeded = future1.get(10, TimeUnit.SECONDS);
            boolean secondSucceeded = future2.get(10, TimeUnit.SECONDS);

            assertThat(firstSucceeded).isNotEqualTo(secondSucceeded);
            String reason = firstSucceeded ? "Reason A" : "Reason B";

            Patron loadedPatron = patronRepository.findById(patronId).orElseThrow();

            assertThat(loadedPatron.isAdministrativelyBlocked()).isTrue();
            assertThat(loadedPatron.getAdministrativeBlockReason()).isEqualTo(reason);
            assertThat(loadedPatron.getEligibilityVersion()).isEqualTo(1);

            List<AdministrativeBlockHistory> patronRecords = historyRepository.findAll().stream().filter(r -> r.getPatronId().equals(patronId)).toList();

            assertThat(patronRecords.size()).isEqualTo(1);
            assertThat(patronRecords.get(0).getAction()).isEqualTo(AdministrativeBlockAction.BLOCKED);
            assertThat(patronRecords.get(0).getReason()).isEqualTo(reason);

        }   finally {
            var fixtureHistory = historyRepository.findAll().stream()
                .filter(entry -> entry.getPatronId().equals(patronId))
                .toList();

            historyRepository.deleteAll(fixtureHistory);
            patronRepository.deleteById(patronId);
        }
    }

    private void createJwtStaffTokenAndAddItToContext() {
        Jwt staffJwt = Jwt.withTokenValue("staff-test-token")
                        .header("alg", "RS256")
                        .issuer("http://com.project.library/realms/library")
                        .subject("staff-test-subject")
                        .claim("scope", "openid")
                        .claim("realm_access", Map.of("roles", List.of("STAFF")))
                        .build();

        JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(staffJwt, List.of(new SimpleGrantedAuthority("ROLE_STAFF")));

        SecurityContextHolder.getContext().setAuthentication(jwtAuthenticationToken);
    }

    private boolean attemptBlock(UUID patronId, String reason, CountDownLatch ready, CountDownLatch start) throws InterruptedException {
        createJwtStaffTokenAndAddItToContext();

        try {
            ready.countDown();
            assertThat(start.await(10, TimeUnit.SECONDS)).isTrue();
            patronService.blockPatron(patronId, reason);
                return true;
        }   catch (AdministrativeBlockConflictException exception) {
                assertThat(exception.getMessage()).isEqualTo("Invalid Blocking operation: patron already blocked");
                return false;
        }   finally {
                SecurityContextHolder.clearContext();
        }
    }

    @Test
    public void test2() throws Exception {
        var transactionTemplate = new TransactionTemplate(transactionManager);
        var blockApplied = new CountDownLatch(1);
        var allowCommit = new CountDownLatch(1);

        var firstPid = new AtomicInteger();
        var secondPid = new AtomicInteger();
        var secondReady = new CountDownLatch(1);

        Patron patron = new Patron(PatronType.MEMBER, "FName", "LName", "0");
        UUID patronId = patronRepository.saveAndFlush(patron).getId();

        try (var executor = Executors.newFixedThreadPool(2)) {
            Future<?> future = executor.submit(() -> {
                createJwtStaffTokenAndAddItToContext();
                try {
                    transactionTemplate.executeWithoutResult(status -> {
                        firstPid.set(jdbcTemplate.queryForObject("select pg_backend_pid()", Integer.class));
                        patronService.blockPatron(patronId, "Reason A");
                        blockApplied.countDown();

                        try {
                            assertThat(allowCommit.await(30, TimeUnit.SECONDS)).isTrue();
                        } catch (InterruptedException exception) {
                            Thread.currentThread().interrupt();
                            throw new IllegalStateException("Worker interrupted", exception);
                        }
                    });
                } finally {
                    SecurityContextHolder.clearContext();
                }
            });

            Future<?> future2;

            try {
                assertThat(blockApplied.await(10, TimeUnit.SECONDS)).isTrue();

                Patron uncommittedPatron = patronRepository.findById(patronId).orElseThrow();
                assertThat(uncommittedPatron.isAdministrativelyBlocked()).isFalse();
                assertThat(uncommittedPatron.getEligibilityVersion()).isEqualTo(0);

                future2 = executor.submit(() -> {
                    createJwtStaffTokenAndAddItToContext();
                    try {
                        transactionTemplate.executeWithoutResult(status -> {
                            secondPid.set(jdbcTemplate.queryForObject("select pg_backend_pid()", Integer.class));
                            secondReady.countDown();
                            patronService.blockPatron(patronId, "Reason B");
                        });
                    }   finally {
                        SecurityContextHolder.clearContext();
                    }
                });

                assertThat(secondReady.await(10, TimeUnit.SECONDS)).isTrue();

                await().atMost(5, TimeUnit.SECONDS).until(() ->
                    Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                        "select ? = any(pg_blocking_pids(?))",
                        Boolean.class,
                        firstPid.get(),
                        secondPid.get()
                    ))
                );
            } finally {
                allowCommit.countDown();
            }

            future.get(10, TimeUnit.SECONDS);

            assertThatThrownBy(() -> future2.get(10, TimeUnit.SECONDS))
                .isInstanceOf(ExecutionException.class)
                .cause()
                .isInstanceOf(AdministrativeBlockConflictException.class)
                .hasMessage("Invalid Blocking operation: patron already blocked");

            Patron committedPatron = patronRepository.findById(patronId).orElseThrow();
            assertThat(committedPatron.isAdministrativelyBlocked()).isTrue();
            assertThat(committedPatron.getAdministrativeBlockReason()).isEqualTo("Reason A");
            assertThat(committedPatron.getEligibilityVersion()).isEqualTo(1);

            List<AdministrativeBlockHistory> patronRecords = historyRepository.findAll().stream()
                .filter(r -> r.getPatronId().equals(patronId))
                .toList();

            assertThat(patronRecords.size()).isEqualTo(1);
            assertThat(patronRecords.get(0).getAction()).isEqualTo(AdministrativeBlockAction.BLOCKED);
            assertThat(patronRecords.get(0).getReason()).isEqualTo("Reason A");

        } finally {
            var fixtureHistory = historyRepository.findAll().stream()
                .filter(entry -> entry.getPatronId().equals(patronId))
                .toList();

            historyRepository.deleteAll(fixtureHistory);
            patronRepository.deleteById(patronId);
        }
    }
}