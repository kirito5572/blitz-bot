package me.kirito5572.objects;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;

public class EventPackage {
    final TextChannel textChannel;
    final Member member;
    final Message message;

    public EventPackage(TextChannel textChannel, Member member, Message message) {
        this.textChannel = textChannel;
        this.member = member;
        this.message = message;
    }

    public TextChannel getTextChannel() {
        return this.textChannel;
    }

    public Member getMember() {
        return this.member;
    }

    public Message getMessage() {
        return this.message;
    }

    public JDA getJDA() {
        return this.textChannel.getJDA();
    }

    public MessageChannel getChannel() {
        return this.textChannel;
    }

    public User getAuthor() {
        return this.member.getUser();
    }

    public Guild getGuild() {
        return this.textChannel.getGuild();
    }

    public MessageChannel getMessageChannel() {
        return this.textChannel;
    }
}
