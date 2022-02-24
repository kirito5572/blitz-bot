package BOT.Listener;

import BOT.Objects.SQLConnector;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import me.duncte123.botcommons.messaging.EmbedUtils;
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

        sqlConnector.Insert_Query("INSERT INTO blitz_bot.ChattingDataTable (messageId, userId, messageRaw, isFile) VALUES (?, ?, ?, ?)",
                new int[]{sqlConnector.STRING,sqlConnector.STRING, sqlConnector.STRING, sqlConnector.BOOLEAN},
                new String[] {message.getId(), message.getAuthor().getId(), message.getContentRaw(), String.valueOf(isFile)});
        if(isFile) {
            int i = 0;
            for (Message.Attachment attachment : files) {
                if (attachment.isImage()) {
                    i++;
                    File file = attachment.downloadToFile().join();
                    S3UploadObject(file, message.getId() + "_" + i);
                    boolean isFileDeleted = file.delete();
                    if(!isFileDeleted) {
                        logger.error("파일 삭제에 실패하였습니다.");
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
                String data = resultSet.getString("messageRaw");
                if(data != null) {
                    embedBuilder.addField("수정전 내용", data, false)
                            .addField("수정후 내용",event.getMessage().getContentRaw(), false)
                            .addField("메세지 ID", event.getMessageId(), false);
                }
            } else {
                embedBuilder.addField("수정전 내용", "정보 없음", false)
                        .addField("수정후 내용",event.getMessage().getContentRaw(), false);
            }
            embedBuilder.addField("수정 시간", timeFormat.format(time), false)
                    .setFooter(member.getNickname(), member.getUser().getAvatarUrl());
            Objects.requireNonNull(event.getGuild().getTextChannelById("829023428019355688")).sendMessageEmbeds(embedBuilder.build()).queue();

            sqlConnector.Insert_Query("UPDATE blitz_bot.ChattingDataTable SET messageRaw=? WHERE messageId=?",
                    new int[] {sqlConnector.STRING, sqlConnector.STRING},
                    new String[] {event.getMessage().getContentRaw(), event.getMessageId()});
        } catch (Exception e) {
            e.printStackTrace();
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
                if(resultSet.getString("messageRaw").length() < 1) {
                    embedBuilder.addField("삭제된 내용", "내용이 없이 사진만 있는 메세지", false);
                } else {
                    embedBuilder.addField("삭제된 내용", resultSet.getString("messageRaw"), false);
                }
                embedBuilder.addField("메세지 ID", event.getMessageId(), false).addField("삭제 시간", timeFormat.format(time), false);
            } else {
                embedBuilder.addField("데이터 없음", "데이터 없음", false)
                        .addField("삭제 시간", timeFormat.format(time), false);
            }
            Objects.requireNonNull(event.getGuild().getTextChannelById("829023428019355688")).sendMessageEmbeds(embedBuilder.build()).queue();
            if(isFile) {
                File file = S3DownloadObject(event.getMessageId() + "_" + 1);
                if (file != null) {
                    Objects.requireNonNull(event.getGuild().getTextChannelById("829023428019355688")).sendFile(file).queue();
                }
            }
            sqlConnector.Insert_Query("DELETE FROM blitz_bot.ChattingDataTable WHERE messageId=?", new int[] {sqlConnector.STRING}, new String[] {event.getMessageId()});

        } catch (SQLException e) {
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
        EmbedBuilder embedBuilder = EmbedUtils.getDefaultEmbed();
        Date date = new Date();
        Member member = event.getMember();
        List<Role> roleList = event.getRoles();
        StringBuilder roleData = new StringBuilder();
        for (Role role : roleList) {
            roleData.append(role.getAsMention());
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy/MM/dd a hh:mm:ss");
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
        EmbedBuilder embedBuilder = EmbedUtils.getDefaultEmbed();
        Date date = new Date();
        Member member = event.getMember();
        List<Role> roleList = event.getRoles();
        StringBuilder roleData = new StringBuilder();
        for (Role role : roleList) {
            roleData.append(role.getAsMention());
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy/MM/dd a hh:mm:ss");
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

    private void S3UploadObject(File file, String messageId) {
        Regions clientRegion = Regions.AP_NORTHEAST_2;
        String bucketName = "blitzbot-logger";
        try {
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(clientRegion)
                    .withCredentials(new EnvironmentVariableCredentialsProvider())
                    .build();

            PutObjectRequest request = new PutObjectRequest(bucketName, messageId, file);
            ObjectMetadata metadata = new ObjectMetadata();
            request.setMetadata(metadata);
            request.setStorageClass(StorageClass.StandardInfrequentAccess);
            s3Client.putObject(request);
        } catch (SdkClientException e) {
            e.printStackTrace();
        }
    }


    private File S3DownloadObject(String messageId) {
        Regions clientRegion = Regions.AP_NORTHEAST_2;
        String bucketName = "blitzbot-logger";

        try {
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(clientRegion)
                    .withCredentials(new EnvironmentVariableCredentialsProvider())
                    .build();

            GetObjectRequest request = new GetObjectRequest(bucketName, messageId);
            S3Object object = s3Client.getObject(request);
            ObjectMetadata metadata = object.getObjectMetadata();
            InputStream inputStream = object.getObjectContent();
            Path path = Files.createTempFile(messageId, "." + metadata.getContentType().split("/")[1]);
            try (FileOutputStream out = new FileOutputStream(path.toFile())) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
            } catch (Exception e) {
                // TODO: handle exception
                return null;
            }
            return path.toFile();

        } catch (SdkClientException | IOException e) {
            return null;
        }
    }
}
