package com.sunsheen.hkks.nlp.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BreakSentence {
	static String  REG_EX = "[。；;]|[!?！？]+" ;
	public static String[] splitSentence(String cmt){
        /*正则表达式：句子结束符*/
        Pattern p =Pattern.compile(REG_EX);
        Matcher m = p.matcher(cmt);
        /*按照句子结束符分割句子*/
        String[] sentences = p.split(cmt);
        /*将句子结束符连接到相应的句子后*/
        if(sentences.length > 0)
        {
            int count = 0;
            while(count < sentences.length)
            {
                if(m.find())
                {
                	sentences[count] += m.group();
                }
                count++;
            }
        }
        /*输出结果*/
        return sentences;
    }
}
