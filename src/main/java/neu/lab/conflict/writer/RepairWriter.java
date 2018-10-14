package neu.lab.conflict.writer;

import neu.lab.conflict.container.Conflicts;
import neu.lab.conflict.risk.jar.ConflictJRisk;
import neu.lab.conflict.vo.Conflict;

public class RepairWriter {

	public void write() {
		for (Conflict conflict : Conflicts.i().getConflicts()) {
			ConflictJRisk conflictJRisk = conflict.getJRisk();
			System.out.println(conflictJRisk.getRiskLevel());
		}
	}
}
