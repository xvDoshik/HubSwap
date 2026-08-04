package ru.heldyy.hubswap.config;

import com.google.gson.annotations.Expose;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ModConfig {
    @Expose
    private int classicDelay = 1500;

    @Expose
    private int clickDelay = 200;

    @Expose
    private String classicCommand = "cn";

    @Expose
    private String lightCommand = "ln";

    @Expose
    private String light120Command = "ln120";

    @Expose
    private String primeCommand = "pr";

    @Expose
    private boolean notificationsEnabled = true;

    @Expose
    private boolean smartAutoTuneEnabled = true;

    @Expose
    private ColorTheme colorTheme = ColorTheme.AQUA;

    @Expose
    private String linkColor = ChatFormatting.GOLD.getName();

    @Expose
    private int learnedHubOffset = 0;

    @Expose
    private int learnedClickOffset = 0;

    @Expose
    private int successStreak = 0;

    @Expose
    private int failStreak = 0;

    @Expose
    private List<HotkeySlot> hotkeySlots = createDefaultHotkeySlots();

    private static List<HotkeySlot> createDefaultHotkeySlots() {
        List<HotkeySlot> list = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            list.add(new HotkeySlot());
        }
        return list;
    }

    public List<HotkeySlot> getHotkeySlots() {
        while (hotkeySlots.size() < 8) hotkeySlots.add(new HotkeySlot());
        return hotkeySlots;
    }

    public int getClassicDelay() {
        return classicDelay;
    }

    public int getClickDelay() {
        return clickDelay;
    }

    public String getClassicCommand() {
        return classicCommand;
    }

    public String getLightCommand() {
        return lightCommand;
    }

    public String getLight120Command() {
        return light120Command;
    }

    public String getPrimeCommand() {
        return primeCommand;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public boolean isSmartAutoTuneEnabled() {
        return smartAutoTuneEnabled;
    }

    public ColorTheme getColorTheme() {
        return colorTheme;
    }

    public ChatFormatting getLinkColor() {
        for (ChatFormatting formatting : ChatFormatting.values()) {
            if (formatting.getName().equalsIgnoreCase(linkColor)) {
                return formatting;
            }
        }
        return ChatFormatting.GOLD;
    }

    public int getLearnedHubOffset() {
        return learnedHubOffset;
    }

    public int getLearnedClickOffset() {
        return learnedClickOffset;
    }

    public int getSuccessStreak() {
        return successStreak;
    }

    public int getFailStreak() {
        return failStreak;
    }

    public void setDelays(int classicDelay, int clickDelay) {
        this.classicDelay = clamp(classicDelay, 100, 5000);
        this.clickDelay = clamp(clickDelay, 50, 1000);
    }

    public void setCommands(String classicCommand, String lightCommand, String light120Command, String primeCommand) {
        this.classicCommand = validateCommand(classicCommand, "cn");
        this.lightCommand = validateCommand(lightCommand, "ln");
        this.light120Command = validateCommand(light120Command, "ln120");
        this.primeCommand = validateCommand(primeCommand, "pr");
    }

    public void setNotificationsEnabled(boolean enabled) {
        this.notificationsEnabled = enabled;
    }

    public void setSmartAutoTuneEnabled(boolean enabled) {
        this.smartAutoTuneEnabled = enabled;
    }

    public void setColorTheme(ColorTheme theme) {
        this.colorTheme = theme == null ? ColorTheme.AQUA : theme;
    }

    public void setLinkColor(ChatFormatting color) {
        this.linkColor = color == null ? ChatFormatting.GOLD.getName() : color.getName();
    }

    public void recordSuccess() {
        failStreak = 0;
        successStreak = Math.min(100, successStreak + 1);

        if (successStreak >= 2) {
            learnedHubOffset = Math.max(0, learnedHubOffset - 50);
            learnedClickOffset = Math.max(0, learnedClickOffset - 10);
            successStreak = 0;
        }
    }

    public void recordFailure() {
        successStreak = 0;
        failStreak = Math.min(100, failStreak + 1);

        learnedHubOffset = clamp(learnedHubOffset + 100 + (failStreak >= 2 ? 50 : 0), 0, 2500);
        learnedClickOffset = clamp(learnedClickOffset + 20 + (failStreak >= 2 ? 6 : 0), 0, 500);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private String validateCommand(String command, String defaultCommand) {
        if (command == null || command.trim().isEmpty()) {
            Minecraft client = Minecraft.getInstance();
            if (client != null) {
                client.execute(() -> {
                    if (client.player != null) {
                        client.player.displayClientMessage(
                                Component.literal("[HubSwap] Используется команда по умолчанию: " + defaultCommand),
                                false
                        );
                    }
                });
            }
            return defaultCommand;
        }

        String cleaned = command.trim()
                .replace("/", "")
                .replaceAll("[^a-zA-Z0-9_а-яА-ЯёЁ]", "");

        if (cleaned.length() > 10) {
            cleaned = cleaned.substring(0, 10);
        }

        return cleaned.isEmpty() ? defaultCommand : cleaned;
    }

    public enum ColorTheme {
        AQUA("Синяя", ChatFormatting.AQUA, 0x00d9ff),
        RED("Красная", ChatFormatting.RED, 0xff4444),
        BLUE("Голубая", ChatFormatting.BLUE, 0x5555ff),
        GREEN("Зелёная", ChatFormatting.GREEN, 0x55ff55),
        LIGHT_PURPLE("Фиолетовая", ChatFormatting.LIGHT_PURPLE, 0xff55ff),
        GOLD("Золотая", ChatFormatting.GOLD, 0xffaa00);

        private final String displayName;
        private final ChatFormatting formatting;
        private final int rgbColor;

        ColorTheme(String displayName, ChatFormatting formatting, int rgbColor) {
            this.displayName = displayName;
            this.formatting = formatting;
            this.rgbColor = rgbColor;
        }

        public String getDisplayName() {
            return displayName;
        }

        public ChatFormatting getFormatting() {
            return formatting;
        }

        public int getRgbColor() {
            return rgbColor;
        }

        public ColorTheme next() {
            ColorTheme[] values = values();
            return values[(this.ordinal() + 1) % values.length];
        }
    }
}
