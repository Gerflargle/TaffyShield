package gerflargle.taffy;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

@Mod.EventBusSubscriber(modid = TaffyShield.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
class ShieldConfig {
    private static final Client CLIENT;
    static final ForgeConfigSpec CLIENT_SPEC;
    static {
        final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT_SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    static int tickRate;
    static boolean applyOnLogin;
    static boolean applyOnReSpawn;
    static boolean removeOnMove;
    static boolean showGlowing;
    static String languageShieldsUp;
    static String languageShieldsDown;

    @SubscribeEvent
    public static void onModConfigEvent(final ModConfig.ModConfigEvent configEvent) {
        if (configEvent.getConfig().getSpec() == ShieldConfig.CLIENT_SPEC) {
            bakeConfig();
        }
    }

    private static void bakeConfig() {
        applyOnLogin = CLIENT.applyOnLogin.get();
        applyOnReSpawn = CLIENT.applyOnReSpawn.get();
        removeOnMove = CLIENT.removeOnMove.get();
        showGlowing = CLIENT.showGlowing.get();
        tickRate = CLIENT.tickRate.get();
        languageShieldsUp = CLIENT.languageShieldsUp.get();
        languageShieldsDown = CLIENT.languageShieldsDown.get();
    }

    public static class Client {

        final ForgeConfigSpec.IntValue tickRate;
        final ForgeConfigSpec.BooleanValue applyOnLogin;
        final ForgeConfigSpec.BooleanValue applyOnReSpawn;
        final ForgeConfigSpec.BooleanValue removeOnMove;
        final ForgeConfigSpec.BooleanValue showGlowing;
        final ForgeConfigSpec.ConfigValue<String> languageShieldsUp;
        final ForgeConfigSpec.ConfigValue<String> languageShieldsDown;

        Client(ForgeConfigSpec.Builder builder) {
            builder.push("features");
            applyOnLogin = builder
                    .comment("Should Shields be applied on login? [Default: true]")
                    .translation(TaffyShield.MODID + ".config." + "applyOnLogin")
                    .define("applyOnLogin", true);
            applyOnReSpawn = builder
                    .comment("Should Shields be applied on reSpawn? [Default: true]")
                    .translation(TaffyShield.MODID + ".config." + "applyOnReSpawn")
                    .define("applyOnReSpawn", true);
            removeOnMove = builder
                    .comment("When Shields are applied on login, should they be removed when the player moves? [Default: true]")
                    .translation(TaffyShield.MODID + ".config." + "removeOnMove")
                    .define("removeOnMove", true);
            showGlowing = builder
                    .comment("Should we show a glow effect when a Shield is up? [Default: true]")
                    .translation(TaffyShield.MODID + ".config." + "showGlowing")
                    .define("showGlowing", true);
            builder.pop();
            builder.push("timing");
            tickRate = builder
                    .comment("How often should we check to disable shields? (20 ticks a second) [Default: 40]")
                    .translation(TaffyShield.MODID + ".config." + "tickRate")
                    .defineInRange("tickRate", 40, 0, 200);
            builder.pop();

            builder.push("language");
            languageShieldsUp = builder
                    .comment("What should be displayed when Shields are enabled [Default: Shields Up]")
                    .translation(TaffyShield.MODID + ".config." + "languageShieldsUp")
                    .define("languageShieldsUp", "Shields Up");
            languageShieldsDown = builder
                    .comment("What should be displayed when Shields are disabled [Default: Shields Down]")
                    .translation(TaffyShield.MODID + ".config." + "languageShieldsDown")
                    .define("languageShieldsDown", "Shields Down");
            builder.pop();
        }
    }
}
