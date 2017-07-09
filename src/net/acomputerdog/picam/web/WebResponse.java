package net.acomputerdog.picam.web;

import java.io.IOException;
import java.io.OutputStream;

public class WebResponse {
    private final OutputStream out;
    private int length = 0;

    public WebResponse(OutputStream out) {
        this.out = out;
    }

    public void writeString(String str) throws IOException {
        byte[] bytes = str.getBytes();
        out.write(bytes);
        length += bytes.length;
    }

    public int getLength() {
        return length;
    }
}
