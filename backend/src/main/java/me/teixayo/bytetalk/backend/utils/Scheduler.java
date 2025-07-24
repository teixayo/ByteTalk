package me.teixayo.bytetalk.backend.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Scheduler {

    private static ScheduledExecutorService timerExecutionService = null;

    static {
        timerExecutionService = Executors.newSingleThreadScheduledExecutor();
    }

    public static void runTaskLater(Runnable task, long delay, TimeUnit timeUnit) {
        timerExecutionService.schedule(task, delay, timeUnit);
    }
}
