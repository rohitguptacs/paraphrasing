/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edmatch;

import edmatch.data.PPPair;
import edmatch.data.ExtToken;
import edmatch.data.Token;
import edmatch.data.LdPPSPair;
import edmatch.matching.LevenshteinDistancePPall;
import edmatch.matching.LevenshteinDistance;
import edmatch.matching.LevenshteinDistancePP;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.Comparator;
import java.lang.Exception;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.text.NumberFormat;

/**
 *
 * @author rohit
 */
public class EDMatch {

    /**
     * @param str
     * @param cand
     */
    
    
    
    public static double calcSimilarity(final Token[] str,
            final Token cand[]) {
        LevenshteinDistance dc= new LevenshteinDistance();
        if (str.length == 0 && cand.length == 0) {
            // empty token lists - can't calculate similarity
            return 0;
        }
        int ld = dc.compute(str, cand);
        double similarity = (100.0 * (Math.max(str.length, cand.length) - ld)) / Math.max(str.length, cand.length);
        return similarity;
    }
    
    /**
     *
     * @param str
     * @param cand
     * @param score edit distance
     * @return 
     */
    public static double calcSimilarityPP(int str,int cand, int score
            ) {
        
        double similarity = (100.0 * (Math.max(str, cand) - score)) / Math.max(str, cand);
        System.out.println("ED:"+score+"Sim:"+similarity+" input:"+str+" Match:"+cand);
        return similarity;
    }
    
    /**
     *
     * @param tks token
     * @return
     */
    
    static void printtokens(Token [] tks){
        for (Token tk : tks) {
            System.out.print(tk.getText() + " ");
        }
        System.out.println();
    }
    
    
            /*class SIM_SORT implements Comparator<match>{
                @Override
                public int compare(match m1, match m2){
                    if(m1.sim <m2.sim) return -1;
                    if(m1.sim>m2.sim) return 1;
                    return 0;
                }
            }*/
    /**
     * extract matches in two steps 
     * step1: find matches using simple edit distance
     * step2: Apply paraphrasing to matches find from step 1
     * @param th threshold for step 1 filtering using edit distance
     * @param max filtering too many extracted using simple edit distance
     * @param flag type of paraphrasing '-p' or '-pa' 
     * @param inputtokens user input file tokens
     * @param tmsrctokens tm source tokens
     * @param tgtTM   tm target tokens
     * @param tgtinput expected target for user input file
     * @param outfile  output file in xml format 
     * @return 
    */
    
    static ArrayList<MatchStore>  extractMatch(HashMap<String, ArrayList<String> > ppdict,double th,int max, char flag, ArrayList<Token []> inputtokens, ArrayList<Token []> tmsrctokens,ArrayList<String> tgtTM, ArrayList<String> tgtinput, String outfile){
        ArrayList<MatchStore> listms=new ArrayList();
        
     
            class match implements Comparable<match> {
                double sim;
                int index;
                match(double sim,int index){
                    this.sim=sim;
                    this.index=index;
                }
                @Override
                public int compareTo(match other){
                    return Double.valueOf(this.sim).compareTo(other.sim);
                }
                
            }
    
            
        try{
            FileWriter fw = new FileWriter(outfile);
                int sno=-1;
                fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                fw.write("<document>");
                
                for(Token [] sen:inputtokens){
                    //   MatchStore m=new MatchStore(sen);
                    sno++;
                    System.out.print("Executing->"+sno+" :");
                    printtokens(sen);
                    
                    //double maxsim=0.0;
                    //int maxmatchid=0;
                    int index=0;
                    List<match> thmatches=new ArrayList();
                    for(Token [] tmsen:tmsrctokens){
                        double sim=calcSimilarity(tmsen, sen);
                        // printtokens(tmsen);
                        // printtokens(sen);
                        //  System.out.println(" SIM:"+sim);
                        if(th<sim){
                            thmatches.add(new match(sim,index));
                            
                        }
                        index++;
                        
                    }
                    Collections.sort(thmatches);
                    Collections.reverse(thmatches);
                    ArrayList<SentencePP> topmatches=new ArrayList();
                    for(int i=0;i<max;i++){
                        match m=thmatches.get(i);
                        SentencePP spp=new SentencePP(tmsrctokens.get(m.index),ppdict);
                        topmatches.add(spp);
                    }
                    
                    double maxsim=0.0;
                    int maxmatchid=0;
                    LdPPSPair bestmatch=null;
                    index=0;
                    //System.out.print("Processing:");
                    int tmsno=0;
                    for(SentencePP tmsen:topmatches){
                        // double sim=calcSimilarity(tmsen.get(), sen);
                        //System.out.println("tmsen:");
                        //tmsen.print();
                        System.out.print(++tmsno+"=");
                        LdPPSPair ldpair;
                        double sim;
                        if(flag=='a'){
                            LevenshteinDistancePP dc= new LevenshteinDistancePP();
                            ldpair = dc.compute(sen,tmsen.get());
                            if(ldpair==null)continue;
                            sim=calcSimilarityPP(sen.length,ldpair.length(),ldpair.getEditDistance());
                        }else {
                            LevenshteinDistancePPall dc= new LevenshteinDistancePPall();
                            ldpair = dc.compute(sen,tmsen.get());
                            if(ldpair==null)continue;
                            sim=calcSimilarityPP(sen.length,ldpair.length(),ldpair.getEditDistance());                        
                        }
                        // printtokens(tmsen);
                        // printtokens(sen);
                        //  System.out.println(" SIM:"+sim);
                        if(maxsim<sim){
                            maxsim=sim;
                            maxmatchid=index;
                            bestmatch=ldpair;
                        }
                        /* if(sim>TH){
                        m.add(tmsen);
                        fw.write(sim+" ||| ");
                        for(int i=0;i<tmsen.length;i++)
                        fw.write(tmsen[i].getText()+" ");
                        fw.write("\n");
                        }*/
                        index++;
                        
                    }                    
                    
                    fw.write("<segment id=\""+sno+"\">");
                    for (Token sen1 : sen) {
                        fw.write(sen1.getText() + " ");
                    }
                    fw.write('\n');
                    fw.write("<exptd>");
                    fw.write(tgtinput.get(sno));
                    fw.write("</exptd>\n");
                    String ms=Math.round(maxsim*100.0)/100.0 + "";
                    fw.write("<match score=\""+ms+"\" prevrank=\""+maxmatchid+"\">");
                    //Token [] bestmatch=tmsrctokens.get(maxmatchid);
                    for (short i=0;i<bestmatch.length();i++) {
                        fw.write(bestmatch.getTokenAt(i).getText() + " ");
                    }
                    fw.write("</match>\n");
                    fw.write("<paraphrases>");
                    for(PPPair item : bestmatch.getMatchedParaphrases()){
                        fw.write("<pp index=\"");
                        fw.write(item.getLocation()+"\""+" text=\""+item.getParaphrase().getleft()+"\" "+"para=\""+item.getParaphrase().getright()+"\"");
                        fw.write("/>");
                    }
                    fw.write("</paraphrases>\n");
                    fw.write("<retvd lang=\"FR\">");
                    String french=tgtTM.get(maxmatchid);
                    fw.write(french);fw.write("</retvd>\n");
                    fw.write("<retvd lang=\"EN\" id=\""+thmatches.get(maxmatchid).index+"\">");
                            SentencePP spp=topmatches.get(maxmatchid);
                            ExtToken [] english=spp.get();
                    for(ExtToken extk:english){
                        fw.write(extk.getToken().getText()+" ");
                    }
                    fw.write("</retvd>\n");
                    fw.write("</segment>\n");               
                    
                }
                   fw.write("</document>");
                   fw.close();
            
            }catch(IOException e){
                System.err.print(e);
            }
        return listms;
    }

    /**
     * extract matches without paraphrasing
     * @param inputtokens user input file tokens
     * @param tmsrctokens tm source tokens
     * @param tgtTM   tm target tokens
     * @param tgtinput expected target for user input file
     * @param outfile  output file in xml format 
     * @return 
     */
    
    static ArrayList<MatchStore>  extractMatch(ArrayList<Token []> inputtokens, ArrayList<Token []> tmsrctokens,ArrayList<String> tgtTM, ArrayList<String> tgtinput, String outfile){
        ArrayList<MatchStore> listms=new ArrayList();
        try{
            FileWriter fw = new FileWriter(outfile);
                int sno=-1;
                fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                fw.write("<document>");
                
                for(Token [] sen:inputtokens){
                    //   MatchStore m=new MatchStore(sen);
                    sno++;
                    System.out.print("Executing->"+sno+" :");
                    printtokens(sen);
                    
                    double maxsim=0.0;
                    int maxmatchid=0;
                    int index=0;
                    for(Token [] tmsen:tmsrctokens){
                        double sim=calcSimilarity(tmsen, sen);
                        // printtokens(tmsen);
                        // printtokens(sen);
                        //  System.out.println(" SIM:"+sim);
                        if(maxsim<sim){
                            maxsim=sim;
                            maxmatchid=index;
                        }
                        /* if(sim>TH){
                        m.add(tmsen);
                        fw.write(sim+" ||| ");
                        for(int i=0;i<tmsen.length;i++)
                        fw.write(tmsen[i].getText()+" ");
                        fw.write("\n");
                        }*/
                        index++;
                        
                    }
                    fw.write("<segment id=\""+sno+"\">");
                    for(int i=0;i<sen.length;i++)
                        fw.write(sen[i].getText()+" ");
                    fw.write('\n');
                    fw.write("<exptd>");
                    fw.write(tgtinput.get(sno));
                    fw.write("</exptd>\n");
                    String ms=Math.round(maxsim*100.0)/100.0 + "";
                    fw.write("<match score=\""+ms+"\" id=\""+maxmatchid+"\">");
                    Token [] bestmatch=tmsrctokens.get(maxmatchid);
                    String french=tgtTM.get(maxmatchid);
                    for (Token bestmatch1 : bestmatch) {
                        fw.write(bestmatch1.getText() + " ");
                    }
                    fw.write("</match>\n");
                    fw.write("<retvd lang=\"FR\">");
                    fw.write(french);fw.write("</retvd>\n");
                    fw.write("</segment>\n");
                    
                    //       listms.add(m);
                }
                   fw.write("</document>");
                   fw.close();
            
            }catch(IOException e){
                System.err.print(e);
            }
        return listms;
    }
    
    /**
     * extract matches with paraphrasing
     * @param inputtokens user input file tokens
     * @param tmsrctokens tm source tokens
     * @param tgtTM   tm target tokens
     * @param tgtinput expected target for user input file
     * @param outfile  output file in xml format 
     * @param flag  paraphrasing version 1 or 2
     * @return 
     */
    
    static int extractMatch(char flag, ArrayList<Token []> inputtokens, ArrayList<SentencePP> tmsrcexttokens,ArrayList<String> tgtTM, ArrayList<String> tgtinput, String outfile){
        try{
            FileWriter fw = new FileWriter(outfile);
                int sno=-1;
                fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                fw.write("<document>");
                for(Token [] sen:inputtokens){
                    //   MatchStore m=new MatchStore(sen);
                    sno++;
                    System.out.print("Executing->"+sno+" :");
                    printtokens(sen);
                    
                    double maxsim=0.0;
                    int maxmatchid=0;
                    LdPPSPair bestmatch=null;
                    int index=0;
                    //System.out.print("Processing:");
                    int tmsno=0;
                    for(SentencePP tmsen:tmsrcexttokens){
                        // double sim=calcSimilarity(tmsen.get(), sen);
                        //System.out.println("tmsen:");
                        //tmsen.print();
                        System.out.print(++tmsno+"=");
                        LdPPSPair ldpair;
                        double sim;
                        if(flag=='a'){
                            LevenshteinDistancePP dc= new LevenshteinDistancePP();
                            ldpair = dc.compute(sen,tmsen.get());
                            if(ldpair==null)continue;
                            sim=calcSimilarityPP(sen.length,ldpair.length(),ldpair.getEditDistance());
                        }else {
                            LevenshteinDistancePPall dc= new LevenshteinDistancePPall();
                            ldpair = dc.compute(sen,tmsen.get());
                            if(ldpair==null)continue;
                            sim=calcSimilarityPP(sen.length,ldpair.length(),ldpair.getEditDistance());                        
                        }
                        // printtokens(tmsen);
                        // printtokens(sen);
                        //  System.out.println(" SIM:"+sim);
                        if(maxsim<sim){
                            maxsim=sim;
                            maxmatchid=index;
                            bestmatch=ldpair;
                        }
                        /* if(sim>TH){
                        m.add(tmsen);
                        fw.write(sim+" ||| ");
                        for(int i=0;i<tmsen.length;i++)
                        fw.write(tmsen[i].getText()+" ");
                        fw.write("\n");
                        }*/
                        index++;
                        
                    }
                    System.out.println();
                    fw.write("<segment id=\""+sno+"\">");
                    for (Token sen1 : sen) {
                        fw.write(sen1.getText() + " ");
                    }
                    fw.write('\n');
                    fw.write("<exptd>");
                    fw.write(tgtinput.get(sno));
                    fw.write("</exptd>\n");
                    String ms=Math.round(maxsim*100.0)/100.0 + "";
                    fw.write("<match score=\""+ms+"\" id=\""+maxmatchid+"\">");
                    //Token [] bestmatch=tmsrctokens.get(maxmatchid);
                    for (short i=0;i<bestmatch.length();i++) {
                        fw.write(bestmatch.getTokenAt(i).getText() + " ");
                    }
                    fw.write("</match>\n");
                    fw.write("<paraphrases>");
                    for(PPPair item : bestmatch.getMatchedParaphrases()){
                        fw.write("<pp index=\"");
                        fw.write(item.getLocation()+"\""+" text=\""+item.getParaphrase().getleft()+"\" "+"para=\""+item.getParaphrase().getright()+"\"");
                        fw.write("/>");
                    }
                    fw.write("</paraphrases>\n");
                    fw.write("<retvd lang=\"FR\">");
                    String french=tgtTM.get(maxmatchid);
                    fw.write(french);fw.write("</retvd>\n");
                    fw.write("<retvd lang=\"EN\">");
                            SentencePP spp=tmsrcexttokens.get(maxmatchid);
                            ExtToken [] english=spp.get();
                    for(ExtToken extk:english){
                        fw.write(extk.getToken().getText()+" ");
                    }
                    fw.write("</retvd>\n");
                    fw.write("</segment>\n");
                    
                    //       listms.add(m);
                }
                fw.write("</document>");
                fw.close();
            
            }catch(IOException e){
                System.err.print(e);
            }
        return 0;
    }
    
    /**
     * This method do not use paraphrasing 
     * Edit distance based matching 
     * @return 
     */
    private static int withoutparaphrasing(String tmsrcfilename,String tmtgtfilename,String inputfilename ,String inputtgtfilename,String outfile){
       /* String inputfilename="//Users//rohit//expert//programs//corpus//test1//enfr2013releaseutf8.en.fil.txt";
        String tmsrcfilename="//Users//rohit//expert//programs//corpus//test1//enfr2011_710_12releaseutf8.en.fil.txt";
       // String tmsrcfilename="//Users//rohit//expert//programs//levdistance//enfrppsTMsrc.txt";
        String tmtgtfilename="//Users//rohit//expert//programs//corpus//test1//enfr2011_710_12releaseutf8.fr.fil.txt";
        String inputtgtfilename="//Users//rohit//expert//programs//corpus//test1//enfr2013releaseutf8.fr.fil.txt";*/
        
   /*     String inputfilename="//Users//rohit//expert//programs//corpus//enfr2013releaseutf8.en.fil.txt.200";
        String tmsrcfilename="//Users//rohit//expert//programs//corpus//enfr2011_710_12releaseutf8.en.fil.txt";
       // String tmsrcfilename="//Users//rohit//expert//programs//levdistance//enfrppsTMsrc.txt";
        String tmtgtfilename="//Users//rohit//expert//programs//corpus//enfr2011_710_12releaseutf8.fr.fil.txt";
        String inputtgtfilename="//Users//rohit//expert//programs//corpus//enfr2013releaseutf8.fr.fil.txt.200";
        String outfile="//Users//rohit//expert//programs//corpus//output//without.enfr2013releaseutf8.en.topmatch.200.xml";
*/
                long starttime=System.nanoTime();

        ReadTargetFile rtf=new ReadTargetFile(tmtgtfilename);
        ArrayList<String> tgtTM=rtf.get();
        ReadTargetFile rtf2=new ReadTargetFile(inputtgtfilename);
        ArrayList<String> tgtinput=rtf2.get();
        
        ReadFile rf=new ReadFile(inputfilename);
        ArrayList<Token []> inputtokens=rf.get();
        ReadFile rf2=new ReadFile(tmsrcfilename);
        ArrayList<Token []> tmsrctokens=rf2.get();
       // ArrayList<MatchStore> matches= new ArrayList();
        
        long endtime1=System.nanoTime();
        System.out.println("Total time in Collecting tokens (Seconds)="+TimeUnit.SECONDS.convert(endtime1-starttime,TimeUnit.NANOSECONDS));
        extractMatch(inputtokens,tmsrctokens,tgtTM,tgtinput,outfile);
        long endtime2=System.nanoTime();

        System.out.println("Total time in Editdistance(Seconds)="+TimeUnit.SECONDS.convert(endtime2-endtime1,TimeUnit.NANOSECONDS));
        System.out.println("Complete time(Seconds)="+TimeUnit.SECONDS.convert(endtime2-starttime,TimeUnit.NANOSECONDS));

        return 0;
    }
    
    /**
     * This method use paraphrasing with
     * Edit distance based matching 
     * @return 
     */
    
    private static int withparaphrasing(char flag, String ppfilename,String tmsrcfilename,String tmtgtfilename,String inputfilename ,String inputtgtfilename,String outfile){
        
     /*   String inputfilename="//Users//rohit//expert//programs//corpus//test1//enfr2013releaseutf8.en.fil.txt";
        String tmsrcfilename="//Users//rohit//expert//programs//corpus//test1//enfr2011_710_12releaseutf8.en.fil.txt";
       // String tmsrcfilename="//Users//rohit//expert//programs//levdistance//enfrppsTMsrc.txt";
        String tmtgtfilename="//Users//rohit//expert//programs//corpus//test1//enfr2011_710_12releaseutf8.fr.fil.txt";
        String inputtgtfilename="//Users//rohit//expert//programs//corpus//test1//enfr2013releaseutf8.fr.fil.txt";
        String outfile="//Users//rohit//expert//programs//corpus//test1//withpp.enfr2013releaseutf8.en.topmatch.xml";
        String ppfilename="//Users//rohit//expert//corpusparaphrase//ppdbsphrasal.txt";

      */  
        /*String inputfilename="//Users//rohit//expert//programs//corpus//test2//enfr2013releaseutf8.en.fil.txt";
        String tmsrcfilename="//Users//rohit//expert//programs//corpus//test2//enfr2011_710_12releaseutf8.en.fil.txt";
       // String tmsrcfilename="//Users//rohit//expert//programs//levdistance//enfrppsTMsrc.txt";
        String tmtgtfilename="//Users//rohit//expert//programs//corpus//test2//enfr2011_710_12releaseutf8.fr.fil.txt";
        String inputtgtfilename="//Users//rohit//expert//programs//corpus//test2//enfr2013releaseutf8.fr.fil.txt";
        String outfile="//Users//rohit//expert//programs//corpus//test2//withpp.enfr2013releaseutf8.en.topmatch.xml";
        */
    /*    String inputfilename="//Users//rohit//expert//programs//corpus//test3//enfr2013releaseutf8.en.fil.txt.500";
        String tmsrcfilename="//Users//rohit//expert//programs//corpus//test3//enfr2011_710_12releaseutf8.en.fil.txt";
       // String tmsrcfilename="//Users//rohit//expert//programs//levdistance//enfrppsTMsrc.txt";
        String tmtgtfilename="//Users//rohit//expert//programs//corpus//test3//enfr2011_710_12releaseutf8.fr.fil.txt";
        String inputtgtfilename="//Users//rohit//expert//programs//corpus//test3//enfr2013releaseutf8.fr.fil.txt.500";
        String outfile="//Users//rohit//expert//programs//corpus//test3//withlpp2.enfr2013releaseutf8.en.topmatch.xml";
        
        String ppfilename="//Users//rohit//expert//corpusparaphrase//ppdblphrasal.txt";
    */    
      /*  String inputfilename="//Users//rohit//expert//programs//corpus//enfr2013releaseutf8.en.fil.txt.200";
        String tmsrcfilename="//Users//rohit//expert//programs//corpus//enfr2011_710_12releaseutf8.en.fil.txt";
       // String tmsrcfilename="//Users//rohit//expert//programs//levdistance//enfrppsTMsrc.txt";
        String tmtgtfilename="//Users//rohit//expert//programs//corpus//enfr2011_710_12releaseutf8.fr.fil.txt";
        String inputtgtfilename="//Users//rohit//expert//programs//corpus//enfr2013releaseutf8.fr.fil.txt.200";
        String outfile="//Users//rohit//expert//programs//corpus//output//withlpp.enfr2013releaseutf8.en.topmatch.10.xml";
        String ppfilename="//Users//rohit//expert//corpusparaphrase//ppdblphrasal.txt";
              */
        long starttime=System.nanoTime();

        ReadTargetFile rtf=new ReadTargetFile(tmtgtfilename);
        ArrayList<String> tgtTM=rtf.get();
        ReadFile rf2=new ReadFile(tmsrcfilename);
        ArrayList<Token []> tmsrctokens=rf2.get();
        CollectPP cpp=new CollectPP(ppfilename);
        HashMap<String, ArrayList<String> > ppdict=cpp.getPPDictionary();
        
        System.out.print("PPDICT "+ppdict.entrySet().size());
        ParaphraseTM cspp=new ParaphraseTM(tmsrctokens,ppdict);
        ArrayList<SentencePP> alspp=cspp.getSentencePP();
        ppdict.clear();
      //  cpp.delete();
        System.out.println("alspp finished");
        //alspp.get(200).print();
        System.out.println();
        ReadTargetFile rtf2=new ReadTargetFile(inputtgtfilename);
        ArrayList<String> tgtinput=rtf2.get(); 
        ReadFile rf=new ReadFile(inputfilename);
        ArrayList<Token []> inputtokens=rf.get();
        long endtime1=System.nanoTime();
        System.out.println("Total time in Collecting tokens and parphrases(Seconds)="+TimeUnit.SECONDS.convert(endtime1-starttime,TimeUnit.NANOSECONDS));
        extractMatch(flag,inputtokens,alspp,tgtTM,tgtinput,outfile);
        long endtime2=System.nanoTime();

        System.out.println("Total time in EditdistancePP(Seconds)="+TimeUnit.SECONDS.convert(endtime2-endtime1,TimeUnit.NANOSECONDS));
        System.out.println("Complete time(Seconds)="+TimeUnit.SECONDS.convert(endtime2-starttime,TimeUnit.NANOSECONDS));

        return 0;
    }
    
    public static void main(String[] args) {
      //  double TH=90.0; // Threshold for fuzzy matching
               // String tmsrcfilename="//Users//rohit//expert//programs//levdistance//enfrppsTMsrc.txt";

    /*    String inputfilename="//Users//rohit//expert//programs//corpus//test8//2013.en.txt";
        String tmsrcfilename="//Users//rohit//expert//programs//corpus//test12//2011.en.txt";
        String tmtgtfilename="//Users//rohit//expert//programs//corpus//test12//2011.fr.txt";
        String inputtgtfilename="//Users//rohit//expert//programs//corpus//test12//2013.fr.txt";
        String outfile="//Users//rohit//expert//programs//corpus//test12//topmatch.xml";
       String ppfilename="//Users//rohit//expert//programs//corpus//test12//ppdbtmp.txt";
      */
     /*   String inputfilename="//Users//rohit//expert//programs//corpus//test8//enfr2013releaseutf8.en.fil.txt.r200";
        String tmsrcfilename="//Users//rohit//expert//programs//corpus//test8//enfr2011_710_12releaseutf8.en.fil.txt.r50000";
       // String tmsrcfilename="//Users//rohit//expert//programs//levdistance//enfrppsTMsrc.txt";
        String tmtgtfilename="//Users//rohit//expert//programs//corpus//test8//enfr2011_710_12releaseutf8.fr.fil.txt.r50000";
        String inputtgtfilename="//Users//rohit//expert//programs//corpus//test8//enfr2013releaseutf8.fr.fil.txt.r200";
        String outfile="//Users//rohit//expert//programs//corpus//test8//withlpp.xml";
        String ppfilename="//Users//rohit//expert//corpusparaphrase//ppdblphrasal.txt";
        
      withparaphrasing('a',ppfilename, tmsrcfilename,tmtgtfilename,inputfilename,inputtgtfilename,outfile);
       */  //      withoutparaphrasing(tmsrcfilename,tmtgtfilename,inputfilename,inputtgtfilename,outfile);

      //  System.out.println("Without PP Finished");
      
        if(!(args.length==5 || args.length==7)){
            System.err.println("Please provide all desired file names and options as follows:\n");
            System.err.println("java -jar EDMatch.jar [-p|-pa ppfilename] tmsrcfilename tmtgtfilename inputfilename inputtgtfilename outfile");
            System.exit(1);
        }
        char paraflag='z';
                if(args.length==7){
                    if(args[0].equals("-p") || args[0].equals("-pa")){
                        paraflag='a';
                    }else {
                        paraflag='b';
                    }
                }
                
                
        if(paraflag=='z' && args.length==5){
            withoutparaphrasing(args[0],args[1],args[2],args[3],args[4]);
        }else if(paraflag!='z' && args.length==7){
            withparaphrasing(paraflag,args[1],args[2],args[3],args[4], args[5], args[6]);
        }
        else{
            System.err.println("Please provide file names in format as follows:\n");
            System.err.println("java -jar EDMatch.jar [-p|-pa ppfilename ] tmsrcfilename tmtgtfilename inputfilename inputtgtfilename outfile");
            System.exit(1);
        }
       
    }
   
}
