package org.evosuite.testcase.synthesizer.var;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.branch.Branch;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.graphs.interprocedural.var.DepVariable;
import org.evosuite.runtime.System;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.NullStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.synthesizer.UsedReferenceSearcher;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.generic.GenericField;
import org.objectweb.asm.tree.FieldInsnNode;

public class FieldVariableWrapper extends DepVariableWrapper{

	protected FieldVariableWrapper(DepVariable var) {
		super(var);
	}

	/**
	 * set the field into the parentVarRef
	 * 
	 * @param test
	 * @param position
	 * @param var
	 * @param parentVarRef
	 * @param isStatic
	 * @return
	 */
	@Override
	public List<VariableReference> generateOrFindStatement(TestCase test, boolean isLeaf, VariableReference callerObject,
			Map<DepVariableWrapper, List<VariableReference>> map, Branch b, boolean allowNullValue) {
		List<VariableReference> list = new ArrayList<>();
		VariableReference var = generateOrFind(test, isLeaf, callerObject, map, b, allowNullValue);
		if(var != null) {
			list.add(var);
		}
		
		return list;
	}
	
	
	public VariableReference generateOrFind(TestCase test, boolean isLeaf, VariableReference callerObject,
			Map<DepVariableWrapper, List<VariableReference>> map, Branch b, boolean allowNullValue) {
		FieldInsnNode fieldNode = (FieldInsnNode) this.var.getInstruction().getASMNode();
		String fieldType = fieldNode.desc;
		String fieldOwner = fieldNode.owner.replace("/", ".");
		String fieldTypeName = fieldType.replace("/", ".");
		if(fieldTypeName.startsWith("L")) {
			fieldTypeName = fieldTypeName.substring(1, fieldTypeName.length()-1);
		}
		else if(fieldTypeName.startsWith("[L")) {
			fieldTypeName = fieldTypeName.substring(2, fieldTypeName.length()-1);
		}
		String fieldName = fieldNode.name;
		
		//string value
		if(fieldOwner.equals("java.lang.String") && fieldName.equals("value"))
			return null;

		if (callerObject != null) {
			Statement stat = test.getStatement(callerObject.getStPosition());
			if(stat instanceof NullStatement) {
				return null;
			}
			
			String callerType = callerObject.getClassName();
			if (!VariableCodeGenerationUtil.isPrimitiveClass(callerType)) {
				if (!VariableCodeGenerationUtil.isCompatible(fieldOwner, callerType)) {
					System.currentTimeMillis();
					return null;
				}
			}
		}

		try {
			Class<?> fieldDeclaringClass = TestGenerationContext.getInstance().getClassLoaderForSUT()
					.loadClass(fieldOwner);
//			registerAllMethods(fieldDeclaringClass);	
			Field field = VariableCodeGenerationUtil.searchForField(fieldDeclaringClass, fieldName);
			/**
			 * if the field is leaf, check if there is setter in the testcase
			 * if the field is not leaf, check if there is getter in the testcase
			 * if found, stop here
			 */
			UsedReferenceSearcher usedRefSearcher = new UsedReferenceSearcher();
			VariableReference usedFieldInTest = isLeaf
					? usedRefSearcher.searchRelevantFieldWritingReferenceInTest(test, field, callerObject)
					: usedRefSearcher.searchRelevantFieldReadingReferenceInTest(test, field, callerObject);
//			System.currentTimeMillis();
			if (usedFieldInTest != null) {
				/**
				 * generate some elements for container classes
				 */
				if(Collection.class.isAssignableFrom(usedFieldInTest.getVariableClass())) {
					VariableCodeGenerationUtil.generateElements(field.getType(), test, usedFieldInTest);
				}
				
				return usedFieldInTest;
			}

			/**
			 * now we try to generate the relevant statement in the test case.
			 */
			GenericField genericField = new GenericField(field, field.getDeclaringClass());
			int fieldModifiers = field.getModifiers();

			/**
			 * deal with public field, we handle the public field getter/setter in the same way, a.k.a., create
			 * a new public field instance.
			 */
			if (Modifier.isPublic(fieldModifiers)) {
				if (genericField.isFinal()) {
					return null;
				}
				
				VariableReference obj = 
						VariableCodeGenerationUtil.generatePublicFieldSetterOrGetter(test, callerObject, fieldType, genericField, allowNullValue);
				return obj;
			}

			/**
			 * deal with non-public field
			 */
			if (!isLeaf) {
				VariableReference getterObject = VariableCodeGenerationUtil.generateFieldGetterInTest(test, callerObject, map, fieldDeclaringClass, field,
						usedRefSearcher, b);
				return getterObject;
			} 
			else {
				VariableReference setterObject = VariableCodeGenerationUtil.generateFieldSetterInTest(test, callerObject, map, fieldDeclaringClass, field, allowNullValue);
				if(setterObject != null) {
					Statement statement = test.getStatement(setterObject.getStPosition());
					if(statement instanceof MethodStatement) {
						MethodStatement mStat = (MethodStatement)statement;
						if(!mStat.getParameterReferences().isEmpty()) {
							VariableReference ref = mStat.getParameterReferences().get(0);
							return ref;							
						}
					}
				}
//				System.currentTimeMillis();
				return setterObject;
			}

		} catch (ClassNotFoundException | SecurityException | ConstructionFailedException e) {
//			this.printConstructionError(test, b);
			e.printStackTrace();
			return null;
		}
	}
}
