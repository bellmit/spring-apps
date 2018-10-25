package com.haozhuo.datag.model.textspilt;

import com.haozhuo.datag.service.DataetlJdbcService;
import org.ansj.app.keyword.Keyword;
import org.ansj.domain.Term;
import org.ansj.recognition.impl.StopRecognition;
import org.ansj.splitWord.Analysis;
import org.ansj.splitWord.analysis.BaseAnalysis;
import org.nlpcn.commons.lang.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by Lucius on 10/16/18.
 */
public class MyKeyWordComputer<T extends Analysis> {
    private static Logger logger = LoggerFactory.getLogger(MyKeyWordComputer.class);

    private static StopRecognition getStopRecognition() {
        long beginTime = System.currentTimeMillis();
        StopRecognition filter = new StopRecognition();

        List<String> stopWords = DataetlJdbcService.stopwords;
        for (String stopWord : stopWords) {
            filter.insertStopWords(stopWord); //过滤单词
        }
        logger.info("init StopRecognition finish,cost:{}ms", System.currentTimeMillis() - beginTime);
        return filter;

    }

    private static final Map<String, Double> POS_SCORE = new HashMap<String, Double>();
    private static final StopRecognition stopRecognition = getStopRecognition();
    private T analysisType;


    static {
        POS_SCORE.put("null", 0.0);
        POS_SCORE.put("w", 0.0);
        POS_SCORE.put("en", 0.0);
        POS_SCORE.put("m", 0.0);
        POS_SCORE.put("num", 0.0);
        POS_SCORE.put("nr", 3.0);
        POS_SCORE.put("nrf", 3.0);
        POS_SCORE.put("nw", 3.0);
        POS_SCORE.put("nt", 3.0);
        POS_SCORE.put("l", 0.2);
        POS_SCORE.put("a", 0.2);
        POS_SCORE.put("nz", 3.0);
        POS_SCORE.put("v", 0.2);
        POS_SCORE.put("kw", 6.0); //关键词词性
    }

    private int nKeyword = 5;


    public MyKeyWordComputer() {
    }

    public void setAnalysisType(T analysisType) {
        this.analysisType = analysisType;
    }


    /**
     * 返回关键词个数
     *
     * @param nKeyword
     */
    public MyKeyWordComputer(int nKeyword) {
        this.nKeyword = nKeyword;
        this.analysisType = (T) new BaseAnalysis();//默认使用NLP的分词方式

    }

    public MyKeyWordComputer(int nKeyword, T analysisType) {
        this.nKeyword = nKeyword;
        this.analysisType = analysisType;
    }

    /**
     * @param content 正文
     * @return
     */
    private List<Keyword> computeArticleTfidf(String content, int titleLength) {
        //long begin = System.currentTimeMillis();
        Map<String, Keyword> tm = new HashMap<String, Keyword>();

        List<Term> parse = analysisType.parseStr(content)
                .recognition(stopRecognition)
                .getTerms();

        for (Term term : parse) {
            double weight = getWeight(term, content.length(), titleLength);
            if (weight == 0) {
                continue;
            }

            Keyword keyword = tm.get(term.getName());


            if (keyword == null) {
                keyword = new Keyword(term.getName(), term.termNatures().allFreq, weight);
                tm.put(term.getName(), keyword);
            } else {
                keyword.updateWeight(1);
            }
        }

        TreeSet<Keyword> treeSet = new TreeSet<Keyword>(tm.values());

        ArrayList<Keyword> arrayList = new ArrayList<Keyword>(treeSet);
      //  logger.info("extract words cost:{}ms", System.currentTimeMillis() - begin);
        if (treeSet.size() <= nKeyword) {
            return arrayList;
        } else {
            return arrayList.subList(0, nKeyword);
        }

    }

    /**
     * @param title   标题
     * @param content 正文
     * @return
     */
    public List<Keyword> computeArticleTfidf(String title, String content) {
        if (StringUtil.isBlank(title)) {
            title = "";
        }
        if (StringUtil.isBlank(content)) {
            content = "";
        }
        return computeArticleTfidf(title + "\t" + content, title.length());
    }

    /**
     * 只有正文
     *
     * @param content
     * @return
     */
    public List<Keyword> computeArticleTfidf(String content) {
        return computeArticleTfidf(content, 0);
    }

    private double getWeight(Term term, int length, int titleLength) {
        if (term.getName().trim().length() < 2) {
            return 0;
        }

        String pos = term.natrue().natureStr;

        Double posScore = POS_SCORE.get(pos);

        if (posScore == null) {
            posScore = 1.0;
        } else if (posScore == 0) {
            return 0;
        }

        if (titleLength > term.getOffe()) {
            return 5 * posScore;
        }
        return (length - term.getOffe()) * posScore / length;
    }
}
