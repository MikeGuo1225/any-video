package cn.ictgu.job;

import cn.ictgu.bean.response.Video;
import cn.ictgu.tools.JsoupUtils;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Company
 * @Discription
 * @Author guoxiaojing
 * @CreateDate 2018/3/19 16:10
 * @Version 1.0
 */
@Component
@Log4j2
@AllArgsConstructor
public class YouKuCrawler {
    private static final String HOME_PAGE_PC = "http://www.youku.com/";
    private static final String HOME_PAGE_PHONE_TV = "http://v.qq.com/x/list/tv";
    private static final String HOME_PAGE_PHONE_MOVIE = "http://v.qq.com/x/list/movie";
    private static final String HOME_PAGE_PHONE_CARTOON = "http://v.qq.com/x/list/cartoon";
    private static final String HOME_PAGE_PHONE_RECOMMEND = "http://v.qq.com/x/list/variety";
    private static final String TAG = "YOUKU";

    private final RedisSourceManager redisSourceManager;

    //每天执行
//    @Scheduled(cron = " 0 22 13 ? * *")
    @Scheduled(fixedRate = 60*60*1000)//每一小时执行一下
    public void start(){
        Document docWithPC = JsoupUtils.getDocWithPC(HOME_PAGE_PC);
        saveCarouselsToRedis(docWithPC);
    }

    private void saveCarouselsToRedis(Document document){
        List<Video> carouselVideos = new ArrayList<>();
        Elements elements = document.select("div #m_250379 ul li");
        for (Element carousel : elements) {
            Video video = new Video();
            String title = carousel.getElementsByTag("a").attr("alt");
            String image = carousel.attr("style");
            String lazy = carousel.attr("_lazy");
            String url = carousel.getElementsByTag("a").attr("href");
            video.setValue(url);
            video.setTitle(title);
            if(image.indexOf("background-image") != -1){
                int first = image.indexOf("(", 1);
                int last = image.indexOf(")", 1);
                video.setImage(image.substring(first+2, last-1));
                carouselVideos.add(video);
                continue;
            }else if(lazy.indexOf("background-image") != -1){
                int first = lazy.indexOf("(", 1);
                int last = lazy.indexOf(")", 1);
                video.setImage(lazy.substring(first+2, last-1));
                carouselVideos.add(video);
                continue;
            }else{
                continue;
            }

        }
        String key = redisSourceManager.VIDEO_PREFIX_HOME_CAROUSEL_KEY + "_" + TAG;
        redisSourceManager.saveVideos(key, carouselVideos);
    }

    public static void main(String [] args){
        String s = "background-image: url(http://gw.alicdn.com/mt/TB1Ht5yojnD8KJjSspbXXbbEXXa-1664-520.png)";
        String s1 = s.split("\\(")[1];
        System.out.println(s1.split("\\)")[0]);
    }

}
