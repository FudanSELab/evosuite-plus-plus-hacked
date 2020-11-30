package feature.objectconstruction.testgeneration.testcase;

import java.lang.reflect.Method;

import org.evosuite.Properties;
import org.evosuite.Properties.StatisticsBackend;
import org.evosuite.utils.MethodUtil;
import org.junit.Test;

import common.TestUtility;
import feature.objectconstruction.testgeneration.example.ObjectExample;
import sf100.CommonTestUtil;

public class ProjectOverallTest extends TestUtility{
	@Test
	public void testCascadeCall() {
		Class<?> clazz = ObjectExample.class;
		String methodName = "test";
		int parameterNum = 1;
		
		String targetClass = clazz.getCanonicalName();
//		Method method = clazz.getMethods()[0];
		Method method = TestUtility.getTargetMethod(methodName, clazz, parameterNum);

		String targetMethod = method.getName() + MethodUtil.getSignature(method);
		String cp = "target/test-classes";

		// Properties.LOCAL_SEARCH_RATE = 1;
//		Properties.DEBUG = true;
//		Properties.PORT = 8000;
		Properties.CLIENT_ON_THREAD = true;
		Properties.STATISTICS_BACKEND = StatisticsBackend.DEBUG;

		Properties.TIMEOUT = 1000;
//		Properties.TIMELINE_INTERVAL = 3000;
		
		String fitnessApproach = "branch";
		
		int timeBudget = 100;
		
		boolean aor = true;
		TestUtility.evoTestSingleMethod(targetClass,  
				targetMethod, timeBudget, true, aor, cp, fitnessApproach, 
				"generateMOSuite", "MOSUITE", "DynaMOSA");
		
	}
	
	@Test
	public void testStudentInterface() {
		Class<?> clazz = ObjectExample.class;
		String methodName = "test1";
		int parameterNum = 2;
		
		String targetClass = clazz.getCanonicalName();
//		Method method = clazz.getMethods()[0];
		Method method = TestUtility.getTargetMethod(methodName, clazz, parameterNum);

		String targetMethod = method.getName() + MethodUtil.getSignature(method);
		String cp = "target/classes";

		// Properties.LOCAL_SEARCH_RATE = 1;
//		Properties.DEBUG = true;
//		Properties.PORT = 8000;
		Properties.CLIENT_ON_THREAD = true;
		Properties.STATISTICS_BACKEND = StatisticsBackend.DEBUG;

		Properties.TIMEOUT = 10000000;
//		Properties.TIMELINE_INTERVAL = 3000;
		
		String fitnessApproach = "fbranch";
		
		int timeBudget = 30000;
		TestUtility.evosuiteFlagBranch(targetClass, targetMethod, cp, timeBudget, true, fitnessApproach);
	}
	
	@Test
	public void testStudentAbstract() {
		Class<?> clazz = ObjectExample.class;
		String methodName = "test2";
		int parameterNum = 2;
		
		String targetClass = clazz.getCanonicalName();
//		Method method = clazz.getMethods()[0];
		Method method = TestUtility.getTargetMethod(methodName, clazz, parameterNum);

		String targetMethod = method.getName() + MethodUtil.getSignature(method);
		String cp = "target/classes";

		// Properties.LOCAL_SEARCH_RATE = 1;
//		Properties.DEBUG = true;
//		Properties.PORT = 8000;
		Properties.CLIENT_ON_THREAD = true;
		Properties.STATISTICS_BACKEND = StatisticsBackend.DEBUG;

		Properties.TIMEOUT = 10000000;
//		Properties.TIMELINE_INTERVAL = 3000;
		
		String fitnessApproach = "fbranch";
		
		int timeBudget = 30000;
		TestUtility.evosuiteFlagBranch(targetClass, targetMethod, cp, timeBudget, true, fitnessApproach);
	}
}
