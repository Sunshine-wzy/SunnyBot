package io.github.sunshinewzy.sunnybot.module.server.ping;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Optional;

public class StatusResponse {
    private static final String FAVICON_PREFIX = "data:image/png;base64,";
    

    private Description description;
    private Players players;
    private Version version;
    private String favicon;
    private int time;

    
    public Description getDescription() {
        return description;
    }
    
    @NotNull
    public String getDescriptionText() {
        return Optional.ofNullable(getDescription())
                .map(Description::getText)
                .orElse("?");
    }
    
    @NotNull
    public String getDescriptionExtraContent() {
        return Optional.ofNullable(getDescription())
                .map(Description::getExtraContent)
                .orElse("?");
    }
    
    @NotNull
    public String getDescriptionContent() {
        return getDescriptionText() + "\n" + getDescriptionExtraContent();
    }

    public Players getPlayers() {
        return players;
    }
    
    @NotNull
    public String getPlayersNumber() {
        Players p = getPlayers();
        if(p != null) {
            return p.getOnline() + "/" + p.getMax();
        }
        return "?";
    }
    

    public Version getVersion() {
        return version;
    }
    
    @NotNull
    public String getVersionName() {
        Version ver = getVersion();
        if(ver != null) {
            return ver.getName();
        }
        return "?";
    }
    
    @NotNull
    public String getVersionProtocol() {
        Version ver = getVersion();
        if(ver != null) {
            return String.valueOf(ver.getProtocol());
        }
        return "?";
    }

    
    public String getFavicon() {
        return favicon;
    }
    
    @Nullable
    public String getFaviconBase64() {
        String favicon = getFavicon();
        int index = favicon.indexOf(FAVICON_PREFIX);
        if(index == -1) return null;
        
        return favicon.substring(index + FAVICON_PREFIX.length());
    }
    
    @Nullable
    public ByteArrayInputStream getFaviconInputStream() {
        String base64 = getFaviconBase64();
        if(base64 == null) return null;

        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            return new ByteArrayInputStream(bytes);
        } catch (Exception ex) {
            return null;
        }
    }
    
    @Nullable
    public BufferedImage getFaviconImage() {
        String base64 = getFaviconBase64();
        if(base64 == null) return null;

        byte[] bytes = Base64.getDecoder().decode(base64);
        try(ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            return ImageIO.read(inputStream);
        } catch (Exception ex) {
            return null;
        }
    }
    

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
    
}
