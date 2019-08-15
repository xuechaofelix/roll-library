package roll.learner.bb_a;

import roll.automata.BB_A;
import roll.learner.LearnerBase;
import roll.learner.LearnerType;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.query.QuerySimple;
import roll.table.ExprValue;
import roll.table.ExprValueWordPair;
import roll.table.HashableValue;
import roll.table.ObservationRow;
import roll.table.ObservationTable;
import roll.words.Alphabet;
import roll.words.Word;

public abstract class LearnerBB_A extends LearnerBase<BB_A>{

	public LearnerBB_A(Options options, Alphabet alphabet, MembershipOracle<HashableValue> membershipOracle) {
		super(options, alphabet, membershipOracle);
	}
	
	@Override
	public LearnerType getLearnerType() {
		return LearnerType.BB_A_LOMEGA;
	}

	@Override
	public String toHTML() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Query<HashableValue> makeMembershipQuery(ObservationRow row, int offset, ExprValue exprValue) {
		return new QuerySimple<>(row, row.getWord().concat((Word)exprValue.getLeft()), (Word)exprValue.getRight(), offset);
	}


	@Override
	protected void initializeTable(ObservationTable observationTable) {
		observationTable.clear();
        Word wordEmpty = alphabet.getEmptyWord();
        observationTable.addUpperRow(wordEmpty);
        
        //1. add letter to column
        //2. add every letter extension of the words from the upper table
        for(int letterNr = 0; letterNr < alphabet.getLetterSize(); letterNr ++) {
        	observationTable.addColumn(new ExprValueWordPair(this.alphabet.getEmptyWord(),this.alphabet.getLetterWord(letterNr)));
            observationTable.addLowerRow(alphabet.getLetterWord(letterNr));
        }
        
        // ask initial queries for upper table
        processMembershipQueries(observationTable, observationTable.getUpperTable()
                , 0, observationTable.getColumns().size());
        // ask initial queries for lower table
        processMembershipQueries(observationTable, observationTable.getLowerTable()
                , 0, observationTable.getColumns().size());
	}

	@Override
	protected ExprValue getInitialColumnExprValue() {
		return new ExprValueWordPair(this.alphabet.getEmptyWord(),this.alphabet.getEmptyWord());
	}


}
