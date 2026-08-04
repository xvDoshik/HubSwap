package ru.heldyy.hubswap.mixin;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ru.heldyy.hubswap.HubSwap;
import ru.heldyy.hubswap.gui.TransitionDetector;
import ru.heldyy.hubswap.linkify.ServerLinkifier;

@Mixin(ChatComponent.class)
public class ChatHudMixin {

    @ModifyVariable(
            method = "addMessage(Lnet/minecraft/network/chat/Component;)V",
            at = @At("HEAD"),
            argsOnly = true,
            require = 0
    )
    private Component hubswap$processMessage1(Component message) {
        if (message != null) {
            TransitionDetector.onChatMessage(message.getString());
        }
        return ServerLinkifier.linkify(message, HubSwap.getConfig());
    }

    @ModifyVariable(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
            at = @At("HEAD"),
            argsOnly = true,
            require = 0
    )
    private Component hubswap$processMessage2(Component message) {
        if (message != null) {
            TransitionDetector.onChatMessage(message.getString());
        }
        return ServerLinkifier.linkify(message, HubSwap.getConfig());
    }
}
