package org.roost.roost.client.screen;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.roost.roost.screen.RoostScreenHandler;

public class RoostScreen extends HandledScreen<RoostScreenHandler> {

    private static final Identifier TEXTURE =
            Identifier.of("roost", "textures/gui/roost_gui.png");

    // Coordenadas de la flecha llena en el PNG
    private static final int ARROW_U = 175;
    private static final int ARROW_V = 240;
    private static final int ARROW_WIDTH = 27;  // 202 - 176
    private static final int ARROW_HEIGHT = 16; // 256 - 240

    // Posición de la flecha en la GUI (relativa al origen x,y del fondo)
    private static final int ARROW_X = 48;
    private static final int ARROW_Y = 53;

    public RoostScreen(RoostScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 166;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        // Fondo de la GUI
        context.drawTexture(TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        // Flecha de progreso
        int progress = this.handler.getProgress();
        int maxProgress = this.handler.getMaxProgress();

        if (maxProgress > 0 && progress > 0) {
            int filledWidth = (int) ((float) progress / maxProgress * ARROW_WIDTH);
            context.drawTexture(
                    TEXTURE,
                    x + ARROW_X, y + ARROW_Y,
                    ARROW_U, ARROW_V,
                    filledWidth, ARROW_HEIGHT
            );
        }
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(this.textRenderer, this.title, 8, this.backgroundHeight - 128 + 2, 0x404040, false);
        context.drawText(this.textRenderer, this.playerInventoryTitle, 8, this.backgroundHeight - 95 + 2, 0x404040, false);
    }
}