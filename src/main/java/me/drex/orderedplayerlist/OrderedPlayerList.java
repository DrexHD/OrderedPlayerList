package me.drex.orderedplayerlist;

import me.drex.orderedplayerlist.command.OrderedPlayerListCommand;
import me.drex.orderedplayerlist.config.ConfigManager;
import me.drex.orderedplayerlist.util.OrderedPlayerListManager;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderedPlayerList implements DedicatedServerModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("orderedplayerlist");

    @Override
    public void onInitializeServer() {
        ConfigManager.load();
        OrderedPlayerListManager.init();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> OrderedPlayerListCommand.register(dispatcher));
    }

}
