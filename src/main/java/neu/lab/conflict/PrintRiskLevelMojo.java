package neu.lab.conflict;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import neu.lab.conflict.writer.RiskLevelWriter;

@Mojo(name = "printRiskLevel", defaultPhase = LifecyclePhase.VALIDATE)
public class PrintRiskLevelMojo extends ConflictMojo {

	@Override
	public void run() {
		// TODO Auto-generated method stub
		RiskLevelWriter riskLevelWriter = new RiskLevelWriter();
		riskLevelWriter.writeRiskLevel("/Users/wangchao/Develop/neu/RiskLevel.xml", append);	//mac下路径
		//riskLevelWriter.writeRiskLevel("D:\\riddle\\RiskLevel.xml", append);	//win下路径
	}

}
