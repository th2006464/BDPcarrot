package com.zjfgh.bluedhook.simple;

import java.util.Collections;
import java.util.List;

public class LiveMessageParser {

    public static class ParseResult {
        public ParseResult(String originalString, List<String> keywords, MessageType messageType) {
            // Empty implementation
        }

        public String getOriginalString() {
            return "";
        }

        public List<String> getKeywords() {
            return Collections.emptyList();
        }

        public MessageType getMessageType() {
            return MessageType.UNKNOWN;
        }

        public String getCharSequence() {
            return "";
        }
    }

    public enum MessageType {
        CHAMELEON_LIFE,
        A_DESERT_DREAM,
        HOLY_SWORDSMAN,
        PRIMARY_TREASURE,
        ADVANCED_TREASURE,
        GLOWING_TREASURE,
        MULTIPLIER_REWARD,
        UNKNOWN
    }

    public ParseResult parse(String message) {
        return new ParseResult(message, Collections.emptyList(), MessageType.UNKNOWN);
    }
}
