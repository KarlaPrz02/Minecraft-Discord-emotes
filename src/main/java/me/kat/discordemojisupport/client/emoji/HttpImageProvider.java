package me.kat.discordemojisupport.client.emoji;

import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;

import java.io.File;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class HttpImageProvider {

    private final TextureManager textureManager;
    private final File emojiCacheDir;

    public HttpImageProvider(TextureManager textureManager, File emojiCacheDir) {
        this.textureManager = textureManager;
        this.emojiCacheDir = emojiCacheDir;
    }

    public Identifier loadImage(String name, String httpUri, BiConsumer<Identifier, AbstractTexture> onLoadedCallback) {
        Identifier id = new Identifier("emojis/" + name);
        AbstractTexture abstractTexture = this.textureManager.getTexture(id);

        if (abstractTexture != null) {
            onLoadedCallback.accept(id, abstractTexture);
        } else {
            File file = new File(this.emojiCacheDir,)
        }
    }

}
