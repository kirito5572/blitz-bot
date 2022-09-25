package me.kirito5572.objects;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

//https://wonsama.tistory.com/456의 코드를 편집하여 사용하였습니다. 감사합니다.

//TODO https://github.com/Jylpah/blitz-tools 리플레이시 분석 참고

/**
 * 입력 텍스트를 이미지로 만들어 준다
 * @author parkwon
 * @since 2017.03.24
 */
public class ImageCreator {
    //https://worldoftanks.asia/en/news/media/ 의 사항을 참조합니다.
    private final String[] backGroundImageUrl = new String[] {
            "https://sg-wotp.wgcdn.co/dcont/fb/image/mwp2209_1920x1080px_logo.jpg",
            "https://sg-wotp.wgcdn.co/dcont/fb/image/mwp_2208_1920x1080px_logo.png",
            "https://sg-wotp.wgcdn.co/dcont/fb/image/wallpaper_4_1680x1050.jpg,",                                       //숙삼 파이크 위장
            "https://sg-wotp.wgcdn.co/dcont/fb/image/wallpaper_3_1680x1050.jpg",                                        //오공비 코뿔소 위장 & 프로게토46 사파리 위장
            "https://sg-wotp.wgcdn.co/dcont/fb/image/wallpaper_2_1680x1050.jpg",                                        //오공비 코뿔소 위장
            "https://sg-wotp.wgcdn.co/dcont/fb/image/wallpaper_1_1680x1050.jpg",                                        //프로게토46 사파리 위장
            "https://sg-wotp.wgcdn.co/dcont/fb/image/su-130pm_vs_rheinmetall_skorpion_g_wallpaper_2560x1440.jpg",       //130 & 스콜지
            "https://sg-wotp.wgcdn.co/dcont/fb/image/lowe-wallpaper-2560x1440.jpg",                                     //로루
            "https://sg-wotp.wgcdn.co/dcont/fb/image/t_49_wallpaper_2560x1440.jpg",                                     //통사구
            "https://sg-wotp.wgcdn.co/dcont/fb/image/wallpaper_1920x1080.jpg",                                          //2019 여름
            "https://sg-wotp.wgcdn.co/dcont/fb/image/wot_spring2019.jpg",                                               //2019 봄
            "https://sg-wotp.wgcdn.co/dcont/fb/image/winter_himmelsdorf_jdxnpxn.jpg",                                   //힘멜스도르프
            "https://sg-wotp.wgcdn.co/dcont/fb/image/op_typhoon_2560x1440_wall.jpg",                                    //통곡치프틴(추정)
            "https://sg-wotp.wgcdn.co/dcont/fb/image/karelia_wallpaper_2560x1440.jpg",                                  //카렐리야
            "https://sg-wotp.wgcdn.co/dcont/fb/image/1024x600_wot_warhammer_wp_-21.jpg",                                //워해머 콜라보
            //2017.11 ~ 2018.3 월페이퍼는 파일임
            "https://sg-wotp.wgcdn.co/dcont/fb/image/pzkpfw_vii_2560x1440_noc_ru.jpg",                                  //7호전차
            "https://sg-wotp.wgcdn.co/dcont/fb/image/2560x1440_image-november2017_noc_eng.jpg",                         //이벤트 레비아탄
            "https://sg-wotp.wgcdn.co/dcont/fb/image/wallpaper_october_2017_2560x1440_.jpg",                            //병일 & 4호
            "https://sg-wotp.wgcdn.co/dcont/fb/image/september2017_wallpapers_2560x1440_.jpg",                          //짱구축 3인방
            "https://sg-wotp.wgcdn.co/dcont/fb/image/wz-111-model-5awot_wallpaper2560x1440.jpg",                        //5A
            "https://sg-wotp.wgcdn.co/dcont/fb/image/t26e5_2560x1440_noc_ru.jpg",                                       //점보퍼싱
            "https://sg-wotp.wgcdn.co/dcont/fb/image/su-152_2560x1440_noc_ru.jpg",                                      //수152
            "https://sg-wotp.wgcdn.co/dcont/fb/image/2560x1440_berlin's-fivewot_wallpapereng.jpg",                      //숙투 & 크롬비 & 이지에잇 & 3485M & 이수박122 (베를린 점령)
            "https://sg-wotp.wgcdn.co/dcont/fb/image/2560x1440_strv-s-1wot_wallpapereng.jpg",                           //스트라브 s1
            "https://sg-wotp.wgcdn.co/dcont/fb/image/2560x1440_-obj252ud-wall.jpg",                                     //디펜더(252U)
            "https://sg-wotp.wgcdn.co/dcont/fb/image/feb_2017_wall_2560x1440.jpg",                                      //빵셔먼
            "https://sg-wotp.wgcdn.co/dcont/fb/image/kranvagn_2560x1440_noc_ru.jpg",                                    //크란방
            "https://sg-wotp.wgcdn.co/dcont/fb/image/strv_103b_wallpaper_2560x1440.jpg",                                //103B
            "https://sg-wotp.wgcdn.co/dcont/fb/image/2560x1440_bat-chat-25tap-nov16_wallpaper.jpg",                     //바샷25t
            "https://sg-wotp.wgcdn.co/dcont/fb/image/2560x1440_-t-54_wallpaper.jpg",                                    //떼오사
            "https://sg-wotp.wgcdn.co/dcont/fb/image/2560x1440_121_wallpaper.jpg",                                      //121
            "https://sg-wotp.wgcdn.co/dcont/fb/image/2560x1440_-t110e3jul_wallpaper.jpg",                               //이쓰리
            "https://sg-wotp.wgcdn.co/dcont/fb/image/2560x1440wallp_churchillvii.jpg",                                  //처칠칠
            "https://sg-wotp.wgcdn.co/dcont/fb/image/wot_t-34_2560x1440_noc_ru.jpg",                                    //3476
            "https://sg-wotp.wgcdn.co/dcont/fb/image/2560x1440_u3pxSPA.jpg",                                            //이벤트 숙팔
            "https://sg-wotp.wgcdn.co/dcont/fb/image/wot_amx50foch(155)_wall_2560x1440_noc_ru.jpg",                     //고슈
            "https://sg-wotp.wgcdn.co/dcont/fb/image/wot_wallpaper_jp-e100_2560x1440_noc_ru.jpg",                       //야이백
            "https://sg-wotp.wgcdn.co/dcont/fb/image/wot_tvp_wallpaper_2560x1440_noc_ru.jpg"                            //5051
            //2016년 1월까지 땄음
    };

    //1200x735 이미지
    private final BufferedImage img;
    private final Graphics g;

    /**
     * 테스트용 진입점
     *
     * @param args 파라미터
     * @since 2017.03.24
     */
    public static void main(String[] args) {
        try {
            new ImageCreator();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    /**
     * 생성자
     *
     * @since 2017.03.24
     */
    ImageCreator() throws Exception {

        img = resizeImage(ImageIO.read(new URL(backGroundImageUrl[(int)(Math.random()*10000)%backGroundImageUrl.length])),1200,735);
        g = img.getGraphics();
        drawTextWithImage((int)(Math.random()*10000)%2500, (int)(Math.random()*10000)%3000);
    }

    /**
     * 웹 이미지 내부에 텍스트를 포함하여 이미지로 만들어준다
     *
     * @throws Exception 오류
     * @since 2017.03.27
     */
    public void drawTextWithImage(int WN7, int WN8) throws Exception {
        Graphics2D g2D = getG2D(img);
        Color color = new Color(255, 255, 255, 79);
        g2D.setPaint(color);
        g2D.fillRoundRect(25, 15, 1000, 60, 7, 7);
        g2D.fillRoundRect(50, 130, 350, 540, 20, 20);
        g2D.fillRoundRect(500, 130, 600, 540, 20, 20);
        g2D.dispose();

        inputWord("XPERT_kirito[XPERT]", 30, 20, 40, Color.WHITE);
        inputWord("30일 전적",60, 150, 50, Color.WHITE);
        inputWord("승률: 55.55%", 75, 250, 30, Color.WHITE);
        inputWord("전투: 55", 75, 295, 30, Color.WHITE);
        inputWord("평균 데미지: 555.5", 75, 340, 30, Color.WHITE);
        inputWord("평균 티어: 5.5", 75, 385, 30, Color.WHITE);
        inputWord("생존율: 33.3%", 75, 430, 30, Color.WHITE);
        inputWord("WN7", 75, 500, 40, Color.WHITE);
        inputWord(String.valueOf(WN7), 195, 475, 60, WN7Cal(WN7));
        inputWord("WN8", 75, 600, 40, Color.WHITE);
        inputWord(String.valueOf(WN8), 195, 575, 60, WN7Cal(WN8));

        inputWord("그외 전적",880, 150, 50, Color.WHITE);
        inputWord("60일", 515, 220, 25, Color.WHITE);
        inputWord("90일", 705, 220, 25, Color.WHITE);
        inputWord("전체", 895, 220, 25, Color.WHITE);
        //60일
        inputWord("승률: 55.66%", 515, 280, 20, Color.WHITE);
        inputWord("전투: 66", 515, 325, 20, Color.WHITE);
        inputWord("평균 데미지: 666.6", 515, 370, 20, Color.WHITE);
        inputWord("평균 티어: 6.6", 515, 415, 20, Color.WHITE);
        inputWord("생존율: 33.4%", 515, 470, 20, Color.WHITE);
        inputWord("WN7", 515, 530, 20, Color.WHITE);
        inputWord(String.valueOf(WN7), 570, 505, 40, WN7Cal(WN7));
        inputWord("WN8", 515, 620, 20, Color.WHITE);
        inputWord(String.valueOf(WN8), 570, 605, 40, WN7Cal(WN8));
        //90일
        inputWord("승률: 55.77%", 705, 280, 20, Color.WHITE);
        inputWord("전투: 77", 705, 325, 20, Color.WHITE);
        inputWord("평균 데미지: 777.7", 705, 370, 20, Color.WHITE);
        inputWord("평균 티어: 7.7", 705, 415, 20, Color.WHITE);
        inputWord("생존율: 33.5%", 705, 470, 20, Color.WHITE);
        inputWord("WN7", 705, 530, 20, Color.WHITE);
        inputWord(String.valueOf(WN7), 760, 505, 40, WN7Cal(WN7));
        inputWord("WN8", 705, 620, 20, Color.WHITE);
        inputWord(String.valueOf(WN8), 760, 605, 40, WN7Cal(WN8));
        //전체
        inputWord("승률: 55.88%", 895, 280, 20, Color.WHITE);
        inputWord("전투: 88", 895, 325, 20, Color.WHITE);
        inputWord("평균 데미지: 888.8", 895, 370, 20, Color.WHITE);
        inputWord("평균 티어: 8.8", 895, 415, 20, Color.WHITE);
        inputWord("생존율: 33.6%", 895, 470, 20, Color.WHITE);
        inputWord("WN7", 895, 530, 20, Color.WHITE);
        inputWord(String.valueOf(WN7), 920, 505, 40, WN7Cal(WN7));
        inputWord("WN8", 895, 620, 20, Color.WHITE);
        inputWord(String.valueOf(WN8), 920, 605, 40, WN7Cal(WN8));

        // 이미지 파일을 생성한다
        ImageIO.write(img, "png", new File("C:\\Users\\CKIRUser\\Desktop\\text.png"));
    }

    public void inputWord(String text, int x, int y, int font_size, Color font_color) {
        Font font = new Font("나눔고딕", Font.PLAIN, font_size);
        Graphics2D g2d = getG2D(img);
        g2d.setFont(font);

        drawStringDropshadow(g2d, text, x, (font_size + y), font_color);

        g.dispose();
    }

    /**
     * 그림자 텍스트를 그려준다
     *
     * @param g2d  2D 그래픽스
     * @param text 문자열
     * @param x    X좌표
     * @param y    Y좌표
     * @since 2017.03.27
     */
    private void drawStringDropshadow(Graphics2D g2d, String text, int x, int y, Color font_color) {
        g2d.setColor(new Color(20, 20, 20));
        g2d.drawString(text, ShiftEast(x, 2), ShiftSouth(y, 2));
        g2d.setColor(font_color);
        g2d.drawString(text, x, y);
    }

    /**
     * 폰트를 회전 처리한다
     *
     * @param font  폰트
     * @param angle 회전각 0~360 / -를 넣으면 반시계 방향으로 계산
     * @return 회전처리 된 폰트
     * @since 2017.03.24
     */
    private Font rotatedFont(Font font, double angle) {
        // 폰트 회전처리
        AffineTransform form = new AffineTransform();
        form.rotate(Math.toRadians(angle), 0, 0);
        return font.deriveFont(form);
    }

    /**
     * 이미지 리사이징
     *
     * @param img  이미지
     * @param newW 새로운 넓이
     * @param newH 새로운 높이
     * @return 신규 이미지
     * @since 2017.03.24
     */
    private BufferedImage resizeImage(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }

    /**
     * 이미지에서 2D 그래픽스 개체를 얻어온다
     *
     * @param img 이미지버퍼
     * @return 2D 그래픽스
     * @since 2017.03.24
     */
    private Graphics2D getG2D(BufferedImage img) {
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        return g2d;
    }

    /**
     * 좌표 이동처리 - 아래쪽 / 남
     *
     * @param p        현 좌표
     * @param distance 이동거리
     * @return 변경된 좌표
     * @since 2017.03.27
     */
    int ShiftSouth(int p, int distance) {
        return (p + distance);
    }

    /**
     * 좌표 이동처리 - 오른쪽 / 동
     *
     * @param p        현 좌표
     * @param distance 이동거리
     * @return 변경된 좌표
     * @since 2017.03.27
     */
    int ShiftEast(int p, int distance) {
        return (p + distance);
    }

    Color WN7Cal(int WN7) {
        Color WN7Color = Color.WHITE;
        if(WN7 < 454) {
            WN7Color = Color.RED;
        } else if(455 < WN7 && WN7 < 814) {
            WN7Color = Color.ORANGE;
        } else if(815 < WN7 && WN7 < 1179) {
            WN7Color = Color.YELLOW;
        } else if(1180 < WN7 && WN7 < 1569) {
            WN7Color = Color.GREEN;
        } else if(1570 < WN7 && WN7 < 1889) {
            WN7Color = new Color(0, 160, 160);
        } else if(1890 < WN7 && WN7 < 9999) {
            WN7Color = new Color(85, 10, 138);
        }
        return WN7Color;
    }

    Color WN8Cal(int WN7) {
        Color WN7Color = Color.WHITE;
        if(WN7 < 314) {
            WN7Color = Color.RED;
        } else if(314 < WN7 && WN7 < 754) {
            WN7Color = Color.ORANGE;
        } else if(755 < WN7 && WN7 < 1314) {
            WN7Color = Color.YELLOW;
        } else if(1315 < WN7 && WN7 < 1964) {
            WN7Color = Color.GREEN;
        } else if(1965 < WN7 && WN7 < 2524) {
            WN7Color = new Color(0, 160, 160);
        } else if(2525 < WN7 && WN7 < 9999) {
            WN7Color = new Color(85, 10, 138);
        }
        return WN7Color;
    }
}