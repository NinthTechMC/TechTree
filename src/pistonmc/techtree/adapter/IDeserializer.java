package pistonmc.techtree.adapter;

import java.nio.charset.StandardCharsets;

public interface IDeserializer {
    public boolean readBoolean();
    public char readChar();
    public byte readByte();
    public short readShort();
    public int readInt();
    public long readLong();
    public float readFloat();
    public double readDouble();

    public default String readString() {
        boolean isNull = readBoolean();
        if (isNull) {
            return null;
        }
        byte[] bytes = readByteArray();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public default byte[] readByteArray() {
        int length = readInt();
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = readByte();
        }
        return bytes;
    }
}
