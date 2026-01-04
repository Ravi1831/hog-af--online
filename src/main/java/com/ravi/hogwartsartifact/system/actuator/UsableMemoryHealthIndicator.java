package com.ravi.hogwartsartifact.system.actuator;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.health.contributor.Status;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class UsableMemoryHealthIndicator implements HealthIndicator {

    @Override
    public @Nullable Health health() {
        File path = new File(".");
        long diskUsableInByte = path.getUsableSpace();
        boolean isHealth = diskUsableInByte >= 10 * 1024 * 1024; //10mb
        Status status = isHealth ? Status.UP : Status.DOWN;
        return Health
                .status(status)
                .withDetail("usable memory",diskUsableInByte)
                .withDetail("threshold",10 * 1024 * 1024)
                .build();
    }
}
