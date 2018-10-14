package neu.lab.conflict.risk.jar;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import neu.lab.conflict.container.AllCls;
import neu.lab.conflict.container.DepJars;
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
	 * method:得到风险等级 name:wangchao time:2018-9-29 16:27:21
	 */
	public int getRiskLevel() {
		int level = 1;
		int i = 0;
		int a = 0;
		int b = 0;
		int c = 0;
		int t = 0;
		DepJar usedDepJar = conflict.getUsedDepJar(); // 记录usedJar
		Set<DepJar> depJars = conflict.getDepJars();
		for (DepJarJRisk depJarJRisk : jarRisks) {
			for (DepJar depJar : depJars) {
				this.setUsedDepJar(depJar);
				if (depJarJRisk.getConflictJar() != this.conflict.getUsedDepJar()) {
					
					i++;
					
					AllCls.init(DepJars.i(), depJar);

//				for (String m : depJarJRisk.getThrownMthds(depJar)) {
//					System.out.println(m);
//				}	
					Set<String> thrownMthds = depJarJRisk.getThrownMthds(depJar);
					if (thrownMthds.size() > 0) {
						t = 0;
						for (String thrownMthd : thrownMthds) {
							if (usedDepJar.getAllMthd().contains(thrownMthd)) {
								t++;
							}	else {
							}
						}
						if (thrownMthds.size() == t) {
							c++;
						} else {
							a++;
						}
					} else {
						b++;
					}
				} else {
					continue;
				}
			}
		}
		this.setUsedDepJar(usedDepJar);
		AllCls.init(DepJars.i(), usedDepJar);

		//if (a > 0) {
			if (a + c == i) {
				level = 4;
			} else if (i == a + b && a != 0){
				level = 3;
			} else if (i == b + c && c != 0) {
				level = 2;
			} else if (i == b) {
				level = 1;
			}
		//}
		System.out.println("a" + a + "b" + b + "c" + c + "i" + i);
		return level;

	}
}
