package pistonmc.techtree.adapter;

public interface IMapReader {
    public boolean hasKey(String key);
    public boolean readBoolean(String key);
    public byte readByte(String key);
    public short readShort(String key);
    public int readInt(String key);
    public long readLong(String key);
    public float readFloat(String key);
    public double readDouble(String key);
    public String readString(String key);
    public byte[] readByteArray(String key);
    public int[] readIntArray(String key);
    public IMapReader readMap(String key);
}
