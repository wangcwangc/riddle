package neu.lab.conflict.soot.tf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import neu.lab.conflict.graph.Graph4path;
import neu.lab.conflict.graph.Node4path;
import neu.lab.conflict.risk.jar.DepJarJRisk;
import neu.lab.conflict.util.Conf;
import neu.lab.conflict.util.MavenUtil;
import neu.lab.conflict.util.SootUtil;
import neu.lab.conflict.vo.MethodCall;
import soot.Scene;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

public class JRiskMthdPathCgTf extends JRiskCgTf {

	public JRiskMthdPathCgTf(DepJarJRisk depJarJRisk) {
		super(depJarJRisk);
	}

	public JRiskMthdPathCgTf(DepJarJRisk depJarJRisk, Set<String> thrownmethods) {
		super(depJarJRisk, thrownmethods);
	}

	@Override
	protected void formGraph() {
		if (graph == null) {
			MavenUtil.i().getLog().info("start form graph...");
			// get call-graph.
			Map<String, Node4path> name2node = new HashMap<String, Node4path>();

			List<MethodCall> mthdRlts = new ArrayList<MethodCall>();

//			CallGraph cg = null;
//			
//			if (!Conf.subdivisionLevel) {
//				cg = getThisCallGraph();
//			} else {
//				cg = Scene.v().getCallGraph();// 得到图
//			}

			Iterator<Edge> ite = getThisCallGraph().iterator();

			Edge edge = null;

			String srcMthdName = null;
			String tgtMthdName = null;

			String srcClsName = null;
			String tgtClsName = null;

			while (ite.hasNext()) {

				edge = ite.next();

				if (edge.src().isJavaLibraryMethod() || edge.tgt().isJavaLibraryMethod()) {
					// filter relation contains javaLibClass
				} else {

					srcMthdName = edge.src().getSignature();
					tgtMthdName = edge.tgt().getSignature();

					srcClsName = edge.src().getDeclaringClass().getName();
					tgtClsName = edge.tgt().getDeclaringClass().getName();

					if (conflictJarClses.contains(SootUtil.mthdSig2cls(srcMthdName))
							&& conflictJarClses.contains(SootUtil.mthdSig2cls(tgtMthdName))) {
//						System.out.println(srcMthdName);
						// filter relation inside conflictJar
					} else {
						if (!name2node.containsKey(srcMthdName)) {
							name2node.put(srcMthdName,
									new Node4path(srcMthdName, isHostClass(srcClsName) && !edge.src().isPrivate(),
											riskMthds.contains(srcMthdName), usedJarClasses.contains(SootUtil.mthdSig2cls(srcMthdName))));
						}
						if (!name2node.containsKey(tgtMthdName)) {
							name2node.put(tgtMthdName,
									new Node4path(tgtMthdName, isHostClass(tgtClsName) && !edge.tgt().isPrivate(),
											riskMthds.contains(tgtMthdName), usedJarClasses.contains(SootUtil.mthdSig2cls(tgtMthdName))));
						}
						mthdRlts.add(new MethodCall(srcMthdName, tgtMthdName));
					}
				}
			}
			graph = new Graph4path(name2node, mthdRlts);
			MavenUtil.i().getLog().info("end form graph.");
		}
	}

	@Override
	protected void initMthd2branch() {

	}

}
