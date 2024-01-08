package pistonmc.techtree.mc7.data;

import net.minecraft.nbt.NBTTagCompound;
import pistonmc.techtree.adapter.IMapReader;
import pistonmc.techtree.adapter.IMapWriter;

public class NBTTagCompoundWrapper implements IMapReader, IMapWriter {

    private NBTTagCompound inner;
    public NBTTagCompoundWrapper(NBTTagCompound inner) {
        this.inner = inner;
    }

    @Override
    public void writeBoolean(String key, boolean value) {
        this.inner.setBoolean(key, value);
    }

    @Override
    public void writeByte(String key, byte value) {
        this.inner.setByte(key, value);
    }

    @Override
    public void writeShort(String key, short value) {
        this.inner.setShort(key, value);
    }

    @Override
    public void writeInt(String key, int value) {
        this.inner.setInteger(key, value);
    }

    @Override
    public void writeLong(String key, long value) {
        this.inner.setLong(key, value);
    }

    @Override
    public void writeFloat(String key, float value) {
        this.inner.setFloat(key, value);
    }

    @Override
    public void writeDouble(String key, double value) {
        this.inner.setDouble(key, value);
    }

    @Override
    public void writeString(String key, String value) {
        this.inner.setString(key, value);
    }

    @Override
    public void writeByteArray(String key, byte[] value) {
        this.inner.setByteArray(key, value);
    }

    @Override
    public void writeIntArray(String key, int[] value) {
        this.inner.setIntArray(key, value);
    }

    @Override
    public IMapWriter writeMap(String key) {
        NBTTagCompound tag = new NBTTagCompound();
        this.inner.setTag(key, tag);
        return new NBTTagCompoundWrapper(tag);
    }

    @Override
    public boolean hasKey(String key) {
        return this.inner.hasKey(key);
    }

    @Override
    public boolean readBoolean(String key) {
        return this.inner.getBoolean(key);
    }

    @Override
    public byte readByte(String key) {
        return this.inner.getByte(key);
    }

    @Override
    public short readShort(String key) {
        return this.inner.getShort(key);
    }

    @Override
    public int readInt(String key) {
        return this.inner.getInteger(key);
    }

    @Override
    public long readLong(String key) {
        return this.inner.getLong(key);
    }

    @Override
    public float readFloat(String key) {
        return this.inner.getFloat(key);
    }

    @Override
    public double readDouble(String key) {
        return this.inner.getDouble(key);
    }

    @Override
    public String readString(String key) {
        return this.inner.getString(key);
    }

    @Override
    public byte[] readByteArray(String key) {
        return this.inner.getByteArray(key);
    }

    @Override
    public int[] readIntArray(String key) {
        return this.inner.getIntArray(key);
    }

    @Override
    public IMapReader readMap(String key) {
        NBTTagCompound tag = this.inner.getCompoundTag(key);
        if (tag == null) {
            return null;
        }
        return new NBTTagCompoundWrapper(tag);
    }
}
