package roll.learner.bb_a.lomega;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import roll.automata.NBA;
import roll.automata.operations.BB_AGenerator;
import roll.automata.operations.NFAOperations;
import roll.learner.LearnerBase;
import roll.main.CLParser;
import roll.main.Options;
import roll.oracle.nba.TeacherNBA;
import roll.oracle.nba.TeacherNBAImpl;
import roll.query.Query;
import roll.table.HashableValue;
import roll.util.Timer;

public class TestCases {
	public static int numOfTargetState = 5;
	public static final int numOfLetter = 5;
	public static void main(String[] args) {
		CLParser clParser = new CLParser();
        clParser.prepareOptions(args);
        Options options = clParser.getOptions();
        removeDir(new File("Experiment"));
        options.log.println("\n" + options.toString());
        options.log.println("Start testing....");
        for(int numOfState = 1; numOfState <=options.numOfStatesForTest;numOfState++)
        {
        	ArrayList<Long> dataLOmega = new ArrayList<Long>();
        	ArrayList<Long> dataLSimple = new ArrayList<Long>();
        	for(int i=0;i< 14;i++) {
        		dataLOmega.add((long) 0);
        		dataLSimple.add((long) 0);
        	}
        	options.stats.numOfStatesInTraget = numOfState;
        	numOfTargetState = numOfState;
        	for(int numLetter = 1; numLetter <= numOfLetter; numLetter++)
        	{
        		System.out.println("Statesize--->"+numOfState+" letters--->"+numLetter);
        		options.stats.numOfLetters = numLetter;
        		for(int numOfCase = 0; numOfCase < options.numOfTests;numOfCase++)
        		{
        			options.log.println("Testing case " + (numOfCase + 1) + " ...");
        			options.log.println("Generating NBA for States: ["+numOfState+ "] letters ["+numLetter+"]");
        			NBA nba = BB_AGenerator.getRandomBB_A(numOfState, numLetter).ToNBA();
            		options.stats.numOfTransInTraget = NFAOperations.getNumberOfTransitions(nba);
        			try{
                        options.log.println("target: \n" + nba.toDot());
                        write2File("Experiment/Automata/"+numOfState+"/"+numLetter+"/", numOfCase+".ba", nba.toBA(),false);
                        write2File("Experiment/Automata/"+numOfState+"/"+numLetter+"/", numOfCase+".dot", nba.toDot(),false);
            			TeacherNBA teacher = new TeacherNBAImpl(options, nba);
            			options.log.println("Use LOmega Algorithm ....");
            		 	LearnerBase<NBA> learnerLOmega = new LearnerBBALOmega(options, nba.getAlphabet(), teacher,true);
                        Learning(options, nba, learnerLOmega, teacher);
                        recordData("Experiment/Collection/"+numOfState+"/"+numLetter+"/", "collection.log", options, numOfCase, dataLOmega);
                        
                        options.log.println("Use LSimple Algorithm ....");
            		 	LearnerBase<NBA> learnerLSimple = new LearnerBBALOmega(options, nba.getAlphabet(), teacher, false);
                        Learning(options, nba, learnerLSimple, teacher);
                        recordData("Experiment/Collection/"+numOfState+"/"+numLetter+"/", "collection.log", options, numOfCase, dataLSimple);
                        
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                        options.log.err("Exception occured, Learning aborted...");
                        options.log.println(nba.toString());
                        System.exit(-1);
                    }
                    options.log.info("Done for case " + (numOfCase + 1));
        		}
        	}
        	///collect all state information
        	recordDataForEveryState("Experiment/", "collection.log", dataLOmega, dataLSimple, options.numOfTests*numOfLetter);
        }
        System.out.println("testing done");
	}
	private static void prepareStats(Options options, LearnerBase<NBA> learner, NBA hypothesis) {
        options.stats.numOfStatesInHypothesis = hypothesis.getStateSize();
        LearnerBBALOmega learnerBBALOmega = (LearnerBBALOmega)learner;
        NBA nba = learnerBBALOmega.getHypothesis();
        options.stats.numOfStatesInLeading = nba.getStateSize();
        options.stats.hypothesis = hypothesis;
        options.stats.numOfStatesInHypothesis = hypothesis.getStateSize();
		options.stats.numOfTransInHypothesis = NFAOperations.getNumberOfTransitions(options.stats.hypothesis);
        options.stats.print();
    }

	public static void Learning(Options options, NBA target, LearnerBase<NBA> learner, TeacherNBA teacher) {
	        Timer timer = new Timer();
	        options.log.println("Initializing learner...");
	        timer.start();
	        learner.startLearning();
	        timer.stop();
	        options.stats.timeOfLearner += timer.getTimeElapsed();
	        NBA hypothesis = null;
	        while(true) {
	            options.log.verbose("Table/Tree is both closed and consistent\n" + learner.toString());
	            hypothesis = learner.getHypothesis();
	            // along with ce
	            options.log.println("Resolving equivalence query for hypothesis (#Q=" + hypothesis.getStateSize() + ")...  ");
	            Query<HashableValue> ceQuery = teacher.answerEquivalenceQuery(hypothesis);
	            boolean isEq = ceQuery.getQueryAnswer().get();
	            if(isEq) {
	                // store statistics
	                prepareStats(options, learner, hypothesis);
	                break;
	            }
	            ceQuery.answerQuery(null);
	            options.log.verbose("Counterexample is: " + ceQuery.toString());
	            timer.start();
	            options.log.println("Refining current hypothesis...");
	            learner.refineHypothesis(ceQuery);
	            timer.stop();
	            options.stats.timeOfLearner += timer.getTimeElapsed();
	        }
	        options.log.println("Learning completed...");
	}
	private static void write2File(String path, String fileName, String content,boolean isAdd ) {
		try {
			File filePath = new File(path);
		    if (!filePath.exists()) {
		    	filePath.mkdirs();
		    }
			File file = new File(path+fileName);
            FileOutputStream fos = null;
            if(!file.exists()){
                file.createNewFile();
            }
            fos = new FileOutputStream(file,isAdd);

            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            osw.write(content);
            osw.close();
        } catch (IOException e) {
        	
        }
	}
	private static void recordDataForEveryState(String path, String fileName, ArrayList<Long> LOmega, ArrayList<Long> LSimple, int numOfCase) {
		StringBuilder content = new StringBuilder();
		for(Long element : LOmega)
		{
			content.append(element*1.0/numOfCase);
			content.append(",");
		}
		content.append("\r\n");
		for(Long element : LSimple)
		{
			content.append(element*1.0/numOfCase);
			content.append(",");
		}
		content.append("\r\n");
		
		write2File(path,fileName,content.toString(),true);
	}
	private static void recordData(String path, String fileName, Options options, int caseNum, ArrayList<Long> data) {
		StringBuilder content = new StringBuilder();
		content.append(caseNum);
		content.append(",");
		content.append(options.stats.numOfLetters);
		data.set(0, data.get(0)+(options.stats.numOfLetters));
		content.append(",");
		content.append(options.stats.numOfStatesInTraget);
		data.set(1, data.get(1)+(options.stats.numOfStatesInTraget));
		content.append(",");
		content.append(options.stats.numOfTransInTraget);
		data.set(2, data.get(2)+(options.stats.numOfTransInTraget));
		content.append(",");
		content.append(options.stats.numOfStatesInHypothesis);
		data.set(3, data.get(3)+(options.stats.numOfStatesInHypothesis));
		content.append(",");
		content.append(options.stats.numOfTransInHypothesis);
		data.set(4, data.get(4)+(options.stats.numOfTransInHypothesis));
		content.append(",");
		content.append(options.stats.numOfMembershipQuery);
		data.set(5, data.get(5)+(options.stats.numOfMembershipQuery));
		content.append(",");
		content.append(options.stats.numOfEquivalenceQuery);
		data.set(6, data.get(6)+(options.stats.numOfEquivalenceQuery));
		content.append(",");
		content.append(options.stats.timeOfMembershipQuery);
		data.set(7, data.get(7)+(options.stats.timeOfMembershipQuery));
		content.append(",");
		content.append(options.stats.timeOfEquivalenceQuery);
		data.set(8, data.get(8)+(options.stats.timeOfEquivalenceQuery));
		content.append(",");
		content.append(options.stats.timeOfLastEquivalenceQuery);
		data.set(9, data.get(9)+(options.stats.timeOfLastEquivalenceQuery));
		content.append(",");
		content.append(options.stats.timeOfTranslator);
		data.set(10, data.get(10)+(options.stats.timeOfTranslator));
		content.append(",");
		content.append(options.stats.timeOfLearner);
		data.set(11, data.get(11)+(options.stats.timeOfLearner));
		content.append(",");
		content.append(options.stats.timeInTotal);
		data.set(12, data.get(12)+(options.stats.timeInTotal));
		content.append(",");
		content.append(options.stats.timeOfLearnerLeading);
		data.set(13, data.get(13)+(options.stats.timeOfLearnerLeading));
		content.append("\r\n");
		
		write2File(path,fileName,content.toString(),true);
	}
	private static void removeDir(File dir) {
		if(!dir.exists()) return;
		File[] files=dir.listFiles();
		for(File file:files){
			if(file.isDirectory()){
				removeDir(file);
			}else{
				file.delete();
			}
		}
	}
}
