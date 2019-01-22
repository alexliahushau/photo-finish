package com.lvs.photofinish.communication;

import java.io.IOException;

public interface CommunicationService {
    void send(byte[] data) throws IOException;
    boolean connect() throws IOException;
    boolean disconnect() throws IOException;
}
