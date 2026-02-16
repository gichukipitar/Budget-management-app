package com.sirhpitar.budget.utils;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Callable;

public final class ReactorBlocking {

    private ReactorBlocking() {
    }

    public static <T> Mono<T> mono(Callable<T> task) {
        return Mono.fromCallable(task)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public static Mono<Void> run(Runnable task) {
        return Mono.fromRunnable(task)
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}