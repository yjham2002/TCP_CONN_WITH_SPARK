package models;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by a on 2017-08-06.
 */
public class TIDBlock {
    private long tid = 0;
    ByteSerial byteSerial;

    public TIDBlock(long tid) {
        this.tid = tid;
    }

    public ByteSerial getByteSerial() {
        return byteSerial;
    }

    public void setByteSerial(ByteSerial byteSerial) {
        this.byteSerial = byteSerial;
    }

    public long getTid() {
        return tid;
    }

    public void setTid(long tid) {
        this.tid = tid;
    }
}
