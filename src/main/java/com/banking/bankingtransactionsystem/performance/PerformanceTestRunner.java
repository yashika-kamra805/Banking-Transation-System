package com.banking.bankingtransactionsystem.performance;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.banking.bankingtransactionsystem.service.TransactionService;

@Profile("performance")
@Component
public class PerformanceTestRunner implements CommandLineRunner {

    private final TransactionService transactionService;

    public PerformanceTestRunner(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    private void runBenchmark(int workerCount) throws Exception {

        int totalTransactions = 50_000; // Trial first

        int successful = 0;
        int failed = 0;

        String runId = UUID.randomUUID().toString();

        ExecutorService executor =
                Executors.newFixedThreadPool(workerCount);

        int batchSize = workerCount * 100;

        long startTime = System.nanoTime();

        try {
            for (int batchStart = 0;
                 batchStart < totalTransactions;
                 batchStart += batchSize) {

                int batchEnd = Math.min(
                        batchStart + batchSize,
                        totalTransactions
                );

                List<Future<Boolean>> batchResults = new ArrayList<>();

                for (int i = batchStart; i < batchEnd; i++) {

                    final int transactionNumber = i;

                    Callable<Boolean> task = () -> {
                        try {
                            long accountId =
                                    5L + (transactionNumber % 4);

                            transactionService.deposit(
                                    accountId,
                                    new BigDecimal("1.00"),
                                    "Performance benchmark deposit",
                                    "PERF-" + workerCount + "-"
                                            + runId + "-" + transactionNumber
                            );

                            return true;

                        } catch (Exception exception) {
                            return false;
                        }
                    };

                    batchResults.add(executor.submit(task));
                }

                for (Future<Boolean> result : batchResults) {
                    if (result.get()) {
                        successful++;
                    } else {
                        failed++;
                    }
                }
            }

        } finally {
            executor.shutdown();

            if (!executor.awaitTermination(10, TimeUnit.MINUTES)) {
                executor.shutdownNow();
                throw new RuntimeException("Benchmark timed out");
            }
        }

        long endTime = System.nanoTime();

        double totalSeconds =
                (endTime - startTime) / 1_000_000_000.0;

        double tps = successful / totalSeconds;

        System.out.println("\n========== BENCHMARK RESULTS ==========");
        System.out.println("Workers: " + workerCount);
        System.out.println("Total transactions: " + totalTransactions);
        System.out.println("Successful: " + successful);
        System.out.println("Failed: " + failed);
        System.out.println("Total time: " + totalSeconds + " seconds");
        System.out.println("TPS: " + tps);
        System.out.println("=======================================");
    }

    @Override
    public void run(String... args) throws Exception {

        int[] workerCounts = {1, 5, 10, 20};

        for (int workers : workerCounts) {

            System.out.println(
                    "\nStarting benchmark with " + workers + " workers"
            );

            runBenchmark(workers);
        }
    }
}
