package org.litebridge.core.dto;

import jakarta.annotation.Nullable;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

public final class TrackedDtoStack {

    private Deque<TrackedDto> trackedDtos = new ConcurrentLinkedDeque<>();

    public void addTrackedDto(final TrackedDto trackedDto) {
        trackedDtos.add(trackedDto);
    }

    public @Nullable TrackedDto removeLastTrackedDto() {
        return trackedDtos.pollLast();
    }
}
