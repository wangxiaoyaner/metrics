package net.sourceforge.metrics.calculators;

import org.eclipse.jdt.core.dom.*;
import net.sourceforge.metrics.core.*;

import net.sourceforge.metrics.core.sources.AbstractMetricSource;

public class NumLocals extends Calculator {
	private static class Visitor extends ASTVisitor {
		private int n;

		@Override
		public boolean visit(VariableDeclarationFragment node) {
			n++;
			return false;
		}
	}

	public NumLocals() {
		super(NUM_LOCALS);
	}

	@Override
	public void calculate(AbstractMetricSource source)
			throws InvalidSourceException {
		ASTNode node = source.getASTNode();
		Visitor v = new Visitor();
		node.accept(v);
		source.setValue(new Metric(NUM_LOCALS, v.n));
	}
}
