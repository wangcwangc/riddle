package neu.lab.conflict.risk.jar;

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
import neu.lab.conflict.graph.Graph4path;
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
	public Map<Integer, String> getRiskLevel() {
		boolean isUsedDepJar = false; // 记录参与运算的depJar是不是本项目被使用的usedDepJar
		DepJar usedDepJar = conflict.getUsedDepJar(); // 记录usedJar
		Set<DepJar> depJars = conflict.getDepJars();
		Set<String> usedDepJarSet = new HashSet<String>(); // 被使用的usedDepJar风险方法集合
		Map<String, Map<String, Set<String>>> isNotUsedDepJarMap = new HashMap<String, Map<String, Set<String>>>(); // 未被使用的usedDepJars风险方法集合
		Map<String, Set<String>> nowUsedDepJarMethod = null; // 当前DepJar风险方法集合
		Set<String> bottomMethods = null;
		Map<Integer, String> result = new HashMap<Integer, String>();
		for (DepJar depJar : depJars) {
			nowUsedDepJarMethod = new HashMap<String, Set<String>>();
			// 初始化
			this.setUsedDepJar(depJar);
			AllCls.init(DepJars.i(), depJar);
			AllRefedCls.init(depJar);

			for (DepJarJRisk depJarJRisk : jarRisks) {
				bottomMethods = new HashSet<String>();
				if (depJarJRisk.getConflictJar() != this.conflict.getUsedDepJar()) {
					isUsedDepJar = false;
					if (depJar.isSelf(usedDepJar)) {
						isUsedDepJar = true;
					}

					Graph4distance distanceGraph = depJarJRisk.getGraph4distance(depJar);

					if (distanceGraph.getAllNode().isEmpty()) {
						MavenUtil.i().getLog().info("distanceGraph is empty");
						nowUsedDepJarMethod.put(depJarJRisk.getConflictJar().toString(), bottomMethods);
						break;
					}

					Map<String, IBook> distanceBooks = new Dog(distanceGraph).findRlt(distanceGraph.getHostNds(),
							Conf.DOG_DEP_FOR_DIS, Strategy.NOT_RESET_BOOK);
					bottomMethods = depJarJRisk.getMethodBottom(distanceBooks);
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
		String jarSig = null;
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
				jarSig = entrys.getKey();
			}
		}
		if (noUseSetNum > 0) {
			noUseSet = true;
		}

		if (useSet) {
			jarSig = usedDepJar.toString();
		}
		int riskLevel = 0;
		/*
		 * 风险1：项目使用的jar包和不使用的jar包的风险方法集合都为空 风险2：项目使用的jar包风险方法集合为空，不使用的jar包都有风险方法
		 * 风险3：项目使用的jar包风险方法集合不为空，不使用的jar包中有风险方法集合为空的jar包
		 * 风险4：项目使用的jar包和不使用的jar包的风险方法集合都为空
		 */
		if (useSet && noUseSet && noUseSetNum == isNotUsedDepJarMapSize) {
			riskLevel = 1;
		} else if (useSet) {
			riskLevel = 2;
		} else if (!useSet && noUseSet) {
			riskLevel = 3;
		} else if (!useSet && !noUseSet) {
			riskLevel = 4;
		}
		// 重置
		this.setUsedDepJar(usedDepJar);
		AllCls.init(DepJars.i(), usedDepJar);
		AllRefedCls.init(usedDepJar);
		result.put(riskLevel, jarSig);
		return result;
	}

	/**
	 * 得到Conflict的等级 分为1-3两个等级 记录3等级Conflict（1等级的Conflict忽略不计算，可大幅减少运算时间）
	 * 继续计算3等级，分为3-4两个等级
	 * 
	 * @return
	 */
	public Set<String> getConflictLevel() {
		Set<String> usedRiskMethods = new HashSet<String>(); // 被使用的usedDepJar风险方法集合
		for (DepJarJRisk depJarJRisk : jarRisks) {
			Graph4distance distanceGraph = depJarJRisk.getGraph4distance();
			Map<String, IBook> distanceBooks = new Dog(distanceGraph).findRlt(distanceGraph.getHostNds(),
					Conf.DOG_DEP_FOR_DIS, Strategy.NOT_RESET_BOOK);
			Set<String> bottomMethods = depJarJRisk.getMethodBottom(distanceBooks);
			usedRiskMethods.addAll(bottomMethods);
		}
		return usedRiskMethods;
	}

//	 public Set<String> getReachRiskMethods(){
//		 Set<String> usedRiskMethods = new HashSet<String>(); // 被使用的usedDepJar风险方法集合
//		 Map<String, IBook> pathBooks = null;
//			for (DepJarJRisk depJarJRisk : jarRisks) {
//				 Graph4path pathGraph = depJarJRisk.getGraph4mthdPath();
//				 if (Conf.fromHostSearch) {
//					 pathBooks = new Dog(pathGraph).findRlt(pathGraph.getHostNds(),
//								Conf.DOG_DEP_FOR_DIS, Strategy.NOT_RESET_BOOK);
//					 usedRiskMethods.add("fromHostSearch");
//				 }else {
//					 pathBooks = new Dog(pathGraph).findRlt(pathGraph.getMiddleNodes(),
//								Conf.DOG_DEP_FOR_DIS, Strategy.NOT_RESET_BOOK);
//					 usedRiskMethods.add("notFromHostSearch");
//				 }
//				Set<String> bottomMethods = depJarJRisk.getMethodBottomForPath(pathBooks);
//				usedRiskMethods.addAll(bottomMethods);
//			}
//			return usedRiskMethods;
//	 }
	public int getReachRiskMethods() {
		Map<String, IBook> pathBooks = null;
		for (DepJarJRisk depJarJRisk : jarRisks) {
			Graph4path pathGraph = depJarJRisk.getGraph4mthdPath();
			pathBooks = new Dog(pathGraph).findRlt(pathGraph.getMiddleNodes(), Conf.DOG_DEP_FOR_DIS,
					Strategy.NOT_RESET_BOOK);
			if (depJarJRisk.getMethodBottomForPath(pathBooks).size() > 0) {
				return 2;
			} else {
				pathBooks = new Dog(pathGraph).findRlt(pathGraph.getHostNds(), Conf.DOG_DEP_FOR_DIS,
						Strategy.NOT_RESET_BOOK);
				if (depJarJRisk.getMethodBottomForPath(pathBooks).size() > 0) {
					return 3;
				}
			}
		}
		return 3;
	}
}