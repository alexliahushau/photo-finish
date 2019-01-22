package com.lvs.photofinish.communication.protocol.message;

import java.util.Arrays;

public class Message {

    public static final int MESSAGE_LENGTH = 4;

    private final MessageType type;
    private final byte[] data;
    private final byte[] bytes;

    /*public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        MessageType type;
        byte[] data;

        public Builder type(MessageType type) {
            this.type = type;
            return this;
        }

        public Builder data(byte[] data) {
            this.data = data;
            return this;
        }

        public Message build() {
            if (type != null && data != null) {
                return new Message(type, data);
            }

            return new Message(type);
        }
    }*/

    public Message(final byte[] bytes) {
        final byte checkSum = bytes[bytes.length - 1];

        if (checkSum != getCheckSum(bytes)) {
            throw new RuntimeException("Message checksum mismatch");
        }

        this.type = MessageType.valueOf(bytes[0]);
        this.data = Arrays.copyOfRange(bytes, 1, bytes.length - 1);
        this.bytes = bytes;
    }

    public Message(final MessageType type) {
        this(type, new byte[2]);
    }

    public Message(final MessageType type, final byte[] data) {
        final byte[] msg = Arrays.copyOf(new byte[]{type.getValue()}, data.length + 2);

        msg[msg.length - 1] = getCheckSum(msg);

        if (msg.length > MESSAGE_LENGTH) {
            throw new RuntimeException("Message has wrong format");
        }

        this.type = type;
        this.data = data;
        this.bytes = msg;
    }

    private byte getCheckSum(byte[] bytes) {
        byte sum = 0;
        for (int i = 0; i < bytes.length - 1; i++) {
            sum ^= bytes[i];
        }
        return sum;
    }

    public byte[] getBytes() {
        return Arrays.copyOf(bytes, bytes.length);
    }

    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }

    public MessageType getType() {
        return type;
    }
}
