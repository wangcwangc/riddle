package neu.lab.conflict.writer;

import java.io.CharArrayWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultElement;

import neu.lab.conflict.container.Conflicts;
import neu.lab.conflict.container.DepJars;
import neu.lab.conflict.container.NodeAdapters;
import neu.lab.conflict.graph.Book4path;
import neu.lab.conflict.graph.Dog;
import neu.lab.conflict.graph.Graph4distance;
import neu.lab.conflict.graph.Graph4path;
import neu.lab.conflict.graph.IBook;
import neu.lab.conflict.graph.IRecord;
import neu.lab.conflict.graph.Record4path;
import neu.lab.conflict.graph.Dog.Strategy;
import neu.lab.conflict.risk.jar.ConflictJRisk;
import neu.lab.conflict.risk.jar.DepJarJRisk;
import neu.lab.conflict.util.Conf;
import neu.lab.conflict.util.MavenUtil;
import neu.lab.conflict.vo.Conflict;
import neu.lab.conflict.vo.DepJar;
import neu.lab.conflict.vo.NodeAdapter;

public class RiskLevelWriter {

	public void writeRiskLevel(String outPath, boolean append) {
		try {
			Writer fileWriter;
			if (outPath == null) {
				fileWriter = new CharArrayWriter();
			} else {
				fileWriter = new FileWriter(outPath, append);
			}
			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setNewlines(true);
			XMLWriter xmlWriter = new XMLWriter(fileWriter, format);
			Document document = DocumentHelper.createDocument();
			Element root = document.addElement("project");
			root.addAttribute("project",
					MavenUtil.i().getProjectGroupId() + ":" + MavenUtil.i().getProjectArtifactId());
			root.addAttribute("projectInfo", MavenUtil.i().getProjectInfo());
			for (Conflict conflict : Conflicts.i().getConflicts()) {
				root.add(PrintRiskLevel(conflict));
			}
			xmlWriter.write(document);
			xmlWriter.close();
		} catch (Exception e) {
			MavenUtil.i().getLog().error("can't write jar duplicate risk:", e);
		}
	}
	/*
	 * method:输出所有遍历方法的风险等级，无风险1/2，有风险3/4
	 * author:wangchao
	 * time:2018-9-24 13:25:18
	 */
	private Element PrintRiskLevel(Conflict conflict) {
		Element elements = new DefaultElement("conflicts");
		Element element = new DefaultElement("conflictJar");
		elements.add(element);
		element.addAttribute("groupId-artifactId", conflict.getSig());
		element.addAttribute("versions", conflict.getVersions().toString());
		ConflictJRisk conflictJRisk = conflict.getJRisk();
		int riskLevel = conflictJRisk.getRiskLevel();
		element.addAttribute("riskLevel", riskLevel + "");
		
		element.add(AddPath(conflict));
		Element risksEle = element.addElement("RiskMethods");
		if (riskLevel == 3) {
			risksEle.addAttribute("tip", "method that may be used but will not be loaded !");
		} else {
			risksEle.addAttribute("tip", "method that be used and be loaded !");
		}
		return elements;
	}
	/*
	 * method:添加jar包path
	 * author:wangchao
	 * time:2018-9-23 13:30:09
	 */
	private Element AddPath(Conflict conflict) {
		Element elements = new DefaultElement("versions");
		//冲突的jar包
		ConflictJRisk conflictJRisk = conflict.getJRisk();
		for(DepJarJRisk jarRisk : conflictJRisk.getJarRisks()) {
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
