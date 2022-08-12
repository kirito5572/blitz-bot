package me.kirito5572.listener;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.kirito5572.objects.SQLConnector;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;


public class LogListener extends ListenerAdapter {
    private final static Logger logger = LoggerFactory.getLogger(LogListener.class);
    private final SQLConnector sqlConnector;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy년 MM월dd일 HH시mm분ss초");
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy/MM/dd a hh:mm:ss");

    public LogListener(SQLConnector sqlConnector) {
        this.sqlConnector = sqlConnector;
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        boolean isFile = false;

        Message message = event.getMessage();
        if(event.getAuthor().isBot()) {
            return;
        }
        if(message.isWebhookMessage()) {
            return;
        }
        List<Message.Attachment> files = message.getAttachments();
        if(!files.isEmpty()) {
            isFile = true;
        }

        try {
            sqlConnector.Insert_Query("INSERT INTO blitz_bot.ChattingDataTable (messageId, userId, messageRaw, isFile) VALUES (?, ?, ?, ?)",
                    new int[]{sqlConnector.STRING,sqlConnector.STRING, sqlConnector.STRING, sqlConnector.BOOLEAN},
                    new String[] {message.getId(), message.getAuthor().getId(), message.getContentRaw(), String.valueOf(isFile)});
        } catch (SQLException sqlException) {
            logger.error(sqlException.getMessage());
        }
        if(isFile) {
            int i = 0;
            for (Message.Attachment attachment : files) {
                if (attachment.isImage()) {
                    i++;
                    File file = attachment.downloadToFile().join();
                    try {
                        S3UploadObject(file, message.getId() + "_" + i);
                    } catch (SdkClientException e) {
                        logger.error(e.getMessage());
                        e.printStackTrace();
                    }
                    boolean isFileDeleted = file.delete();
                    if(!isFileDeleted) {
                        logger.warn("파일 삭제에 실패하였습니다. 재시도 중입니다.");
                        isFileDeleted = file.delete();
                        if(!isFileDeleted) {
                            logger.error("파일 삭제에 실패하였습니다.");
                        } else {
                            logger.info("파일 삭제에 성공했습니다.");
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onGuildMessageUpdate(@NotNull GuildMessageUpdateEvent event) {
        if(event.getAuthor().isBot()) {
            return;
        }
        if(event.getMessage().isWebhookMessage()) {
            return;
        }
        EmbedBuilder embedBuilder = EmbedUtils.getDefaultEmbed();
        Date time = new Date();
        try (ResultSet resultSet = sqlConnector.Select_Query("SELECT * FROM blitz_bot.ChattingDataTable WHERE messageId=?",
                new int[] {sqlConnector.STRING},
                new String[] {event.getMessageId()})){
            Member member = event.getMember();
            assert member != null;
            embedBuilder.setTitle("수정된 메세지")
                    .setColor(Color.ORANGE)
                    .setDescription("[메세지 이동](" + event.getMessage().getJumpUrl() + ")")
                    .addField("작성 채널", event.getChannel().getAsMention(), false);
            if(resultSet.next()) {
                String pastData = resultSet.getString("messageRaw");
                String nowData = event.getMessage().getContentRaw();
                if(pastData != null) {
                    MessageBuilder(embedBuilder, pastData,"수정전 내용", null);
                    MessageBuilder(embedBuilder, nowData,"수정후 내용", event.getMessageId());
                }
            } else {
                embedBuilder.addField("수정전 내용", "정보 없음", false);
                MessageBuilder(embedBuilder, event.getMessage().getContentRaw(),"수정후 내용", null);
            }
            embedBuilder.addField("수정 시간", timeFormat.format(time), false)
                    .setFooter(member.getNickname(), member.getUser().getAvatarUrl());
            Objects.requireNonNull(event.getGuild().getTextChannelById("829023428019355688")).sendMessageEmbeds(embedBuilder.build()).queue();

            sqlConnector.Insert_Query("UPDATE blitz_bot.ChattingDataTable SET messageRaw=? WHERE messageId=?",
                    new int[] {sqlConnector.STRING, sqlConnector.STRING},
                    new String[] {event.getMessage().getContentRaw(), event.getMessageId()});
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void onGuildMessageDelete(@NotNull GuildMessageDeleteEvent event) {
        EmbedBuilder embedBuilder = EmbedUtils.getDefaultEmbed();
        Date time = new Date();
        try (ResultSet resultSet = sqlConnector.Select_Query("SELECT * FROM blitz_bot.ChattingDataTable WHERE messageId=?;",
                new int[] {sqlConnector.STRING}, new String[] {event.getMessageId()})) {
            embedBuilder.setTitle("삭제된 메세지")
                    .setColor(Color.RED);
            boolean isFile = false;
            if(resultSet.next()) {
                isFile = resultSet.getBoolean("isFile");
                Member member = event.getGuild().getMemberById(resultSet.getString("userId"));
                if (member == null) {
                    embedBuilder.setFooter("<@" + resultSet.getString("userId") + ">");
                } else {
                    embedBuilder.setFooter(member.getNickname(), member.getUser().getAvatarUrl());
                }
                if(isFile) {
                    embedBuilder.appendDescription("이미지가 포함된 게시글");
                }
                embedBuilder.addField("작성 채널", event.getChannel().getAsMention(), false);
                String messageRaw = resultSet.getString("messageRaw");
                if(resultSet.getString("messageRaw").length() < 1) {
                    embedBuilder.addField("삭제된 내용", "내용이 없이 사진만 있는 메세지", false);
                } else {
                    MessageBuilder(embedBuilder, messageRaw,"삭제된 내용", event.getMessageId());
                }
            } else {
                embedBuilder.addField("데이터 없음", "데이터 없음", false);
            }
            embedBuilder.addField("삭제 시간", timeFormat.format(time), false);
            Objects.requireNonNull(event.getGuild().getTextChannelById("829023428019355688")).sendMessageEmbeds(embedBuilder.build()).queue();
            if(isFile) {
                try {
                    File file = S3DownloadObject(event.getMessageId() + "_" + 1);
                    Objects.requireNonNull(event.getGuild().getTextChannelById("829023428019355688")).sendFile(file).queue();
                } catch (IOException e) {
                    logger.error(e.getMessage());
                    e.printStackTrace();
                }
            }
            sqlConnector.Insert_Query("DELETE FROM blitz_bot.ChattingDataTable WHERE messageId=?", new int[] {sqlConnector.STRING}, new String[] {event.getMessageId()});

        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        EmbedBuilder embedBuilder = EmbedUtils.getDefaultEmbed();
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy/MM/dd a hh:mm:ss");
        embedBuilder.setTitle("신규 유저 접속")
                .setColor(new Color(50, 200, 50))
                .setDescription(simpleDateFormat.format(date))
                .addField("유저명", event.getMember().getEffectiveName(), false)
                .setFooter(event.getMember().getId(), event.getMember().getAvatarUrl());
        Objects.requireNonNull(event.getGuild().getTextChannelById("946362857795248188")).sendMessageEmbeds(embedBuilder.build()).queue();
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        EmbedBuilder embedBuilder = EmbedUtils.getDefaultEmbed();
        Date date = new Date();
        Member member = event.getMember();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy/MM/dd a hh:mm:ss");
        embedBuilder.setTitle("유저 서버 나감")
                .setColor(new Color(200, 50, 50))
                .setDescription(simpleDateFormat.format(date));
        if(member != null) {
            embedBuilder.addField("유저명", member.getEffectiveName(), false)
                    .setFooter(member.getId(), member.getAvatarUrl());
        } else {
            embedBuilder.addField("유저명", "데이터 알 수 없음", false)
                    .setFooter(event.getUser().getId());
        }
        Objects.requireNonNull(event.getGuild().getTextChannelById("946362857795248188")).sendMessageEmbeds(embedBuilder.build()).queue();
    }

    @Override
    public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {
        Date date = new Date();
        Member member = event.getMember();
        List<Role> roleList = event.getRoles();
        StringBuilder roleData = new StringBuilder();
        for (Role role : roleList) {
            roleData.append(role.getAsMention());
        }
        EmbedBuilder embedBuilder = EmbedUtils.getDefaultEmbed();
        embedBuilder.setTitle("유저 역할 부여")
                .setColor(new Color(255, 200, 0))
                .setDescription(simpleDateFormat.format(date))
                .addField("유저명", member.getAsMention(), false)
                .addField("부여된 역할", roleData.toString(), false)
                .setFooter(member.getId(), member.getAvatarUrl());
        Objects.requireNonNull(event.getGuild().getTextChannelById("946362857795248188")).sendMessageEmbeds(embedBuilder.build()).queue();
    }

    @Override
    public void onGuildMemberRoleRemove(@NotNull GuildMemberRoleRemoveEvent event) {
        Date date = new Date();
        Member member = event.getMember();
        List<Role> roleList = event.getRoles();
        StringBuilder roleData = new StringBuilder();
        for (Role role : roleList) {
            roleData.append(role.getAsMention());
        }
        EmbedBuilder embedBuilder = EmbedUtils.getDefaultEmbed();
        embedBuilder.setTitle("유저 역할 삭제")
                .setColor(new Color(102,20,153))
                .setDescription(simpleDateFormat.format(date))
                .addField("유저명", member.getAsMention(), false)
                .addField("삭제된 역할", roleData.toString(), false)
                .setFooter(member.getId(), member.getAvatarUrl());
        Objects.requireNonNull(event.getGuild().getTextChannelById("946362857795248188")).sendMessageEmbeds(embedBuilder.build()).queue();
    }

    @Override
    public void onGuildBan(@NotNull GuildBanEvent event) {
        super.onGuildBan(event);
    }

    /**
     * upload file to bot s3 cloud
     *
     * @param file the {@link java.io.File} to upload
     * @param messageId message id of the file to be uploaded
     */

    private void S3UploadObject(@NotNull File file,@NotNull String messageId) throws SdkClientException{
        Regions clientRegion = Regions.AP_NORTHEAST_2;
        String bucketName = "blitzbot-logger";

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(clientRegion)
                .withCredentials(new EnvironmentVariableCredentialsProvider())
                .build();

        PutObjectRequest request = new PutObjectRequest(bucketName, messageId, file);
        ObjectMetadata metadata = new ObjectMetadata();
        request.setMetadata(metadata);
        request.setStorageClass(StorageClass.StandardInfrequentAccess);
        s3Client.putObject(request);
    }

    /**
     * download {@link java.io.File} to bot s3 cloud
     *
     * @param messageId Message id of the file to be downloaded
     *
     * @return download {@link java.io.File} or null(If the file does not exist)
     */

    private File S3DownloadObject(@NotNull String messageId) throws SdkClientException, IOException{
        Regions clientRegion = Regions.AP_NORTHEAST_2;
        String bucketName = "blitzbot-logger";

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(clientRegion)
                .withCredentials(new EnvironmentVariableCredentialsProvider())
                .build();

        GetObjectRequest request = new GetObjectRequest(bucketName, messageId);
        S3Object object = s3Client.getObject(request);
        ObjectMetadata metadata = object.getObjectMetadata();
        InputStream inputStream = object.getObjectContent();
        Path path = Files.createTempFile(messageId, "." + metadata.getContentType().split("/")[1]);
        FileOutputStream out = new FileOutputStream(path.toFile());
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
        out.close();
        return path.toFile();
    }

    /**
     * Message data splitter(if Message data's length > 1000, split it)
     *
     * @param embedBuilder the EmbedBuilder which is use addField with split data
     * @param data the message string to be split
     * @param messageId the messageId which is modify message's id
     *                  input null if the message which is before modification is null
     * @param name the name
     */

    private void MessageBuilder(@NotNull EmbedBuilder embedBuilder, @NotNull String data, @NotNull String name, @Nullable String messageId) {
        if(1000 < data.length() && data.length() <= 2000) {
            embedBuilder.addField("정보", "1000글자를 넘어가는 메세지로 확인되어 여러단락으로 분리했습니다.", false)
                    .addField(name + "-1", data.substring(0, 1000), false)
                    .addField(name + "-2", data.substring(1000), false);
        } else if(2000 < data.length() && data.length() <= 3000) {
            embedBuilder.addField("정보", "1000글자를 넘어가는 메세지로 확인되어 여러단락으로 분리했습니다.", false)
                    .addField(name + "-1", data.substring(0, 1000), false)
                    .addField(name + "-2", data.substring(1000, 2000), false)
                    .addField(name + "-3", data.substring(2000), false);
        } else if(3000 < data.length() && data.length() <= 4000) {
            embedBuilder.addField("정보", "1000글자를 넘어가는 메세지로 확인되어 여러단락으로 분리했습니다.", false)
                    .addField(name + "-1", data.substring(0, 1000), false)
                    .addField(name + "-2", data.substring(1000, 2000), false)
                    .addField(name + "-3", data.substring(2000, 3000), false)
                    .addField(name + "-4", data.substring(3000), false);
        } else {
            embedBuilder.addField(name, data, false);
        }

        if(messageId != null) {
            embedBuilder.addField("메세지 ID", messageId, false);
        }

    }
}
