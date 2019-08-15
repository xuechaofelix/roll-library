package roll.learner.bb_a.table;

import datastructure.HashSet;
import roll.learner.bb_a.lomega.TestCases;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.ExprValue;
import roll.table.ExprValueWordPair;
import roll.table.HashableValue;
import roll.table.ObservationRow;
import roll.words.Alphabet;
import roll.words.Word;

public class Conflict {
	
	private ObservationRow s; 
	private Word u;////s(u)^w is accepting
	private Word v;////s(v)^w is rejecting
	Options options;
	private int stateSize;
	private int lengthOfWord;
	
	public Conflict(ObservationRow s, Word u, Word v, Options options){
		this.s = s;
		this.u = u;
		this.v = v;
		this.options = options;
		
	}
	public ObservationRow getS(){
		return s;
	}
	public Word getU(){
		return u;
	}
	public Word getV(){
		return v;
	}
	public int getStateSize() {
		return this.stateSize;
	}
	public int getLengthOfWord() {
		return this.lengthOfWord;
	}
	public ExprValue getResolutionNew(Alphabet alphabet, MembershipOracle<HashableValue> membershipOracle) {
		int sizeOfState=1;
//		System.out.println("Get the conflict : s("+ s.getWord()+")----> u("+u+")----->v(" + v+")");
		options.log.println("Get the conflict : s("+ s.getWord()+")----> u("+u+")----->v(" + v+")");
		while(true)
		{
			// if number of State is bigger than target's, throw an exception
			if(TestCases.numOfTargetState<sizeOfState) {
				throw new UnsupportedOperationException("search too many");
			}
			Word u_n = alphabet.getEmptyWord();
			for(int i=0;i<sizeOfState;i++) {
				u_n = u_n.concat(u);
			}
			Query<HashableValue> s_u_n_v_query = new QuerySimple<HashableValue>(s.getWord().concat(u_n), v);
	        HashableValue s_u_n_v_result = membershipOracle.answerMembershipQuery(s_u_n_v_query);
//	        options.log.println("s u^n v^w--->"+ s.getWord()+" "+u_n+ " "+ v + " ^w :" + s_u_n_v_result);
	        if(s_u_n_v_result.isAccepting()) {
	        	this.stateSize = sizeOfState; 
	        	this.lengthOfWord = u_n.length()+v.length();
	        	this.options.stats.timeOfLearnerLeading+=this.lengthOfWord;
	        	return new ExprValueWordPair(u_n,v);
	        }
	        else {
	        	Query<HashableValue> s_u_n_v_u_query = new QuerySimple<HashableValue>(s.getWord().concat(u_n).concat(v), u);
		        HashableValue s_u_n_v_u_result = membershipOracle.answerMembershipQuery(s_u_n_v_u_query);
//		        options.log.println("s u^n v u^w--->"+ s.getWord()+" "+u_n+" "+ v +" "+u+ "^w :" + s_u_n_v_u_result);
		        if(!s_u_n_v_u_result.isAccepting()) {
		        	this.stateSize = sizeOfState; 
		        	this.lengthOfWord = u_n.length()+v.length()+u.length();
		        	this.options.stats.timeOfLearnerLeading+=this.lengthOfWord;
		        	return new ExprValueWordPair(u_n.concat(v),u);
		        }
	        }
	        sizeOfState ++ ;
		}
	}
	///check whether sa !~ sva or sa !~ sua
	private ExprValue getResolutionWord(Word a, Word right, MembershipOracle<HashableValue> membershipOracle){
		///construct sa query
		Query<HashableValue> s_a_query = new QuerySimple<HashableValue>(s.getWord().concat(a), right);
        HashableValue s_a_result = membershipOracle.answerMembershipQuery(s_a_query);

//        options.log.println("s a--->"+ s.getWord()+" "+a+ " "+ right + " ^w :" + s_a_result);
		
        ///construct sva query
        Query<HashableValue> s_v_a_query = new QuerySimple<HashableValue>(s.getWord().concat(v).concat(a), right);
        HashableValue s_v_a_result = membershipOracle.answerMembershipQuery(s_v_a_query);
        
//        options.log.println("sva--->"+ s.getWord().concat(v).concat(a)+ " "+ right + " ^w :" + s_a_result);

        if(!s_a_result.equals(s_v_a_result)){
        	///return va
        	this.lengthOfWord = v.length()+a.length()+right.length();
        	this.options.stats.timeOfLearnerLeading+=this.lengthOfWord;
        	return new ExprValueWordPair(v.concat(a),right);
        }
        
        ///construct sua query
        Query<HashableValue> s_u_a_query = new QuerySimple<HashableValue>(s.getWord().concat(u).concat(a), right);
        HashableValue s_u_a_result = membershipOracle.answerMembershipQuery(s_u_a_query);
        
//        options.log.println("sua--->"+ s.getWord().concat(u).concat(a)+ " "+ right + " ^w :" + s_a_result);
        
        if(!s_a_result.equals(s_u_a_result)){
        	///return ua
        	this.lengthOfWord = u.length()+a.length()+right.length();
        	this.options.stats.timeOfLearnerLeading+=this.lengthOfWord;
        	return new ExprValueWordPair(u.concat(a),right);
        }
        return null;
	}
	public ExprValue getResolution(Alphabet alphabet, MembershipOracle<HashableValue> membershipOracle){
		int sizeOfState=0;
		///for every possible state size, because we can not know the size of target automaton
//		System.out.println("Get the conflict : s("+ s.getWord()+")----> u("+u+")----->v(" + v+")");
//		options.log.println("Get the conflict : s("+ s.getWord()+")----> u("+u+")----->v(" + v+")");
		HashSet<Word> prefix_u = new HashSet<>();
		HashSet<Word> prefix_v = new HashSet<>();
		ExprValue result_u = getResolutionWord(alphabet.getEmptyWord(),u,membershipOracle);
        if(result_u != null) {
        	return result_u;
        }
		prefix_u.add(alphabet.getEmptyWord());
        ExprValue result_v = getResolutionWord(alphabet.getEmptyWord(),v,membershipOracle);
        if(result_v != null) {
        	return result_v;
        }
        prefix_v.add(alphabet.getEmptyWord());
		while(true){
//			options.log.println("State size -->" + sizeOfState);
			if(TestCases.numOfTargetState<sizeOfState) {
				throw new UnsupportedOperationException("search too many");
			}
			boolean initTurn = true;// whether the letter append is same as initial turn
			Word a_v = alphabet.getEmptyWord();// v is the first letter
			Word a_u = alphabet.getEmptyWord(); // u is the first letter
			//options.log.println("a_v -->" + a_v+" a_u -->" + a_u);
			for(int i=0;i<sizeOfState;i = initTurn? i+1:i){
				if(initTurn){
					for(int j=0;j<sizeOfState;j++){
						////(v^n u^n)^n:v.....v u^w
//						for(int letter : v) {
//							a_v = a_v.append(letter);
							a_v = a_v.concat(v);
//							if(prefix_v.contains(a_v)){
//								continue;
//							}
							prefix_v.add(a_v);
							ExprValue result = getResolutionWord(a_v,u,membershipOracle);
					        if(result != null) {
					        	return result;
					        }
//						}
				        
				        ////(u^n v^n)^n: u.....u v^w
//						for(int letter : u){
//							a_u = a_u.append(letter);
					        a_u = a_u.concat(u);
//							if(prefix_u.contains(a_u)){
//								continue;
//							}
							prefix_u.add(a_u);
							result = getResolutionWord(a_u,v,membershipOracle);
					        if(result != null) {
					        	return result;
					        }
//						}
				        
				        
					}
					initTurn = false;
				}
				else{
					for(int j=0;j<sizeOfState;j++){
						////(v^n u^n)^n: v.....u v^w
//						for(int letter : u) {
//							a_v = a_v.append(letter);
							a_v = a_v.concat(u);
//							if(prefix_v.contains(a_v)){
//								continue;
//							}
							ExprValue result = getResolutionWord(a_v,v,membershipOracle);
					        if(result != null) {
					        	return result;
					        }
							prefix_v.add(a_v);
//						}
				        
				        ////(u^n v^n)^n: v.....v u^w
//						for(int letter : v) {
//							a_u = a_u.append(letter);
					        a_u = a_u.concat(v);
//							if(prefix_u.contains(a_u)){
//								continue;
//							}
							result = getResolutionWord(a_u,u,membershipOracle);
					        if(result != null) {
					        	return result;
					        }
							prefix_u.add(a_u);
//						}
				        
					}
					initTurn = true;
				}
			}
			//increase number of state
			sizeOfState++;
		}
		
	}
	
}
