package me.kirito5572.commands.admin;

import groovy.lang.GroovyShell;
import me.kirito5572.objects.EventPackage;
import me.kirito5572.objects.ICommand;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class EvalCommand implements ICommand {
    private final @NotNull GroovyShell engine;
    private final @NotNull String imports;

    public EvalCommand() {
        this.engine = new GroovyShell();
        this.imports = """
                import java.io.*
                import java.lang.*
                import java.util.*
                import java.util.Arrays.*
                import java.util.concurrent.*
                import java.sql.*
                import net.dv8tion.jda.api.*
                import net.dv8tion.jda.api.entities.*
                import net.dv8tion.jda.api.managers.*
                import net.dv8tion.jda.api.utils.*
                import me.duncte123.botcommons.messaging.*
                import net.dv8tion.jda.api.events.message.guild.*
                import net.dv8tion.jda.api.exceptions.*
                import net.dv8tion.jda.api.audio.*
                import net.dv8tion.jda.api.events.*
                import net.dv8tion.jda.api.events.channel.category.update.*
                import net.dv8tion.jda.api.events.channel.category.*
                import net.dv8tion.jda.api.events.channel.priv.*
                import net.dv8tion.jda.api.events.channel.store.update.*
                import net.dv8tion.jda.api.events.channel.store.*
                import net.dv8tion.jda.api.events.channel.text.update.*
                import net.dv8tion.jda.api.events.channel.text.*
                import net.dv8tion.jda.api.events.channel.voice.update.*
                import net.dv8tion.jda.api.events.channel.voice.*
                import net.dv8tion.jda.api.events.emote.update.*
                import net.dv8tion.jda.api.events.emote.*
                import net.dv8tion.jda.api.events.guild.update.*
                import net.dv8tion.jda.api.events.guild.voice.*
                import net.dv8tion.jda.api.events.guild.member.*
                import net.dv8tion.jda.api.events.guild.invite.*
                import net.dv8tion.jda.api.events.guild.override.*
                import net.dv8tion.jda.api.events.guild.*
                import net.dv8tion.jda.api.events.http.*
                import net.dv8tion.jda.api.events.message.guild.*
                import net.dv8tion.jda.api.events.message.guild.react.*
                import net.dv8tion.jda.api.events.message.react.*
                import net.dv8tion.jda.api.events.message.priv.react.*
                import net.dv8tion.jda.api.events.message.priv.*
                import net.dv8tion.jda.api.events.message.*
                import net.dv8tion.jda.api.events.role.*
                import net.dv8tion.jda.api.events.role.update.*
                import net.dv8tion.jda.api.events.self.*
                import net.dv8tion.jda.api.events.role.*
                import net.dv8tion.jda.api.events.role.update.*
                import net.dv8tion.jda.api.events.*
                import net.dv8tion.jda.api.managers.*
                import me.duncte123.botcommons.*
                import me.duncte123.botcommons.text.*
                import me.duncte123.botcommons.commands.*
                import me.duncte123.botcommons.config.*
                import me.duncte123.botcommons.messaging.*
                import me.duncte123.botcommons.web.*
                import bot.listener.*
                import bot.commands.*
                import bot.commands.admin.*
                import bot.commands.music.*;
                import bot.objects.*
                import bot.*
                import com.google.gson.*
                """;
    }
    @Override
    public void handle(@NotNull List<String> args, @NotNull EventPackage event) {
        if(!Objects.requireNonNull(event.member()).getId().equals("284508374924787713")) {
            return;
        }
        if (args.isEmpty()) {
            event.getChannel().sendMessage("Missing arguments").queue();

            return;
        }
        if(args.get(0).contains("jda.shutdown()")) {
            event.getChannel().sendMessage("봇이 종료됩니다.").queue();
            System.exit(0);
        }
        try {
            engine.setProperty("args", args);
            engine.setProperty("event", event);
            engine.setProperty("channel", event.getChannel());
            engine.setProperty("message", event.message());
            engine.setProperty("jda", event.getJDA());
            engine.setProperty("guild", event.getGuild());
            engine.setProperty("member", event.member());

            String script = imports + event.message().getContentRaw().split("\\s+", 2)[1];
            Object out = engine.evaluate(script);

            event.getChannel().sendMessage(out == null ? "에러 없이 실행이 완료되었습니다." : out.toString()).queue();
        } catch (Exception e) {
            event.getChannel().sendMessage(e.getMessage()).queue();
        }

    }

    @Override
    public @NotNull String getHelp() {
        return "null";
    }

    @Override
    public String @NotNull [] getInvoke() {
        return new String[] {"eval"};
    }

    @Override
    public @NotNull String getSmallHelp() {
        return "(개발자 전용) 살충제! 살충제!";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }

    @Override
    public boolean isOwnerOnly() {
        return true;
    }

    @Override
    public boolean isMusicOnly() {
        return false;
    }
}
