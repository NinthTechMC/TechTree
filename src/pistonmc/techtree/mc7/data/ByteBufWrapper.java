package pistonmc.techtree.mc7.data;

import io.netty.buffer.ByteBuf;
import pistonmc.techtree.adapter.IDeserializer;
import pistonmc.techtree.adapter.ISerializer;

public class ByteBufWrapper implements ISerializer, IDeserializer {
    private ByteBuf buf;
    public ByteBufWrapper(ByteBuf buf) {
        this.buf = buf;
    }

    @Override
    public boolean readBoolean() {
        return this.buf.readBoolean();
    }

    @Override
    public char readChar() {
        return this.buf.readChar();
    }

    @Override
    public byte readByte() {
        return this.buf.readByte();
    }

    @Override
    public short readShort() {
        return this.buf.readShort();
    }

    @Override
    public int readInt() {
        return this.buf.readInt();
    }

    @Override
    public long readLong() {
        return this.buf.readLong();
    }

    @Override
    public float readFloat() {
        return this.buf.readFloat();
    }

    @Override
    public double readDouble() {
        return this.buf.readDouble();
    }

    @Override
    public void writeBoolean(boolean value) {
        this.buf.writeBoolean(value);
    }

    @Override
    public void writeChar(char value) {
        this.buf.writeChar(value);
    }

    @Override
    public void writeByte(byte value) {
        this.buf.writeByte(value);
    }

    @Override
    public void writeInt(int value) {
        this.buf.writeInt(value);
    }

    @Override
    public void writeLong(long value) {
        this.buf.writeLong(value);
    }

    @Override
    public void writeShort(short value) {
        this.buf.writeShort(value);
    }

    @Override
    public void writeFloat(float value) {
        this.buf.writeFloat(value);
    }

    @Override
    public void writeDouble(double value) {
        this.buf.writeDouble(value);
    }
}
