package ru.randomwalk.matcherservice.service.util;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class VirtualThreadUtil {

    public static <T, R> List<R> getResultsConcurrently(List<T> arguments, Function<T, R> function) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CompletableFuture<?>[] futures = arguments.stream()
                    .map(arg -> CompletableFuture.supplyAsync(() -> function.apply(arg), executor))
                    .toArray(CompletableFuture[]::new);

            return CompletableFuture.allOf(futures)
                    .thenApply(ignored ->
                            Arrays.stream(futures)
                                    .map(future -> (R) future.join())
                                    .toList()
                    ).join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
