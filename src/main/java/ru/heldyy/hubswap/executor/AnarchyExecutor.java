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

    private static final int[] PRIME_TARGET_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20};

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

                    runSequence(
                            TransitionMode.CLASSIC,
                            anarchyNumber,
                            delays,
                            16,
                            getClassicTargetSlot(anarchyNumber)
                    );
                    return;
                }

                if ("light".equals(mode)) {
                    if (!isValidLightNumber(anarchyNumber)) {
                        sendErrorMessage("Недопустимый номер анархии: " + anarchyNumber);
                        return;
                    }

                    int[] slots = getLightTargetSlots(anarchyNumber);
                    runSequence(
                            TransitionMode.LIGHT,
                            anarchyNumber,
                            delays,
                            13,
                            slots[0],
                            slots[1]
                    );
                    return;
                }

                if ("light120".equals(mode)) {
                    if (anarchyNumber < 1 || anarchyNumber > 3) {
                        sendErrorMessage("Недопустимый номер сервера: " + anarchyNumber);
                        return;
                    }

                    runSequence(
                            TransitionMode.LIGHT120,
                            anarchyNumber,
                            delays,
                            10,
                            getLite120TargetSlot(anarchyNumber)
                    );
                    return;
                }

                if ("prime".equals(mode)) {
                    if (anarchyNumber < 1 || anarchyNumber > PRIME_TARGET_SLOTS.length) {
                        sendErrorMessage("Недопустимый номер прайм анархии: " + anarchyNumber);
                        return;
                    }

                    runSequence(
                            TransitionMode.PRIME,
                            anarchyNumber,
                            delays,
                            14,
                            getPrimeTargetSlot(anarchyNumber)
                    );
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

    private static void runSequence(
            TransitionMode mode,
            int targetNumber,
            AutoTuneManager.Delays delays,
            int modeSlot,
            int... targetSlots
    ) throws InterruptedException {
        TransitionDetector.startAttempt(
                mode,
                targetNumber,
                delays.hubDelay(),
                delays.clickDelay(),
                delays.confirmDelay()
        );

        sendCommand("hub");
        sleep(delays.hubDelay());

        sendCommand("menu");
        sleep(delays.clickDelay());

        clickSlot(modeSlot);
        sleep(delays.clickDelay() + 60L);

        for (int i = 0; i < targetSlots.length; i++) {
            clickSlot(targetSlots[i]);
            if (i < targetSlots.length - 1) {
                sleep(delays.clickDelay() + 60L);
            }
        }
    }

    private static boolean isValidLightNumber(int number) {
        if (number == 57) {
            return false;
        }
        return (number >= 1 && number <= 17)
                || (number >= 18 && number <= 38)
                || (number >= 39 && number <= 56)
                || (number >= 58 && number <= 74);
    }

    private static int getClassicTargetSlot(int number) {
        return 19 + number;
    }

    private static int[] getLightTargetSlots(int number) {
        if (number >= 1 && number <= 17) {
            return new int[]{0, getSoloTargetSlot(number)};
        }
        if (number >= 18 && number <= 38) {
            return new int[]{1, getDuoTargetSlot(number)};
        }
        if (number >= 39 && number <= 56) {
            return new int[]{2, getTrioTargetSlot(number)};
        }
        return new int[]{3, getClanTargetSlot(number)};
    }

    private static int getSoloTargetSlot(int number) {
        return 10 + (number - 1) * 3 / 2;
    }

    private static int getDuoTargetSlot(int number) {
        return 10 + (number - 18) * 7 / 5;
    }

    private static int getTrioTargetSlot(int number) {
        return 10 + (number - 39) * 25 / 17;
    }

    private static int getClanTargetSlot(int number) {
        return 10 + (number - 58) * 3 / 2;
    }

    private static int getLite120TargetSlot(int number) {
        return 10 + number;
    }

    private static int getPrimeTargetSlot(int number) {
        return PRIME_TARGET_SLOTS[number - 1];
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
