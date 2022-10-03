package me.kirito5572.objects;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FilterSystem {
    private final Logger logger = LoggerFactory.getLogger(FilterSystem.class);
    private final List<String> filterList = new ArrayList<>();
    private final List<String[]> whiteFilterList = new ArrayList<>();
    private final MySQLConnector mySQLConnector;

    public FilterSystem(MySQLConnector mySQLConnector) {
        this.mySQLConnector = mySQLConnector;
    }


    /**
     * Refresh filter words from database
     * @return if True, refresh success, if False, refresh fail
     */
    public boolean filterRefresh() {
        boolean isRefreshEnd = false;
        try (ResultSet resultSet = mySQLConnector.Select_Query(
                "SELECT * FROM blitz_bot.FilterWord;",
                new int[]{},
                new String[]{})) {
            filterList.clear();
            isRefreshEnd = true;
            while(resultSet.next()) {
                filterList.add(resultSet.getString("Word"));
            }
        } catch (SQLException sqlException) {
            logger.error(sqlException.getMessage());
            logger.error(sqlException.getSQLState());
            sqlException.printStackTrace();
        }
        return isRefreshEnd;
    }

    /**
     * Refresh filter's white list from database
     * @return if True, refresh success, if False, refresh fail
     */

    public boolean whiteFilterRefresh() {
        boolean isRefreshEnd = false;
        try (ResultSet resultSet = mySQLConnector.Select_Query(
                "SELECT * FROM blitz_bot.WhiteListWord;",
                new int[]{},
                new String[]{})) {
            whiteFilterList.clear();
            isRefreshEnd = true;
            while(resultSet.next()) {
                String[] data = new String[2];
                data[0] = resultSet.getString("FilterWord");
                data[1] = resultSet.getString("Word");
                whiteFilterList.add(data);
            }
        } catch (SQLException sqlException) {
            logger.error(sqlException.getMessage());
            logger.error(sqlException.getSQLState());
            sqlException.printStackTrace();
        }

        return isRefreshEnd;
    }

    public void wordUpdate(boolean isWhiteList, boolean isInsert, String[] word) throws SQLException {
        if(isInsert) {
            //INSERT WORD
            if(isWhiteList) {
                mySQLConnector.Insert_Query("INSERT INTO blitz_bot.WhiteListWord (FilterWord, Word)VALUES (?, ?);",
                        new int[]{mySQLConnector.STRING, mySQLConnector.STRING},
                        new String[]{word[0], word[1]});
                whiteFilterList.add(word);
            } else {
                mySQLConnector.Insert_Query("INSERT INTO blitz_bot.FilterWord (Word) VALUES (?);",
                        new int[]{mySQLConnector.STRING},
                        new String[]{word[0]});
                filterList.add(word[0]);
            }
        } else {
            //DELETE WORD
            if(isWhiteList) {
                mySQLConnector.Insert_Query("DELETE FROM blitz_bot.WhiteListWord WHERE FilterWord = ? AND Word = ?;",
                        new int[]{mySQLConnector.STRING, mySQLConnector.STRING},
                        new String[]{word[0], word[1]});
                whiteFilterList.remove(word);
            } else {
                mySQLConnector.Insert_Query("DELETE FROM blitz_bot.FilterWord WHERE Word = ?;",
                        new int[]{mySQLConnector.STRING},
                        new String[]{word[0]});
                filterList.remove(word[0]);
            }
        }
    }

    public boolean commandAuthorityCheck(@NotNull List<String> args, @NotNull EventPackage event, boolean isWhiteList) {
        Member member = event.getMember();
        assert member != null;
        Guild guild = event.getGuild();
        Role role = guild.getRoleById("827009999145926657");
        if (!member.getRoles().contains(role)) {
            boolean passRole = member.getRoles().contains(event.getGuild().getRoleById("827010848442548254")) ||    //R:모더레이터
                    member.getRoles().contains(event.getGuild().getRoleById("827009999145926657")) ||               //R:Administrator
                    member.getRoles().contains(event.getGuild().getRoleById("827011445187280906"));                 //R:컨트리뷰터
            if (!passRole) {
                event.getChannel().sendMessage("명령어를 사용할 권한이 없습니다.").queue();
                return false;
            }
        }
        if(args.isEmpty()) {
            event.getChannel().sendMessage("필터링 단어를 입력하여주십시오").queue();
            return false;
        }
        if(isWhiteList && (args.size() == 1)) {
            event.getChannel().sendMessage("화이트 리스트에 추가될 단어만 입력하였습니다. 예외 단어도 입력하여주십시오").queue();
            return false;
        }
        return true;
    }

    public @NotNull List<String> getFilterList() {
        return filterList;
    }

    public @NotNull List<String[]> getWhiteFilterList() {
        return whiteFilterList;
    }

}
