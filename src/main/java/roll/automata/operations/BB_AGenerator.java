package roll.automata.operations;

import java.util.Random;

import roll.automata.BB_A;
import roll.automata.StateNFA;
import roll.words.Alphabet;

public class BB_AGenerator {
	
	public static BB_A getBB_A()
	{
		int numState = 5;
		int numLetter = 3;
		Alphabet alphabet = new Alphabet();
	    char[] letters = { 'a', 'b', 'c', 'd', 'e' };

	    for (int i = 0; i < numLetter && i < letters.length; i++) {
	        alphabet.addLetter(letters[i]);
	    }
	    BB_A result = new BB_A(alphabet);
	    for(int i = 0; i < numState; i ++) {
            result.createState();
        }
	    result.setInitial(0);
	    result.getState(0).addTransition(2, 1);
	    result.getState(0).addTransition(1, 3);
	    result.getState(0).addTransition(0, 1);
	    result.getState(1).addTransition(2, 1);
	    result.getState(1).addTransition(1, 1);
	    result.getState(1).addTransition(0, 2);
	    result.getState(2).addTransition(2, 1);
	    result.getState(2).addTransition(1, 0);
	    result.getState(2).addTransition(0, 0);
	    result.getState(3).addTransition(2, 4);
	    result.getState(3).addTransition(1, 3);
	    result.getState(3).addTransition(0, 4);
	    result.getState(4).addTransition(2, 4);
	    result.getState(4).addTransition(1, 4);
	    result.getState(4).addTransition(0, 4);
	    result.setFinal(3);
	    return result;
	    
//	    result.getState(0).addTransition(4, 12);
//	    result.getState(0).addTransition(3, 6);
//	    result.getState(0).addTransition(2, 8);
//	    result.getState(0).addTransition(1, 0);
//	    result.getState(0).addTransition(0, 7);
//	    result.getState(1).addTransition(4, 8);
//	    result.getState(1).addTransition(3, 7);
//	    result.getState(1).addTransition(2, 3);
//	    result.getState(1).addTransition(1, 11);
//	    result.getState(1).addTransition(0, 6);
//	    result.getState(2).addTransition(4, 8);
//	    result.getState(2).addTransition(3, 8);
//	    result.getState(2).addTransition(2, 11);
//	    result.getState(2).addTransition(1, 11);
//	    result.getState(2).addTransition(0, 12);
//	    result.getState(3).addTransition(4, 7);
//	    result.getState(3).addTransition(3, 11);
//	    result.getState(3).addTransition(2, 2);
//	    result.getState(3).addTransition(1, 7);
//	    result.getState(3).addTransition(0, 11);
//	    result.getState(4).addTransition(4, 4);
//	    result.getState(4).addTransition(3, 4);
//	    result.getState(4).addTransition(2, 4);
//	    result.getState(4).addTransition(1, 9);
//	    result.getState(4).addTransition(0, 7);
//	    result.getState(5).addTransition(4, 11);
//	    result.getState(5).addTransition(3, 11);
//	    result.getState(5).addTransition(2, 10);
//	    result.getState(5).addTransition(1, 5);
//	    result.getState(5).addTransition(0, 5);
//	    result.getState(6).addTransition(4, 9);
//	    result.getState(6).addTransition(3, 6);
//	    result.getState(6).addTransition(2, 3);
//	    result.getState(6).addTransition(1, 4);
//	    result.getState(6).addTransition(0, 5);
//	    result.getState(7).addTransition(4, 6);
//	    result.getState(7).addTransition(3, 9);
//	    result.getState(7).addTransition(2, 7);
//	    result.getState(7).addTransition(1, 9);
//	    result.getState(7).addTransition(0, 9);
//	    result.getState(8).addTransition(4, 0);
//	    result.getState(8).addTransition(3, 8);
//	    result.getState(8).addTransition(2, 7);
//	    result.getState(8).addTransition(1, 1);
//	    result.getState(8).addTransition(0, 4);
//	    result.getState(9).addTransition(4, 2);
//	    result.getState(9).addTransition(3, 9);
//	    result.getState(9).addTransition(2, 9);
//	    result.getState(9).addTransition(1, 7);
//	    result.getState(9).addTransition(0, 1);
//	    result.getState(10).addTransition(4, 10);
//	    result.getState(10).addTransition(3, 8);
//	    result.getState(10).addTransition(2, 12);
//	    result.getState(10).addTransition(1, 10);
//	    result.getState(10).addTransition(0, 5);
//	    result.getState(11).addTransition(4, 11);
//	    result.getState(11).addTransition(3, 11);
//	    result.getState(11).addTransition(2, 11);
//	    result.getState(11).addTransition(1, 11);
//	    result.getState(11).addTransition(0, 11);
//	    result.getState(12).addTransition(4, 12);
//	    result.getState(12).addTransition(3, 12);
//	    result.getState(12).addTransition(2, 12);
//	    result.getState(12).addTransition(1, 12);
//	    result.getState(12).addTransition(0, 12);
	    
	}
	public static BB_A getRandomBB_A(int numState,int numLetter) {
		if (numLetter > 5) {
	        throw new UnsupportedOperationException("only allow a,b,c,d,e letters in generated BB_A");
	    }
		if(numState <=0) {
			throw new UnsupportedOperationException("Invalid state size in generated BB_A");
		}
	    Alphabet alphabet = new Alphabet();
	    char[] letters = { 'a', 'b', 'c', 'd', 'e' };

	    for (int i = 0; i < numLetter && i < letters.length; i++) {
	        alphabet.addLetter(letters[i]);
	    }
        final int apSize = alphabet.getLetterSize();
        
		assert(numState >=1):"it should have more than one state";
        BB_A result = new BB_A(alphabet);
    	Random r = new Random(System.currentTimeMillis());
    	
    	for(int i = 0; i < numState; i ++) {
            result.createState();
        }
    	if(numState == 1)
    	{
    		result.setInitial(0);
    		result.setFinal(0);
    		StateNFA state = result.getState(0);
    		for(int k=0 ; k < apSize; k++){
                state.addTransition(k, 0);
            }
    		return result;
    	}
        result.setInitial(0);
        StateNFA trap = result.getState(numState-1);
        StateNFA acc = result.getState(numState-2);
        result.setFinal(acc.getId());
        
        // add self loops for those transitions
        for(int i = 0; i < numState; i ++) {
            StateNFA state = result.getState(i);
            for(int k=0 ; k < apSize; k++){
                state.addTransition(k, i);
            }
        }
        
        ///point to trap state
        for(int i=0;i<numState-1;i++) {
        	StateNFA state = result.getState(i);
        	int numTransToTrap = r.nextInt(apSize);
        	for(int j=0;j<numTransToTrap;j++) {
        		int letter = r.nextInt(apSize);
        		state.addTransition(letter, trap.getId());
        	}
        }

        final int preNumState = numState-2;
        if(apSize >0 && preNumState >0) {
            int numTrans = r.nextInt(preNumState * apSize);
            // transitions
            for(int k=0 ; k < apSize; k++){
                for(int n = 0; n < numTrans; n++ ){
                    int i=r.nextInt(preNumState);
                    int j=r.nextInt(preNumState);
                    result.getState(i).addTransition(k, j);
                }
            }
            ///point to final state 
            int numF = r.nextInt(preNumState);
            numF = numF > 0 ? numF : 1;
            for(int n = 0; n < numF ; n ++) {
                int f = r.nextInt(numF);
                int letter = r.nextInt(apSize);
                result.getState(f).addTransition(letter, acc.getId());
            }

        }
        return result;
    }
	

}
