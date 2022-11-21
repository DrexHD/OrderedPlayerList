package me.drex.orderedplayerlist.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.drex.orderedplayerlist.config.ConfigManager;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class OrderedPlayerListCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("ordered-playerlist").requires(src -> Permissions.check(src, "orderedplayerlist.main", 2)).then(
                        Commands.literal("reload").requires(src -> Permissions.check(src, "orderedplayerlist.reload", 2)).executes(OrderedPlayerListCommand::reload)
                )
        );
    }

    public static int reload(CommandContext<CommandSourceStack> ctx) {
        if (ConfigManager.load()) {
            ctx.getSource().sendSuccess(Component.literal("Reloaded Ordered Player List config"), false);
            return Command.SINGLE_SUCCESS;
        } else {
            ctx.getSource().sendFailure(Component.literal("Something went wrong, while reloading the config! Check the console for more information!"));
            return 0;
        }
    }

}
