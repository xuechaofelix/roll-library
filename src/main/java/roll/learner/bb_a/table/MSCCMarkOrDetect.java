package roll.learner.bb_a.table;

import java.util.HashMap;
import java.util.Stack;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import roll.automata.BB_A;
import roll.util.Pair;
import roll.util.sets.ISet;
import roll.util.sets.UtilISet;
import roll.words.Word;

public class MSCCMarkOrDetect {
	 	private final BB_A bb_a;
	    private int index = 0;
	    private Stack<Integer> SCCs = new Stack<>(); /// stack for record SCC
	    private TIntIntMap vIndex = new TIntIntHashMap(); // time stamp
	    private TIntIntMap vLowlink = new TIntIntHashMap(); // lowest farther
	    private final int[]  marks; // marks for every state
	    private final HashMap<Integer, Word> suffix; // suffix for state
	    Pair<Integer, Integer> pair; // conflict state if mark failed
	    
	    public MSCCMarkOrDetect(BB_A bb_a, int[] marks, HashMap<Integer, Word> suffix) {
	        this.bb_a = bb_a;
	        this.marks = marks;
	        this.suffix = suffix;
	    }
	    
	    public boolean markOrdetect() {
	        // only check the states reachable by the automaton
	        final int s = bb_a.getInitialState();
	        if(!vIndex.containsKey(s)){
	            if(tarjan(s))
	               return false;
	        }

	        return true;
	    }

	    // terminate on the first accepting loop
	    boolean tarjan(int v) {
	        vIndex.put(v, index);
	        vLowlink.put(v, index);
	        index++;
	        SCCs.push(v);

	        for(int c = 0; c < bb_a.getAlphabetSize(); c ++) {
	            int w = bb_a.getSuccessor(v, c);
	            // to check whether there is a loop
	            if(!vIndex.containsKey(w)){
	                if(tarjan(w)) return true;
	                vLowlink.put(v, Math.min(vLowlink.get(v), vLowlink.get(w)));
	            }else if(SCCs.contains(w)){
	                vLowlink.put(v, Math.min(vLowlink.get(v), vIndex.get(w)));
	            }
	        }
	        if(vLowlink.get(v) == vIndex.get(v)){
	            int num = 0;
	            int s1 = -1, s2 = -2;
	            ISet scc = UtilISet.newISet();
	            int mark = -1;
	            while(! SCCs.empty()){
	                int t = SCCs.pop();
	                /// rejecting state in SCC and it was marked in previous searching table stage
	                if(marks[t] == 0 && suffix.containsKey(t)){
	                	s1 = t;
		                mark = marks[t];
	                }
	              /// accepting state in SCC and it was marked in previous searching table stage
	                if(marks[t] == 1 && suffix.containsKey(t)) {
	                    s2 = t;
		                mark = marks[t];
	                }
	                // found conflict
	                if(s1 >= 0 && s2 >= 0) {
	                    pair = new Pair<>(s1, s2);
	                    return true;
	                }
	                scc.set(t);
	                ++ num;
	                if(t == v)
	                    break;
	            }
	            if(num > 1) {
	                //mark all other states, including those have been marked
	                for(int s : scc) {
	                    marks[s] = mark;
	                }
	            }
	        }
	        return false;
	    }
	    
	    
}
