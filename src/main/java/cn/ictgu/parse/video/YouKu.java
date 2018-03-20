package cn.ictgu.parse.video;

import cn.ictgu.bean.response.Episode;
import cn.ictgu.bean.response.Video;
import cn.ictgu.parse.Parser;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Company
 * @Discription
 * @Author guoxiaojing
 * @CreateDate 2018/3/20 10:36
 * @Version 1.0
 */
@Log4j2
public class YouKu implements Parser<Video> {
    public static void main(String [] args){
        String jsonUrl = getVideoUrl("http://v.youku.com/v_show/id_XMTgzNDA5OTMy.html");
        System.out.println(jsonUrl);
    }

    public static String getVideoUrl(String htmlUrl) throws JSONException {
        //正则表达式解析地址，取id
        Pattern p = Pattern.compile(".*id_(\\w+)\\.html");
        String u = htmlUrl;
        Matcher m = p.matcher(u);
        String id = "";
        if (m.find()) {
            id = m.group(1);
        }
        System.out.println(id);

//      String id="XMTgzNDA5OTMy";
        String s = getContent("http://v.youku.com/player/getPlayList/VideoIDS/"+id);
        JSONObject jsobj = JSONObject.parseObject(s);
        JSONArray jsonarr = jsobj.getJSONArray("data");
        System.out.println(jsobj);
        JSONObject obj1 = jsonarr.getJSONObject(0);
        String title= obj1.getString("title");
        double seed = obj1.getDouble("seed");
        JSONObject obj2 = obj1.getJSONObject("streamfileids");
        String mp4id = null;
        String flvid = null;
        String format = null;
        try
        {
            mp4id = obj2.getString("mp4");
            format = "mp4";
        } catch (JSONException e)
        {
            System.out.println("没有MP4格式");
            try
            {
                flvid = obj2.getString("flv");
                format = "flv";
            } catch (JSONException e1)
            {
                System.out.println("没有FLV格式");
                return "";
            }
        }
        String realfileid = null;
        if (format.equals("mp4"))
        {
            realfileid = getFileID(mp4id, seed);
        } else
        {
            realfileid = getFileID(flvid, seed);
        }

        String idLeft = realfileid.substring(0, 8);
        String idRight = realfileid.substring(10);

        String sid = genSid();
        JSONObject obj3 = obj1.getJSONObject("segs");
        JSONArray mp4arr = obj3.getJSONArray(format);
        String VideoUrl="";
        for (int i = 0; i < mp4arr.size(); i++)
        {
            JSONObject o = mp4arr.getJSONObject(i);
            String k = o.getString("k");
            String url = "http://f.youku.com/player/getFlvPath/sid/" + sid + "_" + String.format("%1$02X", i) + "/st/" + format
                    + "/fileid/" + idLeft + String.format("%1$02X", i) + idRight + "?K=" + k;
            System.out.println(url);
            VideoUrl+=url+"#";
        }
        return VideoUrl;
    }

    private static String getFileID(String fileid, double seed)
    {
        String mixed = getFileIDMixString(seed);
        String[] ids = fileid.split("\\*");
        StringBuilder realId = new StringBuilder();
        int idx;
        for (int i = 0; i < ids.length; i++)
        {
            idx = Integer.parseInt(ids[i]);
            realId.append(mixed.charAt(idx));
        }
        return realId.toString();
    }

    public static String genSid()
    {
        int i1 = (int) (1000 + Math.floor(Math.random() * 999));
        int i2 = (int) (1000 + Math.floor(Math.random() * 9000));
        return System.currentTimeMillis() + "" + i1 + "" + i2;
    }

    private static String getFileIDMixString(double seed)
    {
        StringBuilder mixed = new StringBuilder();
        StringBuilder source = new StringBuilder("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ/\\:._-1234567890");
        int index, len = source.length();
        for (int i = 0; i < len; ++i)
        {
            seed = (seed * 211 + 30031) % 65536;
            index = (int) Math.floor(seed / 65536 * source.length());
            mixed.append(source.charAt(index));
            source.deleteCharAt(index);
        }
        return mixed.toString();
    }

    public static String getContent(String strUrl) {
        try {

            URL url = new URL(strUrl);
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    url.openStream()));
            String s = "";
            StringBuffer sb = new StringBuffer("");
            while ((s = br.readLine()) != null) {
                sb.append(s);
            }
            br.close();
            return sb.toString();
        } catch (Exception e) {
            return "error open url:" + strUrl;
        }

    }

    @Override
    public Video parse(String url) {
        return null;
    }

    @Override
    public List<Episode> parseEpisodes(String url) {
        return null;
    }
}

