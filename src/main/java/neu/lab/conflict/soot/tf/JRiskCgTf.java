package neu.lab.conflict.soot.tf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import neu.lab.conflict.graph.IGraph;
import neu.lab.conflict.risk.jar.DepJarJRisk;
import neu.lab.conflict.util.Conf;
import neu.lab.conflict.util.LibCopyInfo;
import neu.lab.conflict.util.MavenUtil;
import soot.Scene;
import soot.SceneTransformer;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;

/**
 * to get call-graph. 得到call-graph
 * 
 * @author asus
 *
 */
public abstract class JRiskCgTf extends SceneTransformer {

	// private DepJarJRisk depJarJRisk;
	protected Set<String> entryClses; // 入口类集合
	protected Set<String> conflictJarClses; // 冲突jar类集合
	protected Set<String> usedJarClasses; // 冲突jar类集合
	protected Set<String> riskMthds; // 风险方法集合
	protected Set<String> rchMthds;
	protected IGraph graph;
	protected Map<String, Integer> mthd2branch;

	protected static CallGraph instance = null;

	public static CallGraph getThisCallGraph() {
		if (instance == null) {
			instance = Scene.v().getCallGraph();// 得到图
		}
		return instance;
	}

	public JRiskCgTf(DepJarJRisk depJarJRisk) {
		super();
		// this.depJarJRisk = depJarJRisk;
		entryClses = depJarJRisk.getEntryJar().getAllCls(true);
		conflictJarClses = depJarJRisk.getConflictJar().getAllCls(true);
		riskMthds = depJarJRisk.getThrownMthds();
		rchMthds = new HashSet<String>();

	}

	/**
	 * 重构函数
	 * 
	 * @param depJarJRisk
	 * @param thrownMethods
	 */
	public JRiskCgTf(DepJarJRisk depJarJRisk, Set<String> thrownMethods) {
		super();
		// this.depJarJRisk = depJarJRisk;
		entryClses = depJarJRisk.getEntryJar().getAllCls(true);
		conflictJarClses = depJarJRisk.getConflictJar().getAllCls(true);
		usedJarClasses = depJarJRisk.getUsedJar().getAllCls(true);
		riskMthds = thrownMethods;
		rchMthds = new HashSet<String>();

	}

	@Override
	protected void internalTransform(String arg0, Map<String, String> arg1) {

		if (!Conf.subdivisionLevel) {
			MavenUtil.i().getLog().info("JRiskCgTf start..");
			if (instance == null) {
				Map<String, String> cgMap = new HashMap<String, String>();
				cgMap.put("enabled", "true");
				cgMap.put("apponly", "true");
				cgMap.put("all-reachable", "true");
				initMthd2branch();
				CHATransformer.v().transform("wjtp", cgMap);
			}
		} else {
			MavenUtil.i().getLog().info("JRiskCgTf start..");
			Map<String, String> cgMap = new HashMap<String, String>();
			cgMap.put("enabled", "true");
			cgMap.put("apponly", "true");
			cgMap.put("all-reachable", "true");
			initMthd2branch();
			CHATransformer.v().transform("wjtp", cgMap);
			
			instance = Scene.v().getCallGraph();
		}

		formGraph();

		MavenUtil.i().getLog().info("JRiskCgTf end..");
	}

	protected abstract void initMthd2branch();

	protected abstract void formGraph();

	protected boolean isHostClass(String clsName) {
		return entryClses.contains(clsName) && !LibCopyInfo.isLibCopy(MavenUtil.i().getProjectCor(), clsName);
	}

	public IGraph getGraph() {
		return graph;
	}

}