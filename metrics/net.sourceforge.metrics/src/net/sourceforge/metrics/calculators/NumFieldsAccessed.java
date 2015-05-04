package net.sourceforge.metrics.calculators;

import java.util.HashSet;

import org.eclipse.jdt.core.dom.*;

import net.sourceforge.metrics.core.*;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;

public class NumFieldsAccessed extends Calculator {
	private class Visitor extends ASTVisitor {
		private final ITypeBinding thisClass;
		private int numFields;
		private int numInternalMethods;
		private int numExternalMethods;
		private HashSet<String> countedFields = new HashSet<String>();
		private HashSet<String> countedMethods = new HashSet<String>();

		public Visitor(ITypeBinding thisClass) {
			this.thisClass = thisClass;
		}

		@Override
		public boolean visit(MethodInvocation node) {
			IMethodBinding method = node.resolveMethodBinding();
			if (method != null) {
				String key = method.getKey();
				if (!countedMethods.contains(key)) {
					countedMethods.add(key);
					/* declaring class of the method being called */
					ITypeBinding type = method.getDeclaringClass();
					if (type != null && type.isEqualTo(thisClass)) {
						/* internal method */
						numInternalMethods++;
					} else {
						/* external method */
						numExternalMethods++;
					}
				}
			}
			return false;
		}

		@Override
		public boolean visit(SimpleName node) {
			/* we have to request bindings when building AST! */
			IBinding binding = node.resolveBinding();
			if (binding != null && binding.getKind() == IBinding.VARIABLE) {
				IVariableBinding var = (IVariableBinding) binding;
				ITypeBinding type = var.getDeclaringClass();
				if (type != null && type.isEqualTo(thisClass)) {
					String key = var.getKey();
					if (!countedFields.contains(key)) {
						countedFields.add(key);
						numFields++;
					}
				}
			}
			return false;
		}
	}

	public NumFieldsAccessed() {
		super(NUM_FIELDS_USED);
	}

	@Override
	public void calculate(AbstractMetricSource source)
			throws InvalidSourceException {
		MethodDeclaration node = (MethodDeclaration) source.getASTNode();
		Visitor v = new Visitor(node.resolveBinding().getDeclaringClass());
		node.accept(v);
		source.setValue(new Metric(NUM_FIELDS_USED, v.numFields));
		source.setValue(new Metric(NUM_INT_METHODS_CALLED, v.numInternalMethods));
		source.setValue(new Metric(NUM_EXT_METHODS_CALLED, v.numExternalMethods));
	}
}
