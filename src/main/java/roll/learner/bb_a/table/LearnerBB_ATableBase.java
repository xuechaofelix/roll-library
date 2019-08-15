package roll.learner.bb_a.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import roll.automata.BB_A;
import roll.learner.bb_a.LearnerBB_A;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.ExprValue;
import roll.table.ExprValueWordPair;
import roll.table.HashableValue;
import roll.table.ObservationRow;
import roll.table.ObservationTableAbstract;
import roll.words.Alphabet;
import roll.words.Word;

public abstract class LearnerBB_ATableBase extends LearnerBB_A{

    protected ObservationTableAbstract observationTable;
    
	public LearnerBB_ATableBase(Options options, Alphabet alphabet, MembershipOracle<HashableValue> membershipOracle) {
		super(options, alphabet, membershipOracle);
		this.observationTable = this.getTableInstance();
		
	}
	
    protected ObservationTableAbstract getTableInstance() {
        return new ObservationTableBB_ALOmega();
    }

	@Override
	protected ExprValue getCounterExampleWord(Query<HashableValue> query) {
		assert query != null;
        Word left = query.getPrefix();
        Word right = query.getSuffix();
        assert left != null && right != null;
        return new ExprValueWordPair(left, right);
	}


	@Override
	public void refineHypothesis(Query<HashableValue> query) {
		ExprValue conterexample =  getCounterExampleWord(query);
		/// add all the suffix into the table
		this.addSuffixToTable(conterexample);
        this.makeTableClosed();
	}
	
	@Override
	protected void initialize() {
		/// initialize the table 
		initializeTable(observationTable);
        makeTableClosed();
	}
	
	/**
	 *  add exact word to column
	 * @param state start state
	 * @param word 
	 */
	protected void addSuffixToTable(int state, ExprValue word)
	{
		Word left = word.getLeft();
		Word right = word.getRight();
		int sizeOfColumn = this.observationTable.getColumns().size();
		int sizeOfNew = 0;
		int currentState = state;
		HashableValue preResult = this.processMembershipQuery(this.observationTable.getUpperTable().get(state).getWord().concat(left), right);
		if(!this.observationTable.getColumns().contains(word)) {
			this.observationTable.addColumn(word);
			sizeOfNew++;
		}
		options.log.println("currentState-->("+currentState+") Word-->("+
				this.observationTable.getUpperTable().get(state).getWord().concat(left)+", "+right+") Result-->"+ preResult);
		boolean flag = true;
		///1. suffix of left plus right
		for(int i=1, size = left.length();i<size;i++)
		{
			Word columnLeft = left.getSuffix(i);
			currentState = this.hypothesis.getSuccessor(currentState, left.getLetter(i-1));
			HashableValue currentResult = 
					this.processMembershipQuery(this.observationTable.getUpperTable().get(currentState).getWord().concat(columnLeft), right);
			options.log.println("currentState-->("+currentState+") Word-->("+
					this.observationTable.getUpperTable().get(currentState).getWord().concat(columnLeft)+", "+right+") Result-->"+ currentResult);
			if(flag) {
				ExprValue column = new ExprValueWordPair(columnLeft, right);
				if(!this.observationTable.getColumns().contains(column)) {
					this.observationTable.addColumn(column);
					sizeOfNew++;
				}
			}
			if(! currentResult.equals(preResult)) {
//				ExprValue column = new ExprValueWordPair(columnLeft, right);
//				sizeOfNew++;
//				if(!this.observationTable.getColumns().contains(column)) {
//					this.observationTable.addColumn(column);
//				}
//				break;
				flag = false;
			}
		}
		Word currentRotation = right;
		///2. all the rotation of the right
		if(flag) {
			for(int letter: right)
			{
				currentRotation = currentRotation.getSuffix(1).append(letter);
				currentState = this.hypothesis.getSuccessor(currentState, letter);
				HashableValue currentResult = 
						this.processMembershipQuery(this.observationTable.getUpperTable().get(currentState).getWord(), currentRotation);
				options.log.println("currentState-->("+currentState+") Word-->("+
						this.observationTable.getUpperTable().get(currentState).getWord()+", "+right+") Result-->"+ currentResult);
				if(flag) {
					ExprValue column = new ExprValueWordPair(this.alphabet.getEmptyWord(),currentRotation);
					if(!this.observationTable.getColumns().contains(column)) {
						this.observationTable.addColumn(column);
						sizeOfNew ++;
					}
				}
				if(! currentResult.equals(preResult)) {
//					ExprValue column = new ExprValueWordPair(this.alphabet.getEmptyWord(),currentRotation);
//					sizeOfNew ++;
//					if(!this.observationTable.getColumns().contains(column)) {
//						this.observationTable.addColumn(column);
//					}
//					break;
					flag = false;
				}
			}
		}
		
		//assert(sizeOfNew ==1 && this.observationTable.getColumns().size() == sizeOfColumn+sizeOfNew):
		//									"LearnerBB_ATable:addSuffixToTable: problem in column";
		///add value for new suffix
		this.processMembershipQueries(observationTable, this.observationTable.getLowerTable(), sizeOfColumn, sizeOfNew);
		this.processMembershipQueries(observationTable, this.observationTable.getUpperTable(), sizeOfColumn, sizeOfNew);
	}
	/**
	 * add all the suffix of the word to table
	 * @param word 
	 */
	protected void addSuffixToTable(ExprValue word)
	{
		Word left = word.getLeft();
		Word right = word.getRight();
		int sizeOfColumn = this.observationTable.getColumns().size();
		int sizeOfNew = 0;
		///1. suffix of left plus right
		for(int i=0, size = left.length();i<size;i++)
		{
			ExprValue column = new ExprValueWordPair(left.getSuffix(i),right);
			if(!this.observationTable.getColumns().contains(column)) {
				sizeOfNew++;
				this.observationTable.addColumn(column);
			}
		}
		Word currentRotation = right;
		///2. all the rotation of the right
		for(int letter: right)
		{
			currentRotation = currentRotation.getSuffix(1).append(letter);
			ExprValue column = new ExprValueWordPair(this.alphabet.getEmptyWord(),currentRotation);
			if(!this.observationTable.getColumns().contains(column)) {
				sizeOfNew ++;
				this.observationTable.addColumn(column);
			}
		}
		assert(this.observationTable.getColumns().size() == sizeOfColumn+sizeOfNew):
												"LearnerBB_ATable:addSuffixToTable: problem in column";
		///add value for new suffix
		this.processMembershipQueries(observationTable, this.observationTable.getLowerTable(), sizeOfColumn, sizeOfNew);
		this.processMembershipQueries(observationTable, this.observationTable.getUpperTable(), sizeOfColumn, sizeOfNew);
	}
	
	/**
	 * Get successor from state via letter in table
	 * @param state
	 * @param letter
	 * @return successor state
	 */
	protected int getSuccessorRow(int state, int letter) {
        ObservationRow stateRow = observationTable.getUpperTable().get(state);
        Word succWord = stateRow.getWord().append(letter);
        int succ = observationTable.getUpperTableRowIndex(succWord);
        return succ;
    }
	protected Conflict markAndDetect(BB_A hypothesis){
		HashMap<Integer, Word> suffixWord = new HashMap<>();//for every state,record an \Omega word from it
		List<ObservationRow> S = this.observationTable.getUpperTable();
		List<ExprValue> E = this.observationTable.getColumns();
		int[] marks = new int[hypothesis.getStateSize()];//-1: indefinite 0:reject 1:accept

		//Initially, put all state as indefinite
		for(int i=0;i<S.size();i++)
		{
			marks[i] = -1;
		}
		for(ObservationRow o : S)
		{
			//for every suffix, get the Inf(a)
			for (int i = 0, size = E.size(); i < size; i++) 
			{
				ExprValue e = E.get(i);
				boolean isAccept = o.getValues().get(i).isAccepting();
				assert(e.getLeft() instanceof Word):"E is not the Word";
				assert(e.getRight() instanceof Word):"E is not the Word";
				Word left = (Word)(e.getLeft());
				Word right = (Word)(e.getRight());
				//loop in right part is the Inf(a)
				HashMap<Integer, Word> tmpSuffixWord = new HashMap<>();
				ArrayList<Integer> infAlpha = this.hypothesis.getInfState(o.getWord().concat(left), right, tmpSuffixWord);
				for(int s: infAlpha){
					int markInfA = marks[s];
					if(markInfA!= -1 && markInfA!= (isAccept?1:0))
					{
						//this conflict should not occur.
						//conflict
//						assert(false):"LearnerBB_ATable:201: problem occur";
						if(isAccept) {
							if(!tmpSuffixWord.get(s).equals(suffixWord.get(s)))
								return new Conflict(this.observationTable.getUpperTable().get(s), tmpSuffixWord.get(s), suffixWord.get(s),this.options);
						}
						else {
							if(!tmpSuffixWord.get(s).equals(suffixWord.get(s)))
								return new Conflict(this.observationTable.getUpperTable().get(s), suffixWord.get(s), tmpSuffixWord.get(s),this.options);
						}
					}
					marks[s] = isAccept?1:0;
					suffixWord.put(s, tmpSuffixWord.get(s));
//					this.options.log.println(o.getWord()+" "+e +" "+markInfA);
//					for(int j=0;j<marks.length;j++)
//						this.options.log.println(marks[j]+"");
//					System.out.println(o.getWord()+" "+ e + " "+isAccept);
//					
//	            	System.out.println(suffixWord);	
				}
			}
		}
//		//Compute MSCC and mark state
//		for(int i=0;i<marks.length;i++)
//			System.out.print(marks[i]);
//		System.out.println();
		MSCCMarkOrDetect msccMarkOrDetect = new MSCCMarkOrDetect(hypothesis,marks,suffixWord);
		if(!msccMarkOrDetect.markOrdetect()) {
			/// mark failed, get the conflict
			int t = msccMarkOrDetect.pair.getLeft();
			int s = msccMarkOrDetect.pair.getRight();
			Word z = getWord(s,t);// get the word from state s to state t
	    	Word w = getWord(t,s);
//        	options.log.println(this.observationTable.toString());
//        	options.log.println("current hypothesis:\n"+this.hypothesis);
//        	options.log.println("s: "+this.observationTable.getUpperTable().get(s).getWord());
//        	options.log.println("t: "+this.observationTable.getUpperTable().get(t).getWord());
//        	options.log.println("info: s("+s+") t("+t+") z("+ z+") w("+w+") x("+ suffixWord.get(s)+") y("+suffixWord.get(t)+")");
	    	/// check whether s(zw)^w is accepting
//	    	Query<HashableValue> query = new QuerySimple<HashableValue>(this.observationTable.getUpperTable().get(t).getWord(), w.concat(z));
//	        HashableValue answer = membershipOracle.answerMembershipQuery(query);
//	        options.log.println("(t, wz) is "+answer.isAccepting());
	        Query<HashableValue> query = new QuerySimple<HashableValue>(this.observationTable.getUpperTable().get(s).getWord(), z.concat(w));
	        HashableValue answer = membershipOracle.answerMembershipQuery(query);
	        
	        if(answer.isAccepting())
	        {
	        	///check whether sz(wz) is same as t(wz),,,,,////sz(wz) is accepting, so check whether t(wz) is accepting
//	        	Query<HashableValue> query_sz = 
//	        			new QuerySimple<HashableValue>(this.observationTable.getUpperTable().get(s).getWord().concat(z), w.concat(z));
//		        HashableValue answer_sz = membershipOracle.answerMembershipQuery(query_sz);
		        Query<HashableValue> query_t = 
	        			new QuerySimple<HashableValue>(this.observationTable.getUpperTable().get(t).getWord(), w.concat(z));
		        HashableValue answer_t = membershipOracle.answerMembershipQuery(query_t);
		        if(answer_t.isAccepting()) {
		        	if(!w.concat(z).equals(suffixWord.get(s)))
		        		return new Conflict(this.observationTable.getUpperTable().get(t),w.concat(z),suffixWord.get(t),this.options);
		        }
		        else {
		        	this.addSuffixToTable(new ExprValueWordPair(this.alphabet.getEmptyWord(),w.concat(z)));
		        	this.makeTableClosed();
		        	return null;
		        }
	        }
	        else
	        {
	        	if(!z.concat(w).equals(suffixWord.get(s)))
	        		return new Conflict(this.observationTable.getUpperTable().get(s),suffixWord.get(s),z.concat(w),this.options);
	        }
		}
		/// if mark succeed, set corresponding final state and return null
		for(int rowNr = 0; rowNr < hypothesis.getStateSize(); rowNr ++)
        {
        	if(marks[rowNr] == 1) {
                hypothesis.setFinal(rowNr);
            }
        }
		return null;
		
	}
	/**
	 * sampling a word from state start to end
	 * @param start
	 * @param end
	 * @return Word
	 */
	private Word getWord(int start, int end)
    {
    	Word  result = hypothesis.getAlphabet().getEmptyWord();
    	int[] isVisit = new int[hypothesis.getStateSize()];
    	result = getWord(start,end,isVisit,result);
    	if(result != null)
    	{
    		return result;
    	}
    	return null;
    }
    private Word getWord(int start, int end, int[] isVisit, Word word)
    {
    	isVisit[start] = 1;
    	Word  result;
    	for(int i=0;i<this.hypothesis.getAlphabetSize();i++)
    	{
    		int nextState = this.hypothesis.getSuccessor(start, i);
    		if(isVisit[nextState] == 1) continue;
    		result = word.append(i);
    		if(nextState == end) return result;
    		result = getWord(nextState, end, isVisit,result);
    		if(result != null)
    		{
    			return result;
    		}
    	}
    	return null;
    }
    protected void makeTableClosed() {
        ObservationRow lowerRow = observationTable.getUnclosedLowerRow();

        while(lowerRow != null) {
            // 1. move to upper table
            observationTable.moveRowFromLowerToUpper(lowerRow);
            // 2. add one letter to lower table
            List<ObservationRow> newLowerRows = new ArrayList<>();
            for(int letterNr = 0; letterNr < alphabet.getLetterSize(); letterNr ++) {
                Word newWord = lowerRow.getWord().append(letterNr);
                ObservationRow row = observationTable.getTableRow(newWord); // already existing
                if(row != null) continue;
                ObservationRow newRow = observationTable.addLowerRow(newWord);
                newLowerRows.add(newRow);
            }
            // 3. process membership queries
            processMembershipQueries(observationTable, newLowerRows, 0, observationTable.getColumns().size());
            lowerRow = observationTable.getUnclosedLowerRow();
        }
        constructHypothesis();
    }

	@Override
	public String toHTML() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String toString() {
		return this.observationTable.toString();
	}


}
