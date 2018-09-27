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
			Element elements = new DefaultElement("conflicts");
			//int i = 0;
			
			for (Conflict conflict : Conflicts.i().getConflicts()) {
				
				Element element = new DefaultElement("conflictJar");
				elements.add(element);
				element.addAttribute("groupId-artifactId", conflict.getSig());
				element.addAttribute("versions", conflict.getVersions().toString());
				element.addAttribute("riskLevel", "3/4");
				element.add(PrintRiskMethod(conflict));
				Element risksEle = element.addElement("RiskMethods");
				risksEle.addAttribute("tip", "method that may be used but will not be loaded !");
				//element.add(PrintRiskLevel());
				//i++;
				
				//root.add(PrintRiskMethod(conflict));
			}
			//root.addAttribute("i", ""+i);
			root.add(elements);
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
	private Element PrintRiskLevel() {
		Element elements = new DefaultElement("NoRiskMethods");
		for (NodeAdapter nodeAdapter : NodeAdapters.i().getAllNodeAdapter()) {
			elements.addText(nodeAdapter.toString());
			
		}
		for (DepJar depJar : DepJars.i().getAllDepJar()) {
			elements.addText(depJar.toString());
			
		}
		return elements;
	}
	/*
	 * method:实现输出3/4级别的风险方法
	 * author:wangchao
	 * time:2018-9-23 13:30:09
	 */
	private Element PrintRiskMethod(Conflict conflict) {
		Element elements = new DefaultElement("versions");
		//冲突的jar包
		ConflictJRisk conflictJRisk = conflict.getJRisk();
		for(DepJarJRisk jarRisk : conflictJRisk.getJarRisks()) {
			Graph4distance distanceGraph = jarRisk.getGraph4distance();

			Graph4path pathGraph = distanceGraph.getGraph4path();
			if (distanceGraph.getAllNode().isEmpty()) {
				
			}
			else {
				//element.addAttribute("distance", distanceGraph);
			}
			if (pathGraph.getAllNode().isEmpty()) {
				
			}
			else {
				Set<String> hostNodes = pathGraph.getHostNds();
				Map<String, IBook> books = new Dog(pathGraph).findRlt(hostNodes, Conf.DOG_DEP_FOR_PATH,
						Strategy.NOT_RESET_BOOK);
				for (String topMthd : books.keySet()) {
					if (hostNodes.contains(topMthd)) {
						Book4path book = (Book4path) books.get(topMthd);
						//DepJar usedJar =  conflict.getUsedDepJar();
						for (IRecord iRecord : book.getRecords()) {
							Element element = new DefaultElement("version");
							elements.add(element);
							Record4path record = (Record4path) iRecord;
//							element.addAttribute("conflict",jarRisk.getVersion() + conflict.toString() + conflict.getUsedDepJar().getVersion());
//							element.addAttribute("record", record.toString());
							element.addAttribute("versionId", jarRisk.getVersion());
							element.addAttribute("loaded", "" + jarRisk.getConflictJar().isSelected());
//							element.addAttribute("methodName", record.getRiskMthd().toString().replace("<", "").replace(">", ""));
							Element path = new DefaultElement("path");
							element.add(path);
							path.addText(record.getPathStr().toString().replace("<", "").replace(">", ""));
							
//							element.addAttribute("level", "3/4");
//							element.addText(record.getPathStr().toString().replace("<", "").replace(">", ""));
							
//								}
//							}
							//usedJar错误的jar包
							//usedJar.getOutMthds(record.get)
							
							//dis2records.add(record.getPathlen(), record);
						}
					}
				}
			}
		}
		return elements;
	}
}
