package neu.lab.conflict.risk.jar;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import neu.lab.conflict.util.MavenUtil;
import neu.lab.conflict.vo.Conflict;
import neu.lab.conflict.vo.DepJar;

/**
 * 有风险的冲突jar
 * @author wangchao
 *
 */
public class ConflictJRisk {
	
	private Conflict conflict;		//冲突
	private List<DepJarJRisk> jarRisks;			//依赖风险jar集合

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

	/**
	 * method:得到风险等级
	 * name:wangchao
	 * time:2018-9-29 16:27:21
	 */
	public int getRiskLevel() {
		Set<DepJar> depJars = conflict.getOtherDepJar4Use();
		//System.out.println(depJars.size());
		for (DepJar d : depJars) {
			MavenUtil.i().getLog().info("this is not used jar for use" + d.toString());
			System.out.println(jarRisks.size());
			for (DepJarJRisk eee : jarRisks) {
				System.out.println(eee.toString());
			}
			//System.out.println("this is not used jar" + d.toString());
		}
		int i = 1;
		DepJar depJar = conflict.getUsedDepJar();
		for (DepJarJRisk depJarJRisk : jarRisks) {
			for (String method : depJarJRisk.getThrownMthds()) {
				if(depJar.getAllMthd().contains(method)) {
					i = 1;
					break;
				} else {
					i = 3;
				}
			}
		}
		return i;
		
	}
}
