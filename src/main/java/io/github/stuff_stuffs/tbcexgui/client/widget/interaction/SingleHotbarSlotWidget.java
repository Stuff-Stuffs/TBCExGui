package io.github.stuff_stuffs.tbcexgui.client.widget.interaction;

import io.github.stuff_stuffs.tbcexgui.client.util.NinePatch;
import io.github.stuff_stuffs.tbcexgui.client.util.Rect2d;
import io.github.stuff_stuffs.tbcexgui.client.widget.AbstractWidget;
import io.github.stuff_stuffs.tbcexgui.client.widget.WidgetPosition;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

public class SingleHotbarSlotWidget extends AbstractWidget {
    private static final Map<NinePatch.Part, Sprite> SPRITE_MAP;
    private static final Map<NinePatch.Part, Sprite> SELECTED_SPRITE_MAP;
    private static boolean RELOAD_SPRITE_MAP = true;
    private final WidgetPosition position;
    private final double size;
    private final DoubleSupplier borderWidth;
    private final BooleanSupplier selected;
    private final BiConsumer<SingleHotbarSlotWidget, Integer> onClick;
    private final BiConsumer<SingleHotbarSlotWidget, Integer> onRelease;
    private final BiConsumer<SingleHotbarSlotWidget, Double> mouseScroll;
    private final Consumer<SingleHotbarSlotWidget> onFocus;
    private final Consumer<SingleHotbarSlotWidget> onLoseFocus;
    private @Nullable ItemStackLike itemStackLike;

    public SingleHotbarSlotWidget(final WidgetPosition position, final double size, final DoubleSupplier borderWidth, final BooleanSupplier selected, final BiConsumer<SingleHotbarSlotWidget, Integer> onClick, final BiConsumer<SingleHotbarSlotWidget, Integer> onRelease, final BiConsumer<SingleHotbarSlotWidget, Double> mouseScroll, final Consumer<SingleHotbarSlotWidget> onFocus, final Consumer<SingleHotbarSlotWidget> onLoseFocus, @Nullable final ItemStackLike itemStackLike) {
        this.position = position;
        this.size = size;
        this.borderWidth = borderWidth;
        this.selected = selected;
        this.onClick = onClick;
        this.onRelease = onRelease;
        this.mouseScroll = mouseScroll;
        this.onFocus = onFocus;
        this.onLoseFocus = onLoseFocus;
        this.itemStackLike = itemStackLike;
    }

    @Override
    public void setFocused(final boolean focused) {
        super.setFocused(focused);
        if (focused) {
            onFocus.accept(this);
        } else {
            onLoseFocus.accept(this);
        }
    }

    public void setItemStackLike(@Nullable final ItemStackLike itemStackLike) {
        this.itemStackLike = itemStackLike;
    }

    public @Nullable ItemStackLike getItemStackLike() {
        return itemStackLike;
    }

    @Override
    public WidgetPosition getWidgetPosition() {
        return position;
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        final Rect2d rect = new Rect2d(position.getX(), position.getY(), position.getX() + size, position.getY() + size);
        if (rect.isIn(mouseX, mouseY)) {
            onClick.accept(this, button);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
        final Rect2d rect = new Rect2d(position.getX(), position.getY(), position.getX() + size, position.getY() + size);
        if (rect.isIn(mouseX, mouseY)) {
            onRelease.accept(this, button);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(final double mouseX, final double mouseY, final int button, final double deltaX, final double deltaY) {
        final Rect2d rect = new Rect2d(position.getX(), position.getY(), position.getX() + size, position.getY() + size);
        return rect.isIn(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double amount) {
        final Rect2d rect = new Rect2d(position.getX(), position.getY(), position.getX() + size, position.getY() + size);
        if (rect.isIn(mouseX, mouseY)) {
            mouseScroll.accept(this, amount);
            return true;
        }
        return false;
    }

    @Override
    public void render(final MatrixStack matrices, final double mouseX, final double mouseY, final float delta) {
        if (RELOAD_SPRITE_MAP) {
            reloadSpriteMap();
            RELOAD_SPRITE_MAP = false;
        }
        final Map<NinePatch.Part, Sprite> spriteMap = selected.getAsBoolean() ? SELECTED_SPRITE_MAP : SPRITE_MAP;
        NinePatch.render(spriteMap, position.getX(), position.getY(), size, size, getHorizontalPixel(), getVerticalPixel(), borderWidth.getAsDouble(), matrices);
        if (itemStackLike != null) {
            itemStackLike.render(matrices, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean keyPress(final int keyCode, final int scanCode, final int modifiers) {
        return false;
    }

    public interface ItemStackLike {
        void render(MatrixStack matrices, double mouseX, double mouseY, float delta);
    }

    private static void reloadSpriteMap() {
        Identifier base = new Identifier("tbcexgui", "gui/hotbar/single");
        for (final NinePatch.Part part : NinePatch.Part.values()) {
            SPRITE_MAP.put(part, MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).apply(part.append(base)));
        }
        base = new Identifier("tbcexgui", "gui/hotbar/single/selected");
        for (final NinePatch.Part part : NinePatch.Part.values()) {
            SELECTED_SPRITE_MAP.put(part, MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).apply(part.append(base)));
        }
    }

    static {
        SPRITE_MAP = new EnumMap<>(NinePatch.Part.class);
        SELECTED_SPRITE_MAP = new EnumMap<>(NinePatch.Part.class);
        ClientSpriteRegistryCallback.event(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).register((atlasTexture, registry) -> RELOAD_SPRITE_MAP = true);
    }
}