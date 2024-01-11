package neu.lab.conflict.soot.tf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import neu.lab.conflict.GlobalVar;
import neu.lab.conflict.graph.Graph4distance;
import neu.lab.conflict.graph.Node4distance;
import neu.lab.conflict.risk.jar.DepJarJRisk;
import neu.lab.conflict.util.Conf;
import neu.lab.conflict.util.MavenUtil;
import neu.lab.conflict.util.SootUtil;
import neu.lab.conflict.vo.MethodCall;
import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.internal.JIfStmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

public class JRiskDistanceCgTf extends JRiskCgTf {

	private static CallGraph instance = null;
//	private static Iterator<Edge> iterator = null;

	public JRiskDistanceCgTf(DepJarJRisk depJarJRisk) {
		super(depJarJRisk);
	}

	/**
	 * 重构函数
	 * 
	 * @param depJarJRisk
	 */
	public JRiskDistanceCgTf(DepJarJRisk depJarJRisk, Set<String> thrownmethods) {
		super(depJarJRisk, thrownmethods);
	}

	public static CallGraph getThisCallGraph() {
		if (instance == null) {
			instance = Scene.v().getCallGraph();// 得到图
		}
		return instance;
	}

//	public static Iterator<Edge> getIteratorForEdge(){
//		if (iterator == null) {
//			System.out.println("cg null");
//			iterator = getThisCallGraph().iterator();//得到边集合
//		}
//		return iterator;
//	}
	/**
	 * 产生图，其中不包含JavaLib的方法，同时不包含conflictJar的方法
	 */
	protected void formGraph() {
		if (graph == null) {
			MavenUtil.i().getLog().info("start form graph...");
			// get call-graph.
			Map<String, Node4distance> name2node = new HashMap<String, Node4distance>();
			// 初始化20000大小

			List<MethodCall> mthdRlts = new ArrayList<MethodCall>();
			CallGraph cg = null;
			if (!Conf.subdivisionLevel) {
				cg = getThisCallGraph();
			} else {
				cg = Scene.v().getCallGraph();// 得到图
			}
			 
			Iterator<Edge> ite = cg.iterator();

			long start = System.currentTimeMillis();

			Edge edge = null;
			SootMethod source = null;
			SootMethod target = null;

			while (ite.hasNext()) {

				edge = ite.next();
				source = edge.src();
				target = edge.tgt();

				if (source.isJavaLibraryMethod() || target.isJavaLibraryMethod()) {
					// filter relation contains javaLibClass 过滤掉JavaLib的类
				} else {
					if (source.isConcrete() && target.isConcrete()) {
						String srcMthdName = source.getSignature();// 源方法名
						String tgtMthdName = target.getSignature();// 目标方法名
						String srcClsName = source.getDeclaringClass().getName();// 源方法的类名
						String tgtClsName = target.getDeclaringClass().getName();// 目标方法的类名

						if (conflictJarClses.contains(SootUtil.mthdSig2cls(srcMthdName))
								&& conflictJarClses.contains(SootUtil.mthdSig2cls(tgtMthdName))) {
							// filter relation inside conflictJar 过滤掉conflictJar中的类
						} else {
							if (!name2node.containsKey(srcMthdName)) {
								name2node.put(srcMthdName, new Node4distance(srcMthdName,
										isHostClass(srcClsName) && !edge.src().isPrivate(),
										riskMthds.contains(srcMthdName), getBranchNum(edge.src().getSignature())));
							}
							if (!name2node.containsKey(tgtMthdName)) {
								name2node.put(tgtMthdName, new Node4distance(tgtMthdName,
										isHostClass(tgtClsName) && !edge.tgt().isPrivate(),
										riskMthds.contains(tgtMthdName), getBranchNum(edge.tgt().getSignature())));
							}
							mthdRlts.add(new MethodCall(srcMthdName, tgtMthdName));
						}
					}
				}
			}

			graph = new Graph4distance(name2node, mthdRlts);

			cg = null;
			System.gc();

			MavenUtil.i().getLog().info("end form graph.");
			MavenUtil.i().getLog().info("form graph time:" + (System.currentTimeMillis() - start));
		}

	}

	private int getBranchNum(String mthd) {
		Integer branchNum = mthd2branch.get(mthd);
		if (null != branchNum)
			return branchNum;
		return 0;
	}

	private int calBranchNum(SootMethod sootMethod) {
		long startTime = System.currentTimeMillis();
		int cnt = 0;
		if (sootMethod.getSource() == null) {

		} else {
			Body body = sootMethod.retrieveActiveBody();
			for (Unit unit : body.getUnits()) {
				if (isBranchNode(unit)) {
					cnt++;
				}
			}
		}
		long runtime = (System.currentTimeMillis() - startTime) / 1000;
		GlobalVar.branchTime += runtime;
		return cnt;
	}

	private boolean isBranchNode(Unit unit) {
		if (unit instanceof soot.jimple.internal.JIfStmt) {
			JIfStmt ifS = (JIfStmt) unit;
			if (!ifS.getTargetBox().getUnit().branches()) {
				return true;
			}
		}
		if (unit instanceof soot.jimple.internal.AbstractSwitchStmt) {
			return true;
		}
		return false;
	}

	@Override
	protected void initMthd2branch() {
		mthd2branch = new HashMap<String, Integer>();
		List<SootMethod> methods = new CopyOnWriteArrayList<SootMethod>();
		for (SootClass sootClass : Scene.v().getApplicationClasses()) {
//			methods = sootClass.getMethods();
			methods.addAll(sootClass.getMethods());
			for (SootMethod method : methods) {
				mthd2branch.put(method.getSignature(), calBranchNum(method));
			}
			methods.clear();
		}
	}
}
