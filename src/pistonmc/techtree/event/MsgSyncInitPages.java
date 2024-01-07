// package pistonmc.techtree.event;
//
// import java.util.ArrayList;
// import java.util.List;
// import pistonmc.techtree.ModMain;
// import pistonmc.techtree.adapter.IDeserializer;
// import pistonmc.techtree.adapter.ISerializer;
//
// /**
//  * Message sent to the client to initialize new (unread) pages
//  */
// public class MsgSyncInitPages extends Msg {
//     public List<String> pages;
//     public MsgSyncInitPages() {}
//     public MsgSyncInitPages(List<String> pages) {
//         this.pages = pages;
//     }
//
//     @Override
//     public void writeTo(ISerializer serializer) {
//         serializer.writeInt(this.pages.size());
//         for (String page : this.pages) {
//             serializer.writeString(page);
//         }
//     }
//
//     @Override
//     public void readFrom(IDeserializer deserializer) {
//         int len = deserializer.readInt();
//         this.pages = new ArrayList<String>(len);
//         for (int i = 0; i < len; i++) {
//             this.pages.add(deserializer.readString());
//         }
//     }
//
//     @Override
//     public void handleAtClient() {
//         ModMain.getClient().getProgress().onInitNewPages(this.pages);
//     }
//
//     public static byte id;
//     @Override
//     public byte getId() {
//         return id;
//     }
// }
