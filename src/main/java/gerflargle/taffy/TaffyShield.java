package gerflargle.taffy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

@Mod("taffyshield")
public class TaffyShield {

    static final String MODID = "taffyshield";
    private static HashMap<UUID, BlockPos> players = new HashMap<>();
    static ArrayList<UUID> toRemove = new ArrayList<>();
    private static final Logger LOGGER = LogManager.getLogger();
    private static int trigger = 0;

    public TaffyShield() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientLoad);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ShieldConfig.CLIENT_SPEC);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void clientLoad(final FMLCommonSetupEvent event) {
        LOGGER.info("Taffy Shield Starting up");
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        ShieldCommand.register(event.getCommandDispatcher());
    }

    static void shield(PlayerEntity spe, boolean state) {
        ITextComponent msg = new StringTextComponent("");
        if(state) {
            msg.appendText(ShieldConfig.languageShieldsUp);
        } else {
            msg.appendText(ShieldConfig.languageShieldsDown);
        }
        spe.sendMessage(msg);
        if(ShieldConfig.showGlowing) {
            spe.getEntity().setGlowing(state);
        }
        spe.setInvulnerable(state);
    }

    private static void removeFromList(UUID uuid) {
        players.remove(uuid);
    }

    static boolean addPlayer(PlayerEntity spe) {
        BlockPos blockPos = spe.getPosition();//new BlockPos(spe.lastTickPosX,spe.lastTickPosY,spe.lastTickPosZ);
        if(players.containsKey(spe.getUniqueID())) {
            players.replace(spe.getUniqueID(), blockPos);
        } else {
            players.put(spe.getUniqueID(), blockPos);
        }
        return players.containsKey(spe.getUniqueID());
    }

    private static void processEvent(PlayerEntity spe) {
        if(ShieldConfig.removeOnMove) {
            addPlayer(spe);
        }
        shield(spe, true);
    }

    private static void processTick() {
        if(trigger == ShieldConfig.tickRate) {
            trigger = 0;
            if(!players.isEmpty()) {
                PlayerList playerList = ServerLifecycleHooks.getCurrentServer().getPlayerList();
                players.forEach((k, v) -> {
                    PlayerEntity spe = playerList.getPlayerByUUID(k);
                    if(spe != null && !spe.getPosition().equals(v) && players.containsKey(k)) {
                        shield(spe, false);
                        toRemove.add(spe.getUniqueID());
                    }
                });
                if(!toRemove.isEmpty()) {
                    toRemove.forEach(TaffyShield::removeFromList);
                    toRemove.clear();
                }
            }
        }
        trigger++;
    }

    @Mod.EventBusSubscriber(value= Dist.DEDICATED_SERVER)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
            if(ShieldConfig.applyOnLogin) {
                processEvent(event.getPlayer());
            }
        }

        @SubscribeEvent
        public static void onPlayerReSpawn(PlayerEvent.PlayerRespawnEvent event) {
            if(ShieldConfig.applyOnReSpawn) {
                processEvent(event.getPlayer());
            }
        }

        @SubscribeEvent
        public static void processPlayers(TickEvent.ServerTickEvent event) {
            processTick();
        }
    }

    @Mod.EventBusSubscriber(value= Dist.CLIENT)
    public static class RegistryClientEvents {
        @SubscribeEvent
        public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
            if (ShieldConfig.applyOnLogin) {
                processEvent(event.getPlayer());
            }
        }

        @SubscribeEvent
        public static void onPlayerReSpawn(PlayerEvent.PlayerRespawnEvent event) {
            if (ShieldConfig.applyOnLogin) {
                processEvent(event.getPlayer());
            }
        }

        @SubscribeEvent
        public static void processPlayers(TickEvent.ClientTickEvent event) {
            processTick();
        }
    }
}
