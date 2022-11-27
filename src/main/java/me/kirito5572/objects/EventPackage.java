package me.kirito5572.objects;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NotNull;

public record EventPackage(TextChannel textChannel, Member member, Message message) {

    public @NotNull JDA getJDA() {
        return this.textChannel.getJDA();
    }

    public MessageChannel getChannel() {
        return this.textChannel;
    }

    public @NotNull User getAuthor() {
        return this.member.getUser();
    }

    public @NotNull Guild getGuild() {
        return this.textChannel.getGuild();
    }

    public MessageChannel getMessageChannel() {
        return this.textChannel;
    }
}
