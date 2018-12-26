
/*
    本类实现了：
    1。基于Java Jsoup的Web爬虫功能，爬取新浪爱问网站的问答信息
    2。HTML解析功能，对爬取信息进行解析并存储到文件系统之中
 */

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.PrintWriter;

public class Crawler {

    public static void mainPageCrawling() {
        /*
             mainPageCrawling为爬虫工具遍历第一层页面的method。
             本层页面主要选择的是页面上部A~Z以及特殊符号的页签，用于遍历以各个字母开头的问题集合页面。
	         Crawler类爬取的文件是以A～Z的页签为单位存储的，每一个页签下的问题集合信息存储于一个txt文件中。
        */
        try{
            Document doc;
            //获取HTML元素
            doc = Jsoup.connect("https://iask.sina.com.cn/map/q-a.html").get();
            Elements listClass = doc.getElementsByAttributeValue("class", "indexing-nav cf");
            for (Element listElement : listClass) {
                Elements listName = listElement.getElementsByTag("a");
                for (Element element : listName) {
                    String href = element.absUrl("href");
                    //创建新文本与输出流
                    String name = "./data/"+element.text()+".txt";
                    PrintWriter out=new PrintWriter(name,"UTF-8");
                    //进入第二层网页
                    secondPageCrawling(href,out);
                    out.close();
                }
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }

    }

    private static void secondPageCrawling(String url, PrintWriter out){
        /*
            secondPageCrawling为爬虫工具遍历第一层页面第二级目录的method。
            本层页面主要选择的是页面下部1～60页的页签，用于遍历特定字母开头、特定页数的问题集合页面。
            在本工具中，Crawler只爬取每个字母表开头的前三页中的问题。
         */
        try{
            Document doc;
            int count = 0;
            //获取HTML元素
            doc = Jsoup.connect(url).get();
            Elements listClass = doc.getElementsByAttributeValue("class", "indexing-page-ul");
            for (Element listElement : listClass) {
                Elements listName = listElement.getElementsByTag("a");
                for (Element element : listName) {
                    String href = element.absUrl("href");
                    //进入第三层网页
                    thridPageCrawling(href, out);
                    count++;
                    if (count > 3) break;   //只爬取前三页内容
                }

            }
        }
        catch (IOException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void thridPageCrawling(String url, PrintWriter out){
        try{
            Document doc;
            doc = Jsoup.connect(url).get();
            Elements listClass = doc.getElementsByAttributeValue("class", "indexing-list cf");
            for (Element listElement : listClass) {
                Elements listName = listElement.getElementsByTag("a");
                for (Element element : listName) {
                    String href = element.absUrl("href");
                    String name = element.text();
                    //获取UML元素 进入第四层
                    QuestionPageCrawling(href,name, out);
                }
            }
        }
        catch (IOException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void QuestionPageCrawling(String url, String name, PrintWriter out){
        try{
            String dictionary = "./data";
            String field = "";
            Document doc = Jsoup.connect(url).get();
            Elements dir = doc.select("div[class=breadcast-fl]").select("a[target=_blank]");
            boolean flag = false;
            //获取领域信息
            for (Element dirElement : dir){
                String d = dirElement.text().replace('/',' ');  //获取领域信息，替代领域信息中的'/'符号
                field = field+d+" ";
                if (!flag) {
                    dictionary = dictionary+"/"+d;
                    flag = true;
                }
            }

            //控制台输出信息
            System.out.println("-----------------------------------");
            System.out.println(name);
            System.out.println(url);
            System.out.println(dictionary);

            //向文件系统中输出信息
            out.println();
            out.println();
            out.println("$$$$");        //问题标记符号
            out.println(name);          //问题名
            out.println(url);           //问题Url
            out.println(field);         //问题领域
            out.println();

            //答案数
            Elements ansNum = doc.select("h3[class=title-f20]");    //某一版本的页面元素
            if ( ansNum.isEmpty() ) ansNum = doc.select("h2[class=other-title]");


            //答案内容
            //新浪爱问的答案内容页面共有两个版本
            Elements ansContent = doc.select("li[t=disploy]");
            if (ansContent.isEmpty()){  //版本1
                ansContent = doc.select("li[class=good_item]").select("div[class=new-answer-text new-answer-cut new-pre-answer-text]");
                out.println("###");     //答案标记符号
                out.println(ansContent.text());
            }
            else                        //版本2
                for (Element ansC : ansContent) {
                    Elements singleAns = ansC.select("div[class=answer_text]");
                    if (singleAns.isEmpty())
                        singleAns = ansC.select("div[class=new-answer-text new-answer-cut new-pre-answer-text]");
                    out.println("###"); //答案标记符号
                    out.println(singleAns.text());
                }

        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
