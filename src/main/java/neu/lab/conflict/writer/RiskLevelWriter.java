package neu.lab.conflict.writer;

import java.io.BufferedWriter;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultElement;

import neu.lab.conflict.container.Conflicts;
import neu.lab.conflict.risk.jar.ConflictJRisk;
import neu.lab.conflict.risk.jar.DepJarJRisk;
import neu.lab.conflict.util.Conf;
import neu.lab.conflict.util.MavenUtil;
import neu.lab.conflict.vo.Conflict;

public class RiskLevelWriter {

	/**
	 * 输出到XML中
	 * 
	 * @param outPath
	 * @param append
	 */
	public void writeRiskLevelXML(String outPath, boolean append, boolean subdivisionLevel) {
		try {
			Writer fileWriter;
			String fileName = MavenUtil.i().getProjectGroupId() + ":" + MavenUtil.i().getProjectArtifactId() + ":"
					+ MavenUtil.i().getProjectVersion();
			if (outPath == null) {
				fileWriter = new CharArrayWriter();
			} else {
				File file = new File(outPath + fileName.replace('.', '_').replace(':', '_') + ".xml");
//				fileWriter = new FileWriter(outPath + fileName.replace('.', '_').replace(':', '_') + ".xml", append);
				fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));

			}
			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setNewlines(true);
			XMLWriter xmlWriter = new XMLWriter(fileWriter, format);
			Document document = DocumentHelper.createDocument();
//			document.addComment("Our goal\r\n"
//					+ "Decca aims to detect dependency conflict issues and assess their severity levels according to their impacts on the system and maintenance costs. The severity levels are defined as follows:\r\n"
//					+ "\r\n"
//					+ "Level 1: It is a benign conflict, because the feature set referenced by host project is a subset of the actual loaded feature set. Besides, the shadowed version completely cover the feature set used by the host project. This indicates that any orders of the specification of these duplicate classes on the classpath will not induce serious runtime errors. Therefore, this is a benign conflict and will not affect the system reliability at runtime.\r\n"
//					+ "\r\n"
//					+ "Level 2: It is a benign conflict, because the feature set referenced by host project is a subset of the actual loaded feature set. However, the shadowed feature set doesn’t cover the referenced feature set. It is considered as a potential risk for system reliability since different orders of the specifications of these duplicate classes on the classpath (e.g., in different running environment or building platform) might induce runtime errors. Compared with warnings at Level 1, warnings at Level 2 needs more costs to maintain.\r\n"
//					+ "\r\n"
//					+ "Level 3: It is a harmful conflict, as the actual loaded feature set does not consume the feature set referenced by host project. The runtime errors will occur when the expected feature cannot be accessed. However, in this case, the shadowed feature set completely cover the feature set referenced by host project. Therefore, it can be solved by adjusting the dependency order on the classpath, without changing any source code.\r\n"
//					+ "\r\n"
//					+ "Level 4: It is a harmful conflict, as the actual loaded feature set does not cover the referenced feature set. Besides, the shadowed feature set does not consume the referenced feature set neither. Therefore, this type of conflicts can not be easily resolved by adjusting the dependency orders on the classpath. In this case, to solve these issues, it requires more efforts to ensure the multiple versions of classes could be referenced by host project.");

			document.addComment("\r\n" + "工具简介\r\n" + "Decca目标在于检测软件项目中的依赖冲突问题，根据依赖冲突对软件带来的影响和修复的成本对其评估警告的严重等级：\r\n"
					+ "\r\n" + "MODE 1:\r\n" + "\r\n"
					+ "Level 1. 虽然项目中存在多个版本的依赖冲突问题，但是当前依赖树中存在的多个版本的library都完全覆盖了自身项目期望调用的方法和属性的集合。\r\n"
					+ "         所以冲突的library在classpath上不同的声明顺序，都不会引起运行时刻的异常行为发生。\r\n" + "\r\n"
					+ "Level 2. 存在依赖冲突问题，但是目前可以加载到的那个library的版本完全覆盖了自身的项目期望调用的方法和属性的集合，\r\n"
					+ "         然而在依赖树中但是未被加载到的其他版本的library并没有完全覆盖到自身项目所期望调用的方法和属性的集合。\r\n"
					+ "         虽然当前状态不会引发运行时刻的异常行为，但是它具有一定的风险性。\r\n" + "\r\n"
					+ "Level 3. 加载到的library的版本没有完全覆盖自己的项目想要调用的方法和属性的集合，所以运行时刻的异常行为一定会发生。\r\n"
					+ "         但是依赖树中未被加载到的其他版本的library可以满足调用的需求，只需要调整他们的声明顺序，便可以以较低的修复代价解决问题。\r\n" + "\r\n"
					+ "Level 4. 加载到的library的版本没有完全覆盖自己的项目想要调用的方法和属性的集合，所以运行时刻的异常行为一定会发生。\r\n"
					+ "         并且依赖树中未被加载到其他版本的library同样无法满足调用的需求，需要以一定的方式让这些冲突的版本共存于同一个项目当中。修复代价较高。\r\n" + "\r\n"
					+ "\r\n" + "MODE 2:\r\n" + "\r\n"
					+ "Level 1. 当前依赖树中存在的多个版本的library都完全覆盖了自身项目期望调用的方法和属性的集合,不会引发运行时刻的异常行为。\r\n" + "\r\n"
					+ "Level 2. 加载到的library的版本没有完全覆盖自己的项目想要调用的方法和属性的集合，所以运行时刻的异常行为一定会发生。\r\n" + "\r\n" + "\r\n" + "\r\n"
					+ "MODE 3:\r\n" + "\r\n"
					+ "Level 1. 自身的项目没有直接或调用了由于依赖冲突而未被加载到的方法或属性，同时项目依赖的其他library也没有调用到那些未被加载到的方法或属性。\r\n"
					+ "         运行时刻的异常行为不会发生。\r\n" + "\r\n"
					+ "Level 2. 自身的项目没有直接或调用了由于依赖冲突而未被加载到的方法或属性，但是项目与冲突的library之间间隔的其他library却调用了到那些未被加载到的方法或属性。\r\n"
					+ "         运行时刻的异常行为不会发生，但是在版本演化过程中，有引发异常行为的风险。\r\n" + "\r\n"
					+ "Level 3. 自身的项目直接或调用了由于依赖冲突而未被加载到的方法或属性，运行时刻的异常行为一定会发生。\r\n");

			Element root = document.addElement("project");
			root.addAttribute("project",
					MavenUtil.i().getProjectGroupId() + ":" + MavenUtil.i().getProjectArtifactId());
			root.addAttribute("projectInfo", MavenUtil.i().getProjectInfo());
			for (Conflict conflict : Conflicts.i().getConflicts()) {
				root.add(PrintConflictRiskLevel(conflict, subdivisionLevel));
			}
			xmlWriter.write(document);
			xmlWriter.close();
		} catch (Exception e) {
			MavenUtil.i().getLog().error("can't write jar duplicate risk:", e);
		}
	}

	/**
	 * method:输出所有遍历方法的风险等级，无风险1/2，有风险3/4 author:wangchao time:2018-9-24 13:25:18
	 */
	private Element PrintConflictRiskLevel(Conflict conflict, boolean subdivisionLevel) {
		Element elements = new DefaultElement("conflicts");
		Element element = new DefaultElement("conflictJar");
		elements.add(element);
		element.addAttribute("groupId-artifactId", conflict.getSig());
		element.addAttribute("versions", conflict.getVersions().toString());
		ConflictJRisk conflictJRisk = conflict.getJRisk();
		int riskLevel = 0;
		String safeJar = "";

		Set<String> usedRiskMethods = null;
		if (Conf.fromHostSearch) {
			if (subdivisionLevel) {
				Map<Integer, String> result = conflictJRisk.getRiskLevel();
				riskLevel = result.keySet().iterator().next();
				safeJar = result.get(riskLevel);
			} else {
//			Set<String> usedRiskMethods = conflictJRisk.getConflictLevel();
//			Set<String> usedRiskMethods = conflictJRisk.getReachRiskMethods();
				usedRiskMethods = conflictJRisk.getConflictLevel();
				if (usedRiskMethods.isEmpty()) {
					riskLevel = 1;
					safeJar = conflict.getUsedDepJar().toString();
				} else {
					riskLevel = 3;
				}
			}
		} else {
			riskLevel = conflictJRisk.getReachRiskMethods();
		}
		element.addAttribute("riskLevel", riskLevel + "");

		element.add(AddPath(conflict));
		Element risksEle = element.addElement("RiskMethods");
		if (usedRiskMethods != null) {
			for (String method : usedRiskMethods) {
				Element riskMethod = new DefaultElement("RiskMethod");
				risksEle.add(riskMethod);
				riskMethod.addText(method.replace('<', ' ').replace('>', ' '));
			}
		}
		if (riskLevel == 1) {
			risksEle.addAttribute("tip", "jar was be referenced and be loaded !");
		} else if (riskLevel == 2) {
			risksEle.addAttribute("tip", "jar was be referenced and be loaded !");
		} else if (riskLevel == 3) {
			risksEle.addAttribute("tip", "methods would be referenced but not be loaded !");
		} else if (riskLevel == 4) {
			risksEle.addAttribute("tip", "methods would be referenced but not be loaded !");
		}

		Element safeJarElement = element.addElement("SafeJar");
		safeJarElement.addText(safeJar);
		return elements;
	}

	/**
	 * method:添加jar包path author:wangchao time:2018-9-23 13:30:09
	 */
	private Element AddPath(Conflict conflict) {
		Element elements = new DefaultElement("versions");
		// 冲突的jar包
		ConflictJRisk conflictJRisk = conflict.getJRisk();
		for (DepJarJRisk jarRisk : conflictJRisk.getJarRisks()) {
			Element element = new DefaultElement("version");
			elements.add(element);
			element.addAttribute("versionId", jarRisk.getVersion());
			element.addAttribute("loaded", "" + jarRisk.getConflictJar().isSelected());
			Element path = new DefaultElement("path");
			element.add(path);
			path.addText(jarRisk.getConflictJar().getAllDepPath());
		}
		return elements;
	}
}
