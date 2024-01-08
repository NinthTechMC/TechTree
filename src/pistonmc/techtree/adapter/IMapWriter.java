package pistonmc.techtree.adapter;

public interface IMapWriter {
    public void writeBoolean(String key, boolean value);
    public void writeByte(String key, byte value);
    public void writeShort(String key, short value);
    public void writeInt(String key, int value);
    public void writeLong(String key, long value);
    public void writeFloat(String key, float value);
    public void writeDouble(String key, double value);
    public void writeString(String key, String value);
    public void writeByteArray(String key, byte[] value);
    public void writeIntArray(String key, int[] value);
    public IMapWriter writeMap(String key);
}
