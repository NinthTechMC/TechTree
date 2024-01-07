package pistonmc.techtree.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import pistonmc.techtree.ModMain;
import pistonmc.techtree.adapter.INetworkServer;
import pistonmc.techtree.adapter.IPlayerData;
import pistonmc.techtree.adapter.IPlayerServerSide;
import pistonmc.techtree.event.Msg;
import pistonmc.techtree.event.MsgSyncInit;
import pistonmc.techtree.event.MsgSyncObtainItem;

/**
 * Server progress manager that owns the progress for all players
 */
public class ProgressServer {
    private TechTree tree;
    private INetworkServer<Msg> network;
    private AtomicLong nextCorrelationId = new AtomicLong(0);
    private HashMap<String, PlayerProgress<ItemUnionPaged>> progresses = new HashMap<>();

    public ProgressServer(TechTree tree, INetworkServer<Msg> network) {
        this.tree = tree;
        this.network = network;
    }

    public TechTree getTree() {
        return this.tree;
    }

    public void onPlayerLoad(IPlayerServerSide player, IPlayerData data) {
        ModMain.log.info("Loading progress for player " + player.getClientPlayerName());
        PlayerProgress<ItemUnionPaged> progress = this.ensureProgress(player);
        progress.loadFrom(data);
        player.markDirty();
    }

    public void onPlayerSave(IPlayerServerSide player, IPlayerData data) {
        ModMain.log.info("Saving progress for player " + player.getClientPlayerName());
        PlayerProgress<ItemUnionPaged> progress = this.ensureProgress(player);
        progress.saveTo(data);
    }

    public void onPlayerObtainItem(IPlayerServerSide player, ItemSpecSingle item) {
        Msg msg = this.processObtainItem(player, item);
        if (msg != null) {
            this.network.sendToPlayer(msg, player);
        }
    }

    public MsgSyncObtainItem processObtainItem(IPlayerServerSide player, ItemSpecSingle item) {
        if (!this.tree.isItemInvolved(item)) {
            return null;
        }
        PlayerProgress<ItemUnionPaged> progress = this.ensureProgress(player);
        ItemUnionPaged obtained = progress.getObtained();
        if (obtained.contains(item)) {
            return null;
        }
        ModMain.log.info("Player " + player.getClientPlayerName() + " obtained item " + item);
        obtained.union(item);
        return new MsgSyncObtainItem(item);
    }

    /**
     * Called when client notifies server that it has read a page
     */
    public void onReadPage(IPlayerServerSide player, String pageId) {
        PlayerProgress<ItemUnionPaged> progress = this.ensureProgress(player);
        if (progress.removeNewPage(pageId)) {
            ModMain.log.info("Player " + player.getClientPlayerName() + " read page " + pageId);
            player.markDirty();
        }
    }

    /**
     * Called when client discovers new pages
     */
    public void onNewPages(IPlayerServerSide player, List<String> pageIds) {
        PlayerProgress<ItemUnionPaged> progress = this.ensureProgress(player);
        ModMain.log.info("Player " + player.getClientPlayerName() + " unlocked pages " + pageIds);
        progress.addNewPages(pageIds);
        player.markDirty();
    }

    public void sendProgressTo(IPlayerServerSide player) {
        String name = player.getClientPlayerName();
        PlayerProgress<ItemUnionPaged> progress = this.ensureProgress(player);

        long nextCorrelationId = this.nextCorrelationId.getAndIncrement();
        ModMain.log.info("Sending progress to player " + name + " with correlation " + nextCorrelationId);
        ItemUnionPaged obtained = progress.getObtained();
        int pages = obtained.pages();
        if (pages == 0) {
            List<String> initEntryPages = new ArrayList<>(progress.getNewPages());
            this.network.sendToPlayer(new MsgSyncInit(obtained, nextCorrelationId, 0, initEntryPages), player);
        } else {
            for (int i = 0; i < pages; i++) {
                List<String> initEntryPages = null;
                if (i == 0) {
                    initEntryPages = new ArrayList<>(progress.getNewPages());
                }
                this.network.sendToPlayer(new MsgSyncInit(obtained, nextCorrelationId, i, initEntryPages), player);
            }
        }
    }

    private PlayerProgress<ItemUnionPaged> ensureProgress(IPlayerServerSide player) {
        String name = player.getClientPlayerName();
        PlayerProgress<ItemUnionPaged> progress = this.progresses.get(name);
        if (progress == null) {
            progress = new PlayerProgress<>(new ItemUnionPaged());
            ModMain.log.info("Creating new progress for player " + name);
            player.markDirty();
            this.progresses.put(name, progress);
        }
        return progress;
    }


}
