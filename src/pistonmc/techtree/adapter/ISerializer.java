package pistonmc.techtree.adapter;

import java.nio.charset.StandardCharsets;

public interface ISerializer {
    public void writeBoolean(boolean value);
    public void writeChar(char value);
    public void writeByte(byte value);
    public void writeInt(int value);
    public void writeLong(long value);
    public void writeShort(short value);
    public void writeFloat(float value);
    public void writeDouble(double value);

    public default void writeString(String value) {
        boolean isNull = value == null;
        this.writeBoolean(isNull);
        if (isNull) {
            return;
        }
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        this.writeNonNullByteArray(bytes);
    }

    public default void writeNonNullByteArray(byte[] value) {
        this.writeInt(value.length);
        for (int i = 0; i < value.length; i++) {
            this.writeByte(value[i]);
        }
    }

}
