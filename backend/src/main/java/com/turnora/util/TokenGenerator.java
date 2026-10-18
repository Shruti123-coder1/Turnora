package com.turnora.util;

import org.springframework.stereotype.Component;

// Makes token numbers like F-001, F-002 ... (office prefix + 3 digit number)
@Component
public class TokenGenerator {

    public String format(String prefix, int sequenceNo) {
        return prefix + "-" + String.format("%03d", sequenceNo);
    }
}
