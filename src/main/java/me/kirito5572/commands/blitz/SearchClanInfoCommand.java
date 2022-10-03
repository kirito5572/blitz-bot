package me.kirito5572.commands.blitz;

import me.duncte123.botcommons.messaging.EmbedUtils;
import me.kirito5572.objects.EventPackage;
import me.kirito5572.objects.ICommand;
import me.kirito5572.objects.WargamingAPI;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class SearchClanInfoCommand implements ICommand {
    private final WargamingAPI wargamingAPI;

    public SearchClanInfoCommand(WargamingAPI wargamingAPI) {
        this.wargamingAPI = wargamingAPI;
    }

    @Override
    public void handle(List<String> args, @NotNull EventPackage event) {
        if(args.isEmpty()) {
            event.getChannel().sendMessage("검색할 클랜명을 입력해주세요").queue(
                    message -> message.delete().queueAfter(10, TimeUnit.SECONDS)
            );
            return;
        }
        WargamingAPI.ClanSearchData[] data = wargamingAPI.getClanSearchData(args.get(0));
        if(data == null) {
            event.getChannel().sendMessage("관련한 클랜에 대한 검색 결과가 없습니다.")
                    .queue(message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
            return;
        }
        EmbedBuilder builder = EmbedUtils.getDefaultEmbed()
                .setTitle(String.format("%s 에 대한 검색 결과", args.get(0)));
        for(int i = 0; i < 5; i++) {
            WargamingAPI.ClanSearchData searchData = data[i];
            if(searchData != null) {
                builder.addField(String.format("%d. [%s]%s", i, searchData.tag, searchData.name),
                        String.format("유저 수: %d", searchData.members_count), false);
            }
        }
        AtomicInteger selectInt = new AtomicInteger();
        event.getChannel().sendMessageEmbeds(builder.build()).queue(message -> {
            final int[] i = {0};
            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    i[0]++;
                    if(i[0] == 20) {
                        message.delete().queue();
                        message.getChannel().sendMessage("대기시간을 초과하여 동작이 취소되었습니다")
                                .queue(message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
                        timer.cancel();
                        return;
                    }
                    message.getChannel().retrieveMessageById(message.getChannel().getLatestMessageId()).queue(message1 -> {
                        boolean pass;
                        try {
                            selectInt.set((Integer.parseInt(message1.getContentRaw()) - 1));
                            pass = true;
                        } catch (NumberFormatException e) {
                            pass = false;
                        }
                        if (pass) {
                            ClanInfoBuilder(message, data[selectInt.get()].clan_id);
                            timer.cancel();
                        }
                    });
                }
            };
            timer.scheduleAtFixedRate(timerTask, 0, 500);
        });
    }

    private void ClanInfoBuilder(Message message, int clanId) {
        WargamingAPI.ClanData clanData = wargamingAPI.getClanData(clanId);
        if(clanData == null) {
            message.getChannel().sendMessage("해당 클랜은 해체되어 정보를 조회할 수 없습니다.")
                    .queue(message1 -> message1.delete().queueAfter(10, TimeUnit.SECONDS));
            return;
        }
        EmbedBuilder builder = EmbedUtils.getDefaultEmbed()
                .setTitle(String.format("[%s] %s", clanData.tag, clanData.name));
        String policy;
        SimpleDateFormat format = new SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분 ss초");
        if(clanData.recruiting_policy.equals("restricted")) {
            policy = "가입 제한";
        } else {
            policy = "공개";
        }
        builder.addField("클랜 사령관", String.format("%s", clanData.leader_name), true)
                .addField("유저수", String.format("%d", clanData.members_count), true)
                .addField("가입 제한", policy, true)
                .addField("클랜 생성일", format.format(new Date(clanData.created_at * 1000)), false)
                .addField("클랜 모토", String.format("%s", clanData.motto), false)
                .addField("클랜 설명",String.format("%s", clanData.description),false);
        message.getChannel().sendMessageEmbeds(builder.build()).queue();
        if(policy.equals("가입 제한")) {
            builder = EmbedUtils.getDefaultEmbed()
                    .setTitle("클랜 가입 제한 조건")
                    .addField("전투수", String.format("%d", clanData.recruiting_options.battles), true)
                    .addField("승률", String.format("%d", clanData.recruiting_options.wins_ratio), true)
                    .addField("평균 데미지", String.format("%d", clanData.recruiting_options.average_damage), true)
                    .addField("평균 티어", String.format("%d", clanData.recruiting_options.vehicles_level), true)
                    .addField("평균 일일 전투 횟수", String.format("%d", clanData.recruiting_options.average_battles_per_day), true);
            message.getChannel().sendMessageEmbeds(builder.build()).queue();
        }
        Comparator<String> comparator = Comparator.naturalOrder();
        Map<String, String> memberMap = new TreeMap<>(comparator);
        for(WargamingAPI.ClanMembers members : clanData.members.values()) {
            memberMap.put(members.role, members.account_name);
        }
        AtomicReference<String> Commander = new AtomicReference<>();
        Commander.set("");
        List<String> executive_officer_list = new ArrayList<>();
        List<String> private_list = new ArrayList<>();
        memberMap.forEach((role, name) -> {
            switch (role) {
                case "executive_officer" -> executive_officer_list.add(name);
                case "private" -> private_list.add(name);
                case "commander" -> Commander.set(name);
            }
        });
        executive_officer_list.sort(Comparator.naturalOrder());
        private_list.sort(Comparator.naturalOrder());
        StringBuilder stringBuilder = new StringBuilder();
        for(String officer : executive_officer_list) {
            stringBuilder.append(officer).append("\n");
        }
        String officer = stringBuilder.toString();
        stringBuilder = new StringBuilder();
        for(String privates : private_list) {
            stringBuilder.append(privates).append("\n");
        }
        String privates = stringBuilder.toString();

        builder = EmbedUtils.getDefaultEmbed()
                .addField("클랜 사령관", Commander.get(), false)
                .addField("클랜 부사령관",officer, false)
                .addField("클랜원",privates, false);
        message.getChannel().sendMessageEmbeds(builder.build()).queue();
    }

    @Override
    public @NotNull String getHelp() {
        return "클랜 정보를 조회하는 명령어입니다.";
    }

    @Override
    public String @NotNull [] getInvoke() {
        return new String[] {"클랜정보", "blitzclan", "bc"};
    }

    @Override
    public @NotNull String getSmallHelp() {
        return "클랜 정보를 조회하는 명령어입니다.";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }

    @Override
    public boolean isOwnerOnly() {
        return false;
    }

    @Override
    public boolean isMusicOnly() {
        return false;
    }
}
