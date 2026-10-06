package com.example.statushud;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class StatusHudClient implements ClientModInitializer {

    private static final EquipmentSlot[] SLOTS = {
        EquipmentSlot.MAINHAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST,
        EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.OFFHAND
    };

    @Override
    public void onInitializeClient() {
        HudElementRegistry.addLast(
            Identifier.of("statushud", "overlay"),
            (ctx, tickCounter) -> render(ctx)
        );
    }

    private static void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        TextRenderer tr = mc.textRenderer;
        int x = 4;
        int y = 4;

        // --- Ping ---
        String pingText;
        int pingColor;
        PlayerListEntry entry = mc.getNetworkHandler() == null
            ? null : mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        if (mc.isInSingleplayer() || entry == null) {
            pingText = "Ping: local";
            pingColor = 0xAAAAAA;
        } else {
            int ping = entry.getLatency();
            pingText = "Ping: " + ping + " ms";
            pingColor = ping < 80 ? 0x55FF55 : ping < 150 ? 0xFFFF55 : 0xFF5555;
        }
        ctx.drawTextWithShadow(tr, pingText, x, y, 0xFF000000 | pingColor);
        y += 14;

        // --- Durability (held items + armor) ---
        for (EquipmentSlot slot : SLOTS) {
            ItemStack stack = mc.player.getEquippedStack(slot);
            if (stack.isEmpty() || !stack.isDamageable()) continue;

            int max = stack.getMaxDamage();
            int left = max - stack.getDamage();
            float pct = (float) left / max;
            int color = pct > 0.5f ? 0x55FF55 : pct > 0.25f ? 0xFFFF55 : 0xFF5555;

            ctx.drawItem(stack, x, y - 4);
            ctx.drawTextWithShadow(tr, left + " / " + max, x + 20, y, 0xFF000000 | color);
            y += 18;
        }
    }
}
