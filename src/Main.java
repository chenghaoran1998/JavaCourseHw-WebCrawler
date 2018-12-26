public class Main {

    private static boolean getData = false;

    public static void main(String[] args) throws Exception {

        String indexPath="./index";		    //索引所在目录
        Search w=new Search();              //创建搜索

        if ( getData ){                     //如果需要获取数据
            Crawler.mainPageCrawling();     //创建爬虫，爬取信息
            w.createIndex(indexPath);		//创建索引
        }

        w.search(indexPath);			    //搜索

    }


}

