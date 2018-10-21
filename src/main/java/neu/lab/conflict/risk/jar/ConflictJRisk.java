package neu.lab.conflict.risk.jar;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import neu.lab.conflict.container.AllCls;
import neu.lab.conflict.container.AllRefedCls;
import neu.lab.conflict.container.DepJars;
import neu.lab.conflict.graph.Dog;
import neu.lab.conflict.graph.Graph4distance;
import neu.lab.conflict.graph.IBook;
import neu.lab.conflict.graph.Dog.Strategy;
import neu.lab.conflict.util.Conf;
import neu.lab.conflict.util.MavenUtil;
import neu.lab.conflict.vo.Conflict;
import neu.lab.conflict.vo.DepJar;

/**
 * 有风险的冲突jar
 * 
 * @author wangchao
 *
 */
public class ConflictJRisk {

	private Conflict conflict; // 冲突
	private List<DepJarJRisk> jarRisks; // 依赖风险jar集合

	/*
	 * 构造函数
	 */
	public ConflictJRisk(Conflict conflict) {
		this.conflict = conflict;
		jarRisks = new ArrayList<DepJarJRisk>();
		for (DepJar jar : conflict.getDepJars()) {
			jarRisks.add(new DepJarJRisk(jar, this));
		}
	}

	public DepJar getUsedDepJar() {
		return conflict.getUsedDepJar();
	}

	public Conflict getConflict() {
		return conflict;
	}

	public List<DepJarJRisk> getJarRisks() {
		return jarRisks;
	}

	public void setUsedDepJar(DepJar depJar) {
		conflict.setUsedDepJar(depJar);
	}

	/**
	 * 可以细分等级1, 2，3，4 method:得到风险等级 name:wangchao time:2018-9-29 16:27:21
	 */
	public int getRiskLevel() {

		boolean isUsedDepJar = false;	//记录参与运算的depJar是不是本项目被使用的usedDepJar
		DepJar usedDepJar = conflict.getUsedDepJar(); // 记录usedJar
		Set<DepJar> depJars = conflict.getDepJars();
		Set<String> usedDepJarSet = new HashSet<String>();		//被使用的usedDepJar风险方法集合
		Map<String, Map<String, Set<String>>> isNotUsedDepJarMap = new HashMap<String, Map<String, Set<String>>>();	//未被使用的usedDepJars风险方法集合
		Map<String, Set<String>> nowUsedDepJarMethod = null;	//当前DepJar风险方法集合
		for (DepJar depJar : depJars) {
			nowUsedDepJarMethod = new HashMap<String, Set<String>>();
			for (DepJarJRisk depJarJRisk : jarRisks) {

				this.setUsedDepJar(depJar);
				if (depJarJRisk.getConflictJar() != this.conflict.getUsedDepJar()) {
					isUsedDepJar = false;
					if (depJar.isSelf(usedDepJar)) {
						isUsedDepJar = true;
					}
					//初始化
					AllCls.init(DepJars.i(), depJar);
					AllRefedCls.init(depJar);

					Graph4distance distanceGraph = depJarJRisk.getGraph4distance();

					if (distanceGraph.getAllNode().isEmpty()) {
						MavenUtil.i().getLog().info("distanceGraph is empty");
						break;
					}

					Map<String, IBook> distanceBooks = new Dog(distanceGraph).findRlt(distanceGraph.getHostNds(),
							Conf.DOG_DEP_FOR_DIS, Strategy.NOT_RESET_BOOK);
//					MethodProbDistances distances = depJarJRisk.getMethodProDistances(distanceBooks);
					Set<String> bottomMethods = depJarJRisk.getMethodBottom(distanceBooks);
					if (isUsedDepJar) {
						usedDepJarSet.addAll(bottomMethods);
					} else {

						nowUsedDepJarMethod.put(depJarJRisk.getConflictJar().toString(), bottomMethods);

					}
				}

			}
			if (!nowUsedDepJarMethod.isEmpty()) {
				isNotUsedDepJarMap.put(this.conflict.getUsedDepJar().toString(), nowUsedDepJarMethod);
			}

		}
		/*
		 * 使用的集合是不是为空 不使用的集合是不是为空
		 */
		boolean useSet = false;
		boolean noUseSet = false;
		int isNotUsedDepJarMapSize = isNotUsedDepJarMap.entrySet().size();
		int noUseSetNum = 0;
		if (usedDepJarSet.isEmpty()) {
			useSet = true;
		}
		for (Entry<String, Map<String, Set<String>>> entrys : isNotUsedDepJarMap.entrySet()) {
			Map<String, Set<String>> mapEntry = entrys.getValue();
			boolean isnot = true;
			for (Entry<String, Set<String>> entry : mapEntry.entrySet()) {
				if (entry.getValue().isEmpty()) {
					continue;
				} else {
					isnot = false;
				}
			}
			if (isnot) {
				noUseSetNum++;
			}
		}
		if (noUseSetNum > 0) {
			noUseSet = true;
		}
		int riskLevel = 0;
		/*
		 * 风险1：项目使用的jar包和不使用的jar包的风险方法集合都为空
		 * 风险2：项目使用的jar包风险方法集合为空，不使用的jar包都有风险方法
		 * 风险3：项目使用的jar包风险方法集合不为空，不使用的jar包中有风险方法集合为空的jar包
		 * 风险4：项目使用的jar包和不使用的jar包的风险方法集合都为空
		 */
		if (useSet && noUseSet && noUseSetNum == isNotUsedDepJarMapSize) {
			riskLevel = 1;
		} else if (useSet && !noUseSet) {
			riskLevel = 2;
		} else if (!useSet && noUseSet) {
			riskLevel = 3;
		} else if (!useSet && !noUseSet) {
			riskLevel = 4;
		}
		return riskLevel;
	}

}