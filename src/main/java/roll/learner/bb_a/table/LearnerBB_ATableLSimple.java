package roll.learner.bb_a.table;

import java.util.List;

import roll.automata.BB_A;
import roll.automata.StateNFA;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.table.ExprValue;
import roll.table.HashableValue;
import roll.table.ObservationRow;
import roll.words.Alphabet;

public class LearnerBB_ATableLSimple extends LearnerBB_ATableBase {
	
	public LearnerBB_ATableLSimple(Options options, Alphabet alphabet,
			MembershipOracle<HashableValue> membershipOracle) {
		super(options, alphabet, membershipOracle);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void constructHypothesis() {
		hypothesis = new BB_A(alphabet);

        List<ObservationRow> upperTable = observationTable.getUpperTable();
        ///create all states
        for(int rowNr = 0; rowNr < upperTable.size(); rowNr ++) {
            hypothesis.createState();
        }
        
        for(int rowNr = 0; rowNr < upperTable.size(); rowNr ++) {
            StateNFA state = hypothesis.getState(rowNr);
            for(int letterNr = 0; letterNr < alphabet.getLetterSize(); letterNr ++) {
                int succNr = getSuccessorRow(rowNr, letterNr);
                ///according to table add transition
                state.addTransition(letterNr, succNr);
            }
            ///set initial state
            if(observationTable.getUpperTable().get(rowNr).getWord().isEmpty()) {
                hypothesis.setInitial(rowNr);
                
            }
        }
        ////1.mark all the state whether accepting or rejecting
        ////2.if mark failed, return an conflict
        ////  if mark succeed, set the union of accepting state as final state, return null
        options.log.println(this.observationTable.toString());
        options.log.println(this.hypothesis.toDot());
        Conflict conflict = this.markAndDetect(hypothesis);//set final state
        if(conflict != null)
        {
        	///get resolution word
        	ExprValue conflictWord = conflict.getResolutionNew(alphabet, membershipOracle);
        	options.log.println(conflictWord.toString());
        	this.addSuffixToTable(this.observationTable.getUpperTableRowIndex(conflict.getS().getWord()),conflictWord);
            this.makeTableClosed();
        }
	}
}
