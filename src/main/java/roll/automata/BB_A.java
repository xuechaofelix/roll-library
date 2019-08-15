package roll.automata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import roll.words.Alphabet;
import roll.words.Word;

public class BB_A extends DFA{

	public BB_A(Alphabet alphabet) {
		super(alphabet);
		this.accept = new AcceptBB_A(this);
	}

	@Override
	public AutType getAccType() {
		return AutType.BB_A;
	}
	 // specialized for DFA
    public int getSuccessor(Word word) {
        return getSuccessor(getInitialState(), word);
    }
    
    public int getSuccessor(int state, int letter) {
        return getState(state).getSuccessor(letter);
    }
    
    public int getSuccessor(int state, Word word) {
        int index = 0;
        int currState = state;
        while(index < word.length()) {          
            currState = getSuccessor(currState, word.getLetter(index));
            ++ index;
        }
        return currState;
    }
    private class ProduState{
		private final int first;
		private final int second;
		
		public ProduState(int first, int second) {
			this.first = first;
			this.second = second;
		}
		public int getFirstState() { return first;}
		public int getSecondState() { return second;}
		@Override
		public int hashCode() {
			return first + second;
		}
		@Override
		public boolean equals(Object obj) {
			if(! (obj instanceof ProduState)) return false;
			ProduState Obj = (ProduState)obj;
			return this.first == Obj.getFirstState() && this.second == Obj.getSecondState();
		}
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "("+this.first+", "+this.second+")";
		}
		
	}
    public ArrayList<Integer> getInfState(Word stem, Word loop, HashMap<Integer, Word> suffix) {
    	BB_A word = this.word2BB_A(stem, loop, getAlphabet());
    	//System.out.println(word.toDot());
    	ProduState initState = new ProduState(word.getInitialState(),this.getInitialState());
    	HashMap<ProduState, Integer> vIndex = new HashMap<>();
    	HashMap<ProduState, Integer> vLowlink = new HashMap<>();
    	ArrayList<ProduState> States = new ArrayList<>();
    	Stack<ProduState> SCCs = new Stack<>();
    	this.index = 0;
    	ArrayList<Integer> wordResult = new ArrayList<>();
    	ArrayList<Integer> result = new ArrayList<>();
    	if(tarjan(States,initState, vIndex, vLowlink, SCCs, word, this, wordResult, result)){
    		wordResult.add(wordResult.get(0));
        	Word suffixWordRotation = this.getWordFromState(word, wordResult);
        	for(int s: result) {
        		suffix.put(s, suffixWordRotation);
        		suffixWordRotation = suffixWordRotation.getSuffix(1).append(suffixWordRotation.getFirstLetter());
        	}
    	}
    	else {
    		return null;
    	}
    	return result;
    }
    private Word getWordFromState(BB_A automaton, ArrayList<Integer> states){
    	Word result = automaton.alphabet.getEmptyWord();
    	for(int i=0;i<states.size()-1;i++){
    		int currentState = states.get(i);
    		int nextState = states.get(1+i);
    		for(int letter = 0;letter < automaton.alphabet.getLetterSize();letter ++){
        		if(automaton.getSuccessor(currentState, letter) == nextState){
        			result = result.append(letter);
        			break;
        		}
        	}
    	}
    	return result;
    }
    private int index = 0;
    private boolean tarjan(ArrayList<ProduState> States, ProduState v, HashMap<ProduState, Integer> vIndex, 
    			HashMap<ProduState, Integer> vLowlink, Stack<ProduState> SCCs, BB_A word, 
    			BB_A automaton,ArrayList<Integer> wordStates,ArrayList<Integer> automatonStates) {
        vIndex.put(v, index);
        vLowlink.put(v, index);
        index++;
        SCCs.push(v);
        States.add(v);
        
        for(int c = 0; c < word.getAlphabetSize(); c ++) {
            int firstState = word.getSuccessor(v.getFirstState(), c);
            if(firstState == -1) continue;
            int secondState = automaton.getSuccessor(v.getSecondState(), c);
            ProduState w = new ProduState(firstState,secondState);
            int indexOfw = States.indexOf(w);
            if(indexOfw != -1) {
            	w = States.get(indexOfw);
            }
            // to check whether there is a loop
            if(!vIndex.containsKey(w)){
                if(tarjan(States, w, vIndex, vLowlink, SCCs, word, automaton, wordStates, automatonStates)) return true;
                vLowlink.put(v, Math.min(vLowlink.get(v), vLowlink.get(w)));
            }else if(SCCs.contains(w)){
                vLowlink.put(v, Math.min(vLowlink.get(v), vIndex.get(w)));
            }
        }
        if(vLowlink.get(v).equals(vIndex.get(v))){
            boolean isAccepting = true;
            ArrayList<ProduState> scc = new ArrayList<>();
            while(! SCCs.empty()){
            	ProduState t = SCCs.pop();
            	if(isAccepting ) {
            		if(word.isFinal(t.getFirstState())) {
            			scc.add(t);
            		}
            		else {
            			isAccepting = false;
            		}
            	}
                if(t == v)
                    break;
            }
            if(isAccepting) {
            	for(int i=scc.size()-1;i>=0;i--){
            		ProduState t = scc.get(i);
                	wordStates.add(t.getFirstState());
                	automatonStates.add(t.getSecondState());
            	}
            	return true;
            }
            
        }
        return false;
    }
  
    private BB_A word2BB_A(Word stem, Word loop, Alphabet alphabet) {
    	BB_A result = new BB_A(alphabet);
//    	StateNFA trap = result.createState();
//    	for(int letter=0, size = alphabet.getLetterSize();letter<size;letter++)
//    	{
//    		trap.addTransition(letter, trap.getId());
//    	}
    	StateNFA init = result.createState();
    	result.setInitial(init.getId());
    	StateNFA preState = init;
    	for(int i=0;i<stem.length();i++)
    	{
    		int letter = stem.getLetter(i);
//    		for(int letter_=0, size = alphabet.getLetterSize();letter_<size;letter_++)
//    		{
//    			if(letter_ != letter)
//    			{
////    				preState.addTransition(letter_, 0);
//    			}
//    		}
    		StateNFA state = result.createState();
			preState.addTransition(letter, state.getId());
			preState = state;
    	}
    	StateNFA loopState = preState;
    	Word currentRotation = loop;
    	for(int i=0;i<loop.length()-1;i++)
    	{
    		int letter = loop.getLetter(i);
    		currentRotation = currentRotation.getSuffix(1).append(letter);
//    		for(int letter_=0, size = alphabet.getLetterSize();letter_<size;letter_++)
//    		{
//    			if(letter_ != letter)
//    			{
//    				preState.addTransition(letter_, 0);
//    			}
//    		}
//    		
    		StateNFA state = result.createState();
			preState.addTransition(letter, state.getId());
			result.setFinal(state.getId());
			preState = state;
    	}
		int letter = loop.getLastLetter();
//		for(int letter_=0, size = alphabet.getLetterSize();letter_<size;letter_++)
//		{
//			if(letter_ != letter)
//			{
//				preState.addTransition(letter_, 0);
//			}
//		}
		preState.addTransition(letter, loopState.getId());
		result.setFinal(loopState.getId());
		return result;
    }
    public NBA ToNBA(){

        NBA result = new NBA(alphabet);

        for (int i = 0; i < this.getStateSize(); i++) {
            result.createState();
        }
        //set initial state
        result.setInitial(this.getInitialState());
        
        // set final state
        for(int state : this.getFinalStates()){
        	result.setFinal(state);
        }
        
        //transition
        for(int s = 0, stateSize = this.getStateSize();s< stateSize;s++) {
        	for(int letter=0, alphabetSize = this.alphabet.getLetterSize(); letter < alphabetSize;letter++){
        		result.getState(s).addTransition(letter, this.getState(s).getSuccessor(letter));
        	}
        }
    
        return result;
    }
}
