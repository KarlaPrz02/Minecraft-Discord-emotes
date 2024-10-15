package me.kat.discordemojisupport.client.emoji;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.client.texture.TextureUtil;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

public class EmojiTexture extends ResourceTexture {

    private final File cacheFile;
    private final String url;
    private final Runnable onTextureLoadedCallback;

    private boolean loaded;
    private CompletableFuture<?> loader;

    public EmojiTexture(File cacheFile,  Identifier fallbackTexture, String url, Runnable onTextureLoadedCallback) {
        super(fallbackTexture);

        this.cacheFile = cacheFile;
        this.url = url;
        this.onTextureLoadedCallback = onTextureLoadedCallback;

        this.loaded = false;
    }

    private void onTextureLoaded(NativeImage image) {
        this.onTextureLoadedCallback.run();

        MinecraftClient.getInstance().execute(() -> {
            this.loaded = true;
            if (!RenderSystem.isOnRenderThread()) {
                RenderSystem.recordRenderCall(() -> this.uploadTexture(image));
            } else {
                this.uploadTexture(image);
            }
        });
    }

    private void uploadTexture(NativeImage image) {
        TextureUtil.prepareImage(this.getGlId(), image.getWidth(), image.getHeight());
        image.upload(0, 0, 0, true);
    }

    private void loadEmoji(ResourceManager manager) {
        MinecraftClient.getInstance().execute(() -> {
            if (!this.loaded) {
                try {
                    super.load(manager);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                this.loaded = true;
            }
        });

        if (this.loader == null) {
            NativeImage image = null;

            if (this.cacheFile != null && this.cacheFile.isFile()) {
                try (FileInputStream fis = new FileInputStream(this.cacheFile)) {
                    image = NativeImage.read(fis);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (image != null) {
                this.onTextureLoaded(image);
            } else {
                this.loader = CompletableFuture.runAsync(() -> {
                    HttpURLConnection httpURLConnection = null;

                    try {
                        httpURLConnection = (HttpURLConnection) new URL(this.url).openConnection(MinecraftClient.getInstance().getNetworkProxy());
                        httpURLConnection.setDoInput(true);
                        httpURLConnection.setDoOutput(false);
                        httpURLConnection.connect();;

                        if (httpURLConnection.getResponseCode() / 100 == 2) {
                            InputStream inputStream;
                            if (this.cacheFile != null) {
                                FileUtils.copyInputStreamToFile(httpURLConnection.getInputStream(), this.cacheFile);
                                inputStream = Files.newInputStream(this.cacheFile.toPath());
                            } else {
                                inputStream = httpURLConnection.getInputStream();
                            }

                            MinecraftClient.getInstance().execute(() -> {
                                try {
                                    NativeImage loadedImage = NativeImage.read(inputStream);
                                    this.onTextureLoaded(loadedImage);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (httpURLConnection != null) {
                            httpURLConnection.disconnect();
                        }
                    }

                }, Util.getServerWorkerExecutor());
            }
        }
    }
}

































