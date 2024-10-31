package com.plaything.api.common.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


import java.util.concurrent.atomic.AtomicLong;

@Component
public class IdGenerator {

    //2024-10-29-0-0-0가 기준 시간
    private final long baseTimestamp = 1730160000000L;
    private final long machineId = 1L;

    private static final AtomicLong sequence = new AtomicLong();
    private static final int MACHINE_ID_BITS = 10;
    private static final int SEQUENCE_BITS = 12;

    public long generateId(Long currentTimestamp) {
        long timestamp = currentTimestamp - baseTimestamp;
        return (timestamp << MACHINE_ID_BITS + SEQUENCE_BITS) | (machineId << SEQUENCE_BITS) | value(sequence.getAndIncrement(), SEQUENCE_BITS, 0);
    }

    private long value(long value, int bits, int shift) {
        long mask = (((1L << bits) - 1) << shift);
        return (value & mask) >> shift;
    }
}
