package gerflargle.taffy;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;

import static gerflargle.taffy.TaffyShield.*;

class ShieldCommand {
    private static final SuggestionProvider<CommandSource> STATE_SUGGESTIONS = ((context, builder) ->
            builder
                .suggest("on")
                .suggest("off")
            .suggest("temp").buildFuture());
    private ShieldCommand() {}

   static void register(CommandDispatcher<CommandSource> dispatcher) {
       LiteralArgumentBuilder<CommandSource> builder = Commands.literal("taffy")
               .requires(commandSource -> commandSource.hasPermissionLevel(4));

       builder.then(
           Commands.literal("shield")
               .then(Commands.argument("target", EntityArgument.player())
                   .then(Commands.argument("state", StringArgumentType.word())
                       .suggests(STATE_SUGGESTIONS)
                       .executes(context -> {
                           String state = StringArgumentType.getString(context, "state");
                           PlayerEntity spe = EntityArgument.getPlayer(context, "target");
                           switch (state) {
                               case "on":
                                   shield(spe, true);
                                   break;
                               case "off":
                                   shield(spe, false);
                                   toRemove.add(spe.getUniqueID());
                                   break;
                               case "temp":
                                   if (addPlayer(spe)) {
                                       shield(spe, true);
                                   } else {
                                       context.getSource().sendErrorMessage(new StringTextComponent("Failed to apply a shield"));
                                   }
                                   break;
                               default:
                                   context.getSource().sendErrorMessage(new StringTextComponent("No valid state provided"));
                                   break;
                           }
                           return 1;
                   })))
                   .then(Commands.literal("list").executes(context -> {
                       StringTextComponent msg = new StringTextComponent("Shielded players");
                       ArrayList<String> shielded = new ArrayList<>();
                       List<ServerPlayerEntity> players = context.getSource().getWorld().getPlayers();
                       for(PlayerEntity spe : players) {
                           if(spe.isInvulnerable()) {
                               shielded.add(spe.getDisplayName().getString());
                           }
                       }
                       msg.appendText("(" + shielded.size() + "): " + String.join(", ", shielded));
                       context.getSource().sendFeedback(msg, true);
                       return 1;
                   })));

       dispatcher.register(builder);
   }
}
