package neu.lab.conflict.risk.jar;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import neu.lab.conflict.vo.Conflict;
import neu.lab.conflict.vo.DepJar;

public class ConflictJRisk {
	
	private Conflict conflict;
	private List<DepJarJRisk> jarRisks;

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

	/*
	 * method:得到风险等级
	 * name:wangchao
	 * time:2018-9-29 16:27:21
	 */
	public int getRiskLevel() {
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
