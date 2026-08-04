package ru.heldyy.hubswap.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;
import ru.heldyy.hubswap.HubSwap;
import ru.heldyy.hubswap.client.HubSwapClient;
import ru.heldyy.hubswap.config.HotkeySlot;
import ru.heldyy.hubswap.config.ModConfig;
import ru.heldyy.hubswap.config.StatsData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigScreen extends Screen {
    private static final Minecraft client = Minecraft.getInstance();
    private final Screen parent;
    private final ModConfig config;

    private enum Tab { SETTINGS, HOTKEYS, STATS }
    private Tab currentTab = Tab.SETTINGS;
    private Button tabSettings, tabHotkeys, tabStats;

    private EditBox classicDelayField;
    private EditBox clickDelayField;
    private EditBox classicCommandField;
    private EditBox lightCommandField;
    private EditBox light120CommandField;
    private EditBox primeCommandField;

    private boolean notificationsEnabledTmp;
    private boolean smartAutoTuneEnabledTmp;
    private Button notificationsToggleButton;
    private Button smartAutoTuneToggleButton;

    private ModConfig.ColorTheme currentTheme;
    private Button themeToggleButton;

    private ChatFormatting currentLinkColor;
    private Button linkColorToggleButton;

    private List<HotkeySlot> hotkeyTmp;
    private int listeningSlot = -1;
    private final List<Button> hotkeyKeyBtns = new ArrayList<>();
    private final List<Button> hotkeyModeBtns = new ArrayList<>();
    private final List<EditBox> hotkeyNumFields = new ArrayList<>();
    private final List<Button> hotkeyToggleBtns = new ArrayList<>();

    private Button saveButton;
    private Button cancelButton;

    private float backgroundAlpha = 0.0f;
    private float contentOffset = 20.0f;

    // ── Адаптивный layout ─────────────────────────────────────────────────
    private int margin, panelW, lx, rx, colW, contentY, footerY;

    public ConfigScreen(Screen parent) {
        super(Component.literal("Настройки HubSwap"));
        this.parent = parent;
        this.config = HubSwap.getConfig();
        this.currentTheme = config.getColorTheme();
        this.currentLinkColor = config.getLinkColor();
        this.notificationsEnabledTmp = config.isNotificationsEnabled();
        this.smartAutoTuneEnabledTmp = config.isSmartAutoTuneEnabled();
        this.hotkeyTmp = new ArrayList<>();
        for (HotkeySlot s : config.getHotkeySlots())
            hotkeyTmp.add(new HotkeySlot(s.getKeyCode(), s.getMode(), s.getServerNumber(), s.isEnabled()));
    }

    private void recalcLayout() {
        margin   = Math.max(12, this.width / 14);
        panelW   = this.width - margin * 2;
        lx       = margin;
        rx       = this.width / 2 + 6;
        colW     = this.width / 2 - margin - 6;
        contentY = 100;
        footerY  = this.height - 50;
    }

    @Override
    protected void init() {
        recalcLayout();
        buildPersistentWidgets();
        rebuildTab();
    }

    private void buildPersistentWidgets() {
        int cx   = this.width / 2;
        int tabY = 36;
        int tabW = Math.min(130, panelW / 3 - 4);

        tabSettings = addRenderableWidget(Button.builder(Component.literal("⚙ Настройки"), b -> switchTab(Tab.SETTINGS))
                .bounds(cx - tabW / 2 - tabW - 4, tabY, tabW, 18).build());
        tabHotkeys  = addRenderableWidget(Button.builder(Component.literal("⌨ Хоткеи"),    b -> switchTab(Tab.HOTKEYS))
                .bounds(cx - tabW / 2,             tabY, tabW, 18).build());
        tabStats    = addRenderableWidget(Button.builder(Component.literal("📊 Статистика"), b -> switchTab(Tab.STATS))
                .bounds(cx + tabW / 2 + 4,         tabY, tabW, 18).build());

        int btnW = Math.min(150, panelW / 3);
        saveButton   = addRenderableWidget(Button.builder(Component.literal("✓ Сохранить"), btn -> onSave())
                .bounds(cx - btnW - 4, footerY + 10, btnW, 20).build());
        cancelButton = addRenderableWidget(Button.builder(Component.literal("✕ Отмена"), btn -> onClose())
                .bounds(cx + 4, footerY + 10, btnW, 20).build());
    }

    private void switchTab(Tab tab) {
        currentTab = tab;
        listeningSlot = -1;
        clearWidgets();
        hotkeyKeyBtns.clear();
        hotkeyModeBtns.clear();
        hotkeyNumFields.clear();
        hotkeyToggleBtns.clear();
        recalcLayout();
        buildPersistentWidgets();
        rebuildTab();
    }

    private void rebuildTab() {
        switch (currentTab) {
            case SETTINGS -> buildSettingsTab();
            case HOTKEYS  -> buildHotkeysTab();
            case STATS    -> {}
        }
    }

    // ── Настройки ─────────────────────────────────────────────────────────

    private void buildSettingsTab() {
        int sp = Math.min(46, (footerY - contentY) / 6);
        int fh = 20;

        classicDelayField    = addField(lx, contentY + sp * 0 + 10, colW, fh, String.valueOf(config.getClassicDelay()), 4);
        clickDelayField      = addField(lx, contentY + sp * 1 + 10, colW, fh, String.valueOf(config.getClickDelay()), 4);
        classicCommandField  = addField(lx, contentY + sp * 2 + 10, colW, fh, config.getClassicCommand(), 10);
        lightCommandField    = addField(lx, contentY + sp * 3 + 10, colW, fh, config.getLightCommand(), 10);
        light120CommandField = addField(lx, contentY + sp * 4 + 10, colW, fh, config.getLight120Command(), 10);
        primeCommandField    = addField(lx, contentY + sp * 5 + 10, colW, fh, config.getPrimeCommand(), 10);

        notificationsToggleButton = addRenderableWidget(Button.builder(getNotificationButtonText(),
                        btn -> { notificationsEnabledTmp = !notificationsEnabledTmp; btn.setMessage(getNotificationButtonText()); })
                .bounds(rx, contentY + sp * 0 + 10, colW, fh).build());
        smartAutoTuneToggleButton = addRenderableWidget(Button.builder(getSmartAutoTuneButtonText(),
                        btn -> { smartAutoTuneEnabledTmp = !smartAutoTuneEnabledTmp; btn.setMessage(getSmartAutoTuneButtonText()); })
                .bounds(rx, contentY + sp * 1 + 10, colW, fh).build());
        themeToggleButton = addRenderableWidget(Button.builder(getThemeButtonText(),
                        btn -> { currentTheme = currentTheme.next(); btn.setMessage(getThemeButtonText()); })
                .bounds(rx, contentY + sp * 2 + 10, colW, fh).build());
        linkColorToggleButton = addRenderableWidget(Button.builder(getLinkColorButtonText(),
                        btn -> { currentLinkColor = nextLinkColor(currentLinkColor); btn.setMessage(getLinkColorButtonText()); })
                .bounds(rx, contentY + sp * 3 + 10, colW, fh).build());
    }

    private EditBox addField(int x, int y, int w, int h, String text, int maxLen) {
        EditBox f = new EditBox(font, x, y, w, h, Component.literal(""));
        f.setValue(text);
        f.setMaxLength(maxLen);
        return addRenderableWidget(f);
    }

    // ── Хоткеи ────────────────────────────────────────────────────────────

    private void buildHotkeysTab() {
        int rowH   = Math.min(20, (footerY - contentY - 30) / 9);
        int cardH  = rowH + 2;
        int startY = contentY + 24;

        int totalW = panelW;
        int keyW  = (int)(totalW * 0.30);
        int modeW = (int)(totalW * 0.16);
        int numW  = (int)(totalW * 0.09);
        int togW  = (int)(totalW * 0.12);
        int gap   = (totalW - keyW - modeW - numW - togW) / 3;

        int kx = lx;
        int mx = kx + keyW + gap;
        int nx = mx + modeW + gap;
        int tx = nx + numW + gap;

        for (int i = 0; i < 8; i++) {
            HotkeySlot slot = hotkeyTmp.get(i);
            int y = startY + cardH * i;
            final int idx = i;

            Button keyBtn = addRenderableWidget(Button.builder(
                            Component.literal(slot.getKeyCode() < 0 ? "[ --- ]" : "[ " + getKeyName(slot.getKeyCode()) + " ]"),
                            btn -> { listeningSlot = idx; btn.setMessage(Component.literal("[ нажми... ]")); })
                    .bounds(kx, y, keyW, rowH).build());
            hotkeyKeyBtns.add(keyBtn);

            Button modeBtn = addRenderableWidget(Button.builder(
                            Component.literal(getModeShort(slot.getMode())),
                            btn -> {
                                String next = nextMode(hotkeyTmp.get(idx).getMode());
                                hotkeyTmp.get(idx).setMode(next);
                                btn.setMessage(Component.literal(getModeShort(next)));
                            })
                    .bounds(mx, y, modeW, rowH).build());
            hotkeyModeBtns.add(modeBtn);

            EditBox numField = new EditBox(font, nx, y, numW, rowH, Component.literal(""));
            numField.setValue(String.valueOf(slot.getServerNumber()));
            numField.setMaxLength(3);
            addRenderableWidget(numField);
            hotkeyNumFields.add(numField);

            Button toggleBtn = addRenderableWidget(Button.builder(slotToggleText(slot.isEnabled()),
                            btn -> {
                                boolean cur = hotkeyTmp.get(idx).isEnabled();
                                hotkeyTmp.get(idx).setEnabled(!cur);
                                btn.setMessage(slotToggleText(!cur));
                            })
                    .bounds(tx, y, togW, rowH).build());
            hotkeyToggleBtns.add(toggleBtn);
        }
    }

    private Component slotToggleText(boolean on) {
        return on ? Component.literal("✓ Вкл").withStyle(ChatFormatting.GREEN)
                : Component.literal("✗ Выкл").withStyle(ChatFormatting.RED);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        if (listeningSlot >= 0) {
            if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
                hotkeyTmp.get(listeningSlot).setKeyCode(-1);
                hotkeyKeyBtns.get(listeningSlot).setMessage(Component.literal("[ --- ]"));
            } else {
                hotkeyTmp.get(listeningSlot).setKeyCode(keyCode);
                hotkeyKeyBtns.get(listeningSlot).setMessage(Component.literal("[ " + getKeyName(keyCode) + " ]"));
            }
            listeningSlot = -1;
            return true;
        }
        return super.keyPressed(event);
    }

    // ── Рендер ────────────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        backgroundAlpha = Math.min(1.0f, backgroundAlpha + delta * 2.0f);
        contentOffset   = Math.max(0.0f, contentOffset   - delta * 60.0f);

        renderGradientBackground(context);
        renderHeaderPanel(context);
        renderFooterPanel(context);

        context.pose().pushMatrix();
        context.pose().translate(0, contentOffset);

        switch (currentTab) {
            case SETTINGS -> renderSettingsLabels(context);
            case HOTKEYS  -> renderHotkeysLabels(context);
            case STATS    -> renderStatsTab(context);
        }

        super.render(context, mouseX, mouseY, delta);
        context.pose().popMatrix();

        renderActiveTabUnderline(context);
    }

    private void renderActiveTabUnderline(GuiGraphics context) {
        Button btn = switch (currentTab) {
            case SETTINGS -> tabSettings;
            case HOTKEYS  -> tabHotkeys;
            case STATS    -> tabStats;
        };
        if (btn == null) return;
        int alpha = (int)(backgroundAlpha * 240);
        int color = alpha << 24 | currentTheme.getRgbColor();
        context.fill(btn.getX(), btn.getY() + btn.getHeight() - 2,
                btn.getX() + btn.getWidth(), btn.getY() + btn.getHeight(), color);
    }

    private void renderSettingsLabels(GuiGraphics context) {
        int sp = Math.min(46, (footerY - contentY) / 6);
        int themeRgb = currentTheme.getRgbColor();

        String[] lLabels = { "⏱ Задержка /hub (мс)", "⏱ Задержка между кликами (мс)",
                "⌨ Команда для Classic", "⌨ Команда для Lite", "⌨ Команда для Lite 1.20", "⌨ Команда для Prime" };
        String[] lHints  = { "Напрямую при выключенном автоподборе", "Напрямую при выключенном автоподборе",
                "Например: cn", "Например: ln", "Например: ln120", "Например: pr" };
        for (int i = 0; i < lLabels.length; i++)
            renderLabel(context, lx, contentY + sp * i + 10 - 22, colW, lLabels[i], lHints[i], themeRgb);

        String[] rLabels = { "🔔 Уведомления", "🧠 Умный автоподбор", "🎨 Цветовая тема", "🔗 Цвет серверов" };
        String[] rHints  = { "Показывать уведомления о переходе", "Автоматически подбирает задержки",
                "Изменяет цвет элементов интерфейса", "Цвет кликабельных серверов в чате" };
        for (int i = 0; i < rLabels.length; i++)
            renderLabel(context, rx, contentY + sp * i + 10 - 22, colW, rLabels[i], rHints[i], themeRgb);
    }

    private void renderHotkeysLabels(GuiGraphics context) {
        int rowH   = Math.min(20, (footerY - contentY - 30) / 9);
        int cardH  = rowH + 2;
        int startY = contentY + 24;
        int themeRgb = currentTheme.getRgbColor();
        int alpha    = (int)(backgroundAlpha * 255);

        int totalW = panelW;
        int keyW  = (int)(totalW * 0.30);
        int modeW = (int)(totalW * 0.16);
        int numW  = (int)(totalW * 0.09);
        int togW  = (int)(totalW * 0.12);
        int gap   = (totalW - keyW - modeW - numW - togW) / 3;
        int kx = lx;
        int mx = kx + keyW + gap;
        int nx = mx + modeW + gap;
        int tx = nx + numW + gap;

        // Заголовки колонок
        int hY = contentY + 8;
        int headerAlpha = (int)(backgroundAlpha * 180);
        context.fill(lx - 2, hY - 3, lx + panelW + 2, hY + 13, headerAlpha << 24 | 0x1a1f3a);
        context.fill(lx - 2, hY - 3, lx, hY + 13, alpha << 24 | themeRgb);
        context.drawString(font, Component.literal("Клавиша").withStyle(currentTheme.getFormatting()), kx + 3, hY, themeRgb, false);
        context.drawString(font, Component.literal("Анархия").withStyle(currentTheme.getFormatting()),     mx, hY, themeRgb, false);
        context.drawString(font, Component.literal("№").withStyle(currentTheme.getFormatting()),       nx, hY, themeRgb, false);
        context.drawString(font, Component.literal("Статус").withStyle(currentTheme.getFormatting()),     tx, hY, themeRgb, false);

        for (int i = 0; i < 8; i++) {
            int y = startY + cardH * i;
            int cardAlpha = (int)(backgroundAlpha * 45);
            int cardBg = i % 2 == 0 ? 0x16213e : 0x111830;
            context.fill(lx - 2, y - 1, lx + panelW + 2, y + rowH, cardAlpha << 24 | cardBg);

            int lineAlpha = listeningSlot == i ? alpha : (int)(backgroundAlpha * 120);
            context.fill(lx - 2, y - 1, lx, y + rowH, lineAlpha << 24 | themeRgb);

            if (listeningSlot == i) {
                context.fill(lx, y - 1, lx + panelW + 2, y + rowH, (int)(backgroundAlpha * 35) << 24 | themeRgb);
            }

            context.drawString(font,
                    Component.literal(String.valueOf(i + 1)).withStyle(currentTheme.getFormatting()),
                    lx - 14, y + 4, themeRgb, false);
        }

        context.drawString(font,
                Component.literal("Нажмите кнопку → нажмите клавишу   |   ESC = очистить"),
                lx, startY + cardH * 8 + 2, 0x555566, false);
    }

    private void renderStatsTab(GuiGraphics context) {
        StatsData stats  = HubSwap.getStats();
        int themeRgb     = currentTheme.getRgbColor();
        int y            = contentY;

        renderSectionHeader(context, lx, y, panelW, "📊 Переходы", themeRgb);
        y += 16;
        context.drawString(font, Component.literal("Всего: " + stats.getTotalSwitches()), lx + 8, y, 0xFFFFFF, true);
        context.drawString(font, Component.literal("За сессию: " + stats.getSessionSwitches()), rx, y, 0xFFFFFF, true);
        y += 22;

        renderSectionHeader(context, lx, y, panelW, "🏆 Любимый сервер", themeRgb);
        y += 16;
        String fav = stats.getFavoriteKey();
        if (fav != null)
            context.drawString(font,
                    Component.literal(StatsData.formatKey(fav) + "  —  " + stats.getCountForKey(fav) + " раз").withStyle(currentTheme.getFormatting()),
                    lx + 8, y, themeRgb, true);
        else
            context.drawString(font, Component.literal("Пока нет данных"), lx + 8, y, 0x666666, false);
        y += 22;

        renderSectionHeader(context, lx, y, panelW, "📋 Топ серверов", themeRgb);
        y += 16;
        List<Map.Entry<String, Long>> sorted = stats.getServerCounts().entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5).toList();
        if (sorted.isEmpty()) {
            context.drawString(font, Component.literal("Пока нет данных"), lx + 8, y, 0x666666, false);
            y += 14;
        } else {
            for (int i = 0; i < sorted.size(); i++) {
                Map.Entry<String, Long> e = sorted.get(i);
                String medal = switch (i) { case 0 -> "🥇"; case 1 -> "🥈"; case 2 -> "🥉"; default -> (i+1)+"."; };
                context.drawString(font,
                        Component.literal(medal + " " + StatsData.formatKey(e.getKey()) + " — " + e.getValue() + " раз")
                                .withStyle(i == 0 ? currentTheme.getFormatting() : ChatFormatting.WHITE),
                        lx + 8, y + i * 14, i == 0 ? themeRgb : 0xCCCCCC, i == 0);
            }
            y += sorted.size() * 14 + 8;
        }

        renderSectionHeader(context, lx, y, panelW, "⏱ Время на серверах", themeRgb);
        y += 16;
        String[][] rows = { {"Classic","classic"}, {"Lite","light"}, {"Lite 1.20","light120"}, {"Prime","prime"} };
        // Максимум шкалы = 200 часов в миллисекундах
        long maxMs = 200L * 60 * 60 * 1000;
        int barX = lx + 140;
        int barW = this.width - margin - barX - 4;
        for (int i = 0; i < rows.length; i++) {
            int rowY = y + i * 24;
            long ms = stats.getTimeSpentMs(rows[i][1]);
            context.drawString(font, Component.literal(rows[i][0]), lx + 8, rowY + 4, 0xFFFFFF, false);
            context.drawString(font,
                    Component.literal(StatsData.formatTime(ms)).withStyle(currentTheme.getFormatting()),
                    lx + 68, rowY + 4, themeRgb, true);
            float ratio = (float) ms / maxMs;
            if (ratio > 1.0f) ratio = 1.0f;
            int filled = (int)(barW * ratio);
            int bgA = (int)(backgroundAlpha * 120);
            context.fill(barX, rowY + 2, barX + barW, rowY + 12, bgA << 24 | 0x1a1f3a);
            if (filled > 0)
                context.fill(barX, rowY + 2, barX + filled, rowY + 12, (int)(backgroundAlpha * 200) << 24 | themeRgb);
        }
    }

    private void renderSectionHeader(GuiGraphics context, int x, int y, int width, String title, int themeRgb) {
        int alpha = (int)(backgroundAlpha * 160);
        context.fill(x, y, x + width, y + 13, alpha << 24 | 0x16213e);
        context.fill(x, y, x + 3, y + 13, (int)(backgroundAlpha * 255) << 24 | themeRgb);
        context.drawString(font, Component.literal(title).withStyle(currentTheme.getFormatting()), x + 7, y + 3, themeRgb, false);
    }

    private void renderGradientBackground(GuiGraphics context) {
        context.fillGradient(0, 0, this.width, this.height,
                ((int)(backgroundAlpha * 200) << 24) | 0x0a0e27,
                ((int)(backgroundAlpha * 220) << 24) | 0x1a1f3a);
    }

    private void renderHeaderPanel(GuiGraphics context) {
        int panelH = 60;
        int alpha  = (int)(backgroundAlpha * 180);
        context.fillGradient(0, 0, this.width, panelH, alpha << 24 | 0x16213e, alpha << 24 | 0x0f1728);
        context.fill(0, panelH - 2, this.width, panelH, alpha << 24 | currentTheme.getRgbColor());
        context.drawCenteredString(font,
                Component.literal("HubSwap").withStyle(currentTheme.getFormatting()).withStyle(ChatFormatting.BOLD),
                this.width / 2, 17, 0xFFFFFF);
    }

    private void renderFooterPanel(GuiGraphics context) {
        int alpha = (int)(backgroundAlpha * 200);
        // Фиолетовая линия
        context.fill(0, footerY, this.width, footerY + 2, alpha << 24 | currentTheme.getRgbColor());
        // Чистый прозрачный фон под линией — кнопки будут хорошо видны
        context.fill(0, footerY + 2, this.width, this.height, (int)(backgroundAlpha * 160) << 24 | 0x0a0e27);
    }

    private void renderLabel(GuiGraphics context, int x, int y, int width, String label, String hint, int themeRgb) {
        int alpha = (int)(backgroundAlpha * 100);
        context.fill(x - 2, y, x + width + 2, y + 18, alpha << 24 | 0x1a1f3a);
        context.fill(x - 2, y, x, y + 18, (int)(backgroundAlpha * 255) << 24 | themeRgb);
        context.drawString(font, Component.literal(label).withStyle(currentTheme.getFormatting()), x + 3, y + 2, themeRgb, false);
        context.pose().pushMatrix();
        context.pose().translate(x + 3, y + 11);
        context.pose().scale(0.7f, 0.7f);
        context.drawString(font, hint, 0, 0, 0xCCCCCC, false);
        context.pose().popMatrix();
    }

    // ── Сохранение ────────────────────────────────────────────────────────

    private void onSave() {
        if (classicDelayField != null) {
            try {
                int cd = Integer.parseInt(classicDelayField.getValue());
                int ck = Integer.parseInt(clickDelayField.getValue());
                if (cd < 100 || cd > 5000) { sendError("Задержка /hub: от 100 до 5000 мс"); return; }
                if (ck < 50  || ck > 1000) { sendError("Задержка кликов: от 50 до 1000 мс"); return; }
                config.setDelays(cd, ck);
                config.setCommands(classicCommandField.getValue(), lightCommandField.getValue(), light120CommandField.getValue(), primeCommandField.getValue());
                config.setNotificationsEnabled(notificationsEnabledTmp);
                config.setSmartAutoTuneEnabled(smartAutoTuneEnabledTmp);
            } catch (NumberFormatException e) {
                sendError("Введите числовые значения для задержек");
                return;
            }
        }

        for (int i = 0; i < 8 && i < hotkeyNumFields.size(); i++) {
            try {
                int num = Integer.parseInt(hotkeyNumFields.get(i).getValue().trim());
                hotkeyTmp.get(i).setServerNumber(Math.max(1, num));
            } catch (NumberFormatException ignored) {}
        }

        List<HotkeySlot> dst = config.getHotkeySlots();
        for (int i = 0; i < 8; i++) {
            HotkeySlot s = hotkeyTmp.get(i), d = dst.get(i);
            d.setKeyCode(s.getKeyCode());
            d.setMode(s.getMode());
            d.setServerNumber(s.getServerNumber());
            d.setEnabled(s.isEnabled());
        }

        config.setColorTheme(currentTheme);
        config.setLinkColor(currentLinkColor);
        HubSwap.saveConfig();
        HubSwapClient.registerConfiguredCommands();

        if (client != null && client.player != null)
            client.player.displayClientMessage(
                    Component.literal("[HubSwap] ").withStyle(currentTheme.getFormatting())
                            .append(Component.literal("✓ Настройки сохранены!").withStyle(ChatFormatting.GREEN)), false);
        onClose();
    }

    private void sendError(String text) {
        if (client != null && client.player != null)
            client.player.displayClientMessage(
                    Component.literal("[HubSwap] ").withStyle(currentTheme.getFormatting())
                            .append(Component.literal("Ошибка: " + text).withStyle(ChatFormatting.RED)), false);
    }

    // ── Хелперы ───────────────────────────────────────────────────────────

    private String getModeShort(String mode) {
        return switch (mode) { case "classic" -> "Classic"; case "light120" -> "Lite 1.20"; case "prime" -> "Prime"; default -> "Lite"; };
    }

    private String nextMode(String mode) {
        return switch (mode) { case "classic" -> "light"; case "light" -> "light120"; case "light120" -> "prime"; default -> "classic"; };
    }

    private String getKeyName(int keyCode) {
        // Буквы A-Z: GLFW_KEY_A=65 ... GLFW_KEY_Z=90 — всегда английские независимо от раскладки
        if (keyCode >= GLFW.GLFW_KEY_A && keyCode <= GLFW.GLFW_KEY_Z) {
            return String.valueOf((char) keyCode);
        }
        // Цифры 0-9: GLFW_KEY_0=48 ... GLFW_KEY_9=57
        if (keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9) {
            return String.valueOf((char) keyCode);
        }
        return switch (keyCode) {
            case GLFW.GLFW_KEY_F1  -> "F1";  case GLFW.GLFW_KEY_F2  -> "F2";
            case GLFW.GLFW_KEY_F3  -> "F3";  case GLFW.GLFW_KEY_F4  -> "F4";
            case GLFW.GLFW_KEY_F5  -> "F5";  case GLFW.GLFW_KEY_F6  -> "F6";
            case GLFW.GLFW_KEY_F7  -> "F7";  case GLFW.GLFW_KEY_F8  -> "F8";
            case GLFW.GLFW_KEY_F9  -> "F9";  case GLFW.GLFW_KEY_F10 -> "F10";
            case GLFW.GLFW_KEY_F11 -> "F11"; case GLFW.GLFW_KEY_F12 -> "F12";
            case GLFW.GLFW_KEY_INSERT    -> "INS";  case GLFW.GLFW_KEY_DELETE    -> "DEL";
            case GLFW.GLFW_KEY_HOME      -> "HOME"; case GLFW.GLFW_KEY_END       -> "END";
            case GLFW.GLFW_KEY_PAGE_UP   -> "PGUP"; case GLFW.GLFW_KEY_PAGE_DOWN -> "PGDN";
            case GLFW.GLFW_KEY_UP        -> "↑";    case GLFW.GLFW_KEY_DOWN      -> "↓";
            case GLFW.GLFW_KEY_LEFT      -> "←";    case GLFW.GLFW_KEY_RIGHT     -> "→";
            case GLFW.GLFW_KEY_SPACE     -> "SPACE";
            case GLFW.GLFW_KEY_ENTER     -> "ENTER";
            case GLFW.GLFW_KEY_TAB       -> "TAB";
            case GLFW.GLFW_KEY_LEFT_SHIFT  -> "L-SHIFT"; case GLFW.GLFW_KEY_RIGHT_SHIFT -> "R-SHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "L-CTRL"; case GLFW.GLFW_KEY_RIGHT_CONTROL -> "R-CTRL";
            case GLFW.GLFW_KEY_LEFT_ALT    -> "L-ALT";   case GLFW.GLFW_KEY_RIGHT_ALT   -> "R-ALT";
            case GLFW.GLFW_KEY_MINUS       -> "-";        case GLFW.GLFW_KEY_EQUAL       -> "=";
            case GLFW.GLFW_KEY_LEFT_BRACKET -> "[";       case GLFW.GLFW_KEY_RIGHT_BRACKET -> "]";
            case GLFW.GLFW_KEY_SEMICOLON   -> ";";        case GLFW.GLFW_KEY_APOSTROPHE  -> "'";
            case GLFW.GLFW_KEY_COMMA       -> ",";        case GLFW.GLFW_KEY_PERIOD      -> ".";
            case GLFW.GLFW_KEY_SLASH       -> "/";        case GLFW.GLFW_KEY_BACKSLASH   -> "\\";
            case GLFW.GLFW_KEY_GRAVE_ACCENT -> "`";
            case GLFW.GLFW_KEY_KP_0 -> "NUM0"; case GLFW.GLFW_KEY_KP_1 -> "NUM1";
            case GLFW.GLFW_KEY_KP_2 -> "NUM2"; case GLFW.GLFW_KEY_KP_3 -> "NUM3";
            case GLFW.GLFW_KEY_KP_4 -> "NUM4"; case GLFW.GLFW_KEY_KP_5 -> "NUM5";
            case GLFW.GLFW_KEY_KP_6 -> "NUM6"; case GLFW.GLFW_KEY_KP_7 -> "NUM7";
            case GLFW.GLFW_KEY_KP_8 -> "NUM8"; case GLFW.GLFW_KEY_KP_9 -> "NUM9";
            default -> "Key" + keyCode;
        };
    }

    private Component getNotificationButtonText() {
        return notificationsEnabledTmp
                ? Component.literal("🔔 Уведомления: ВКЛ").withStyle(ChatFormatting.GREEN)
                : Component.literal("🔕 Уведомления: ВЫКЛ").withStyle(ChatFormatting.RED);
    }

    private Component getSmartAutoTuneButtonText() {
        return smartAutoTuneEnabledTmp
                ? Component.literal("🧠 Умный автоподбор: ВКЛ").withStyle(ChatFormatting.GREEN)
                : Component.literal("🧠 Умный автоподбор: ВЫКЛ").withStyle(ChatFormatting.RED);
    }

    private Component getThemeButtonText() {
        return Component.literal("🎨 Тема: " + currentTheme.getDisplayName()).withStyle(currentTheme.getFormatting());
    }

    private Component getLinkColorButtonText() {
        return Component.literal("🔗 Цвет ссылок: " + getLinkColorName(currentLinkColor)).withStyle(currentLinkColor);
    }

    private String getLinkColorName(ChatFormatting color) {
        if (color == ChatFormatting.GOLD)         return "Золотой";
        if (color == ChatFormatting.GREEN)        return "Зелёный";
        if (color == ChatFormatting.YELLOW)       return "Жёлтый";
        if (color == ChatFormatting.AQUA)         return "Синий";
        if (color == ChatFormatting.LIGHT_PURPLE) return "Фиолетовый";
        if (color == ChatFormatting.RED)          return "Красный";
        return "Золотой";
    }

    private ChatFormatting nextLinkColor(ChatFormatting current) {
        ChatFormatting[] colors = { ChatFormatting.GOLD, ChatFormatting.GREEN, ChatFormatting.YELLOW,
                ChatFormatting.AQUA, ChatFormatting.LIGHT_PURPLE, ChatFormatting.RED };
        for (int i = 0; i < colors.length; i++)
            if (colors[i] == current) return colors[(i + 1) % colors.length];
        return ChatFormatting.GOLD;
    }

    @Override
    public void onClose() {
        if (client != null) client.setScreen(parent);
    }
}