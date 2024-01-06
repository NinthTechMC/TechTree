package pistonmc.techtree.event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import pistonmc.techtree.adapter.IDeserializer;
import pistonmc.techtree.adapter.ISerializer;

public class MsgRegistry {
    private byte nextId = 0;
    private List<Supplier<Msg>> factories = new ArrayList<>();

    /**
     * Register a message
     * 
     * Example: register(x -> MsgFoo.id = x, MsgFoo::new);
     */
    public void register(Consumer<Byte> idAssigner, Supplier<Msg> factory) {
        idAssigner.accept(nextId);
        factories.add(factory);
        nextId++;
    }

    public Msg create(byte id) {
        return factories.get(id).get();
    }

    public void serialize(Msg msg, ISerializer serializer) {
        serializer.writeByte(msg.getId());
        msg.writeTo(serializer);
    }

    public Msg deserialize(IDeserializer deserializer) {
        byte id = deserializer.readByte();
        Msg msg = create(id);
        msg.readFrom(deserializer);
        return msg;
    }
}
