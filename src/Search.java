/*
    本类实现了Lucene索引工具：
    Lucene索引工具共分为两个部分：索引建立与索引查找。
        1。索引建立部分在需要创建数据时由主函数调用，用于依据Crawler类爬取的信息建立搜索索引。
           因此需要先读取Crawler建立的文件并解析，再一一载入到Lucene的Document之中；
        2。索引查找部分被主函数调用时，会首先引导用户输入查找的值，并调用Lucene的Query语句来进行查找，最后输出查找得到的相应值。
 */


import java.io.*;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;


public class Search {

    public void createIndex(String filePath) throws Exception {
        /*
         CreateIndex为索引建立方法。本方法进行相应的初始化之后，基本的建立工作都交由getDocument来进行。
         */
        File f=new File(filePath);
        IndexWriter iwr=null;
        try {
            Directory dir=FSDirectory.open(f);
            Analyzer analyzer = new IKAnalyzer();

            IndexWriterConfig conf=new IndexWriterConfig(Version.LUCENE_4_10_0,analyzer);
            iwr=new IndexWriter(dir,conf);//建立IndexWriter。固定套路
            getDocument(iwr);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            iwr.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void getDocument(IndexWriter iwr){
        /*
             getDocument用于读取Crawler创建的文件系统。
             讲文件系统中的信息提取出来，并依据其中的分隔符进行解析，对应到相应的field之后再一一添加进Document之中，
             最后再将各Document进行提交并存储。
         */
        //doc中内容由field构成，在检索过程中，Lucene会按照指定的Field依次搜索每个document的该项field是否符合要求。
        String dataPath="./data";		//数据所在目录
        Document doc=new Document();
        try {

            //文件系统共26字母的文件系统，此处用for循环读取文件
            for (int i=0; i<26; i++){

                //生成文件地址
                char character = (char)((int)'A'+i);
                String txtName = dataPath+"/"+character+".txt";

                //初始化索引字符串
                StringBuilder addStr = new StringBuilder("");
                System.out.println(txtName);

                //初始化索引文件
                File f = new File(txtName);
                Scanner input = new Scanner (f, "UTF-8");

                //按行遍历文件
                while(input.hasNext()){

                    //按行读取文件
                    String line = input.next();
                    String str = line;

                    //若查找到问题标记符
                    if (line.equals("$$$$")){

                        //插入字符串，存储之前的doc
                        Field fi = new TextField("QA", addStr.toString(), Field.Store.YES);
                        doc.add(fi);
                        System.out.println(fi.toString());
                        iwr.addDocument(doc);

                        //清空字符串，创建新的doc
                        addStr = new StringBuilder("");
                        doc = new Document();

                        //处理问题标记符
                        str = "【问题】";
                    }

                    //处理订单标记符
                    if (line.equals("###")) str = "【回答】";

                    //添加字符串
                    addStr.append('\n');
                    addStr.append(str);
                }

                Field fi = new TextField("QA", addStr.toString(), Field.Store.YES);
                doc.add(fi);
                System.out.println(fi.toString());
                iwr.addDocument(doc);
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    public void search(String filePath){
        /*
            search方法用于搜索已建立的索引。
            在主函数调用本Search类的search方法之后，本方法会提示用户输入搜索关键词。
            之后，方法会调用Lucene的query工具对关键词进行检索。
            当检索完毕之后，方法会得到一个命中的list，此时，程序只需要将list遍历，并输出结构即可。
         */

        File f=new File(filePath);
        try {
            IndexSearcher searcher=new IndexSearcher(DirectoryReader.open(FSDirectory.open(f)));
            Scanner scan = new Scanner(System.in);

            //引导用户输入
            System.out.println("请输入搜索关键词：");
            String queryStr = scan.nextLine();

            //查询结果
            Analyzer analyzer = new IKAnalyzer();
            QueryParser parser = new QueryParser("QA", analyzer);
            Query query=parser.parse(queryStr);
            TopDocs hits=searcher.search(query,10);

            //输出结果
            for(ScoreDoc doc:hits.scoreDocs){
                Document d=searcher.doc(doc.doc);
                System.out.println();
                System.out.println(d.get("QA"));
            }
        } catch (IOException | ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }



}
