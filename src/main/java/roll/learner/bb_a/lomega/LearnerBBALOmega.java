package roll.learner.bb_a.lomega;

import roll.automata.NBA;
import roll.learner.LearnerBase;
import roll.learner.LearnerType;
import roll.learner.bb_a.LearnerBB_A;
import roll.learner.bb_a.table.LearnerBB_ATableLOmega;
import roll.learner.bb_a.table.LearnerBB_ATableLSimple;
import roll.main.Options;
import roll.oracle.MembershipOracle;
import roll.query.Query;
import roll.table.HashableValue;
import roll.words.Alphabet;

public class LearnerBBALOmega extends LearnerBase<NBA>{
	private final LearnerBB_A bbaLearner;
	public LearnerBBALOmega(Options options, Alphabet alphabet, MembershipOracle<HashableValue> membershipOracle) {
		super(options, alphabet, membershipOracle);
		bbaLearner = new LearnerBB_ATableLOmega(options, alphabet, membershipOracle);
	}
	public LearnerBBALOmega(Options options, Alphabet alphabet, MembershipOracle<HashableValue> membershipOracle, boolean isLOmega) {
		super(options, alphabet, membershipOracle);
		if(isLOmega) {
			bbaLearner = new LearnerBB_ATableLOmega(options, alphabet, membershipOracle);
		}
		else {
			bbaLearner = new LearnerBB_ATableLSimple(options, alphabet, membershipOracle);
		}
	}
	
	@Override
	public LearnerType getLearnerType() {
		return bbaLearner.getLearnerType();
	}

	@Override
	public void refineHypothesis(Query<HashableValue> query) {
		bbaLearner.refineHypothesis(query);
		this.constructHypothesis();
	}

	@Override
	public String toHTML() {
		return bbaLearner.toHTML();
	}

	@Override
	protected void initialize() {
		this.bbaLearner.startLearning();
        constructHypothesis();
	}

	@Override
	protected void constructHypothesis() {
		this.hypothesis = this.bbaLearner.getHypothesis().ToNBA();
	}

	@Override
	public String toString() {
		return bbaLearner.toString();
	}

}
