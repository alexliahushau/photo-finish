package com.lvs.photofinish.communication.protocol.message;

public enum MessageType {

    TEST(0),
    READY(1),
    RACE(2),
    TRACK_1_FINISH(3),
    TRACK_2_FINISH(4),
    FINISH(5);

    private byte value;

    MessageType(Integer value) {
        this.value = value.byteValue();
    }

    public byte getValue() {
        return value;
    }

    //private byte[] bytes;

    /*public byte[] getBytes() {
        if (bytes != null) {
            return bytes;
        }

        final byte[] bytes = {Integer.valueOf(this.ordinal()).byteValue(), 0, 0, 0};
        setCheckSum(bytes);

        return bytes;
    }*/

    /*private static void setCheckSum(byte[] bytes) {
        bytes[bytes.length - 1] = getCheckSum(bytes);
    }*/



    public static MessageType valueOf(byte input) {
        for (MessageType item : MessageType.values()) {
            if (item.getValue() == input) {
                return item;
            }
        }

        throw new RuntimeException("Unknown track protocol message type");
    }

    /*byte data1;
    byte data2;
    byte checksum;*/


}
