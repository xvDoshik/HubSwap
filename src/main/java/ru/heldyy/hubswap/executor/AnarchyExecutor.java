package ru.heldyy.hubswap.executor;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickType;
import ru.heldyy.hubswap.HubSwap;
import ru.heldyy.hubswap.config.ModConfig;
import ru.heldyy.hubswap.gui.AutoTuneManager;
import ru.heldyy.hubswap.gui.TransitionDetector;
import ru.heldyy.hubswap.gui.TransitionMode;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AnarchyExecutor {
    private static final Minecraft client = Minecraft.getInstance();
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void executeSequence(String mode, int anarchyNumber) {
        ModConfig config = HubSwap.getConfig();
        AutoTuneManager.Delays delays = AutoTuneManager.getLiveDelays(config);

        if (client.player == null || client.gameMode == null) {
            sendErrorMessage("Игрок или взаимодействие недоступны");
            return;
        }

        HubSwap.getStats().recordSwitch(mode, anarchyNumber);
        HubSwap.saveStats();

        executor.execute(() -> {
            try {
                if ("classic".equals(mode)) {
                    if (anarchyNumber < 1 || anarchyNumber > 5) {
                        sendErrorMessage("Недопустимый номер анархии: " + anarchyNumber);
                        return;
                    }

                    TransitionDetector.startAttempt(
                            TransitionMode.CLASSIC,
                            anarchyNumber,
                            delays.hubDelay(),
                            delays.clickDelay(),
                            delays.confirmDelay()
                    );

                    sendCommand("hub");
                    sleep(delays.hubDelay());

                    sendCommand("menu");
                    sleep(delays.clickDelay());

                    clickSlot(15);
                    sleep(delays.clickDelay() + 60L);

                    clickSlot(getClassicTargetSlot(anarchyNumber));
                    return;
                }

                if ("light".equals(mode)) {
                    if (anarchyNumber < 1 || anarchyNumber > 70) {
                        sendErrorMessage("Недопустимый номер анархии: " + anarchyNumber);
                        return;
                    }

                    TransitionDetector.startAttempt(
                            TransitionMode.LIGHT,
                            anarchyNumber,
                            delays.hubDelay(),
                            delays.clickDelay(),
                            delays.confirmDelay()
                    );

                    sendCommand("hub");
                    sleep(delays.hubDelay());

                    sendCommand("menu");
                    sleep(delays.clickDelay());

                    clickSlot(12);
                    sleep(delays.clickDelay() + 60L);

                    int[] slots = getLightTargetSlots(anarchyNumber);

                    clickSlot(slots[0]);
                    sleep(delays.clickDelay() + 60L);

                    clickSlot(slots[1]);
                    return;
                }

                if ("light120".equals(mode)) {
                    if (anarchyNumber < 1 || anarchyNumber > 3) {
                        sendErrorMessage("Недопустимый номер сервера: " + anarchyNumber);
                        return;
                    }

                    TransitionDetector.startAttempt(
                            TransitionMode.LIGHT120,
                            anarchyNumber,
                            delays.hubDelay(),
                            delays.clickDelay(),
                            delays.confirmDelay()
                    );

                    sendCommand("hub");
                    sleep(delays.hubDelay());

                    sendCommand("menu");
                    sleep(delays.clickDelay());

                    clickSlot(10);
                    sleep(delays.clickDelay() + 60L);

                    clickSlot(getLite120TargetSlot(anarchyNumber));
                    return;
                }

                sendErrorMessage("Неизвестный режим: " + mode);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                sendErrorMessage("Ошибка выполнения последовательности");
            } catch (Exception e) {
                sendErrorMessage("Сбой автоперехода: " + e.getClass().getSimpleName());
            }
        });
    }

    private static int getClassicTargetSlot(int number) {
        int[] slots = new int[]{20, 21, 22, 23, 24};

        if (number < 1 || number > slots.length) {
            return slots[0];
        }

        return slots[number - 1];
    }

    private static int[] getLightTargetSlots(int number) {
        int pageSlot;
        int offset;

        if (number <= 16) {
            pageSlot = 0;
            offset = number - 1;
        } else if (number <= 37) {
            pageSlot = 1;
            offset = number - 17;
        } else if (number <= 53) {
            pageSlot = 2;
            offset = number - 38;
        } else {
            pageSlot = 3;
            offset = number - 54;
        }

        int targetSlot = 18 + offset;

        return new int[]{pageSlot, targetSlot};
    }

    private static int getLite120TargetSlot(int number) {
        int[] slots = new int[]{0, 11, 12, 13};

        if (number < 1 || number >= slots.length) {
            return slots[1];
        }

        return slots[number];
    }

    private static void sendCommand(String command) {
        client.execute(() -> {
            if (client.player == null || client.getConnection() == null) {
                return;
            }

            String cmd = command == null ? "" : command.trim();

            if (cmd.startsWith("/")) {
                cmd = cmd.substring(1);
            }

            if (cmd.isEmpty()) {
                return;
            }

            client.getConnection().sendCommand(cmd);
        });
    }

    private static void clickSlot(int slot) {
        client.execute(() -> {
            if (client.gameMode != null
                    && client.player != null
                    && client.player.containerMenu != null) {

                client.gameMode.handleInventoryMouseClick(
                        client.player.containerMenu.containerId,
                        slot,
                        0,
                        ClickType.PICKUP,
                        client.player
                );
            } else {
                sendErrorMessage("Меню не открыто или синхронизация нарушена");
            }
        });
    }

    private static void sendErrorMessage(String message) {
        client.execute(() -> {
            if (client.player != null) {
                client.player.displayClientMessage(Component.literal("[HubSwap] Ошибка: " + message), false);
            }
        });
    }

    private static void sleep(long ms) throws InterruptedException {
        Thread.sleep(ms);
    }

    public static void shutdown() {
        executor.shutdown();
    }
}
