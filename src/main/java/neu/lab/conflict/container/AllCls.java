package neu.lab.conflict.container;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import neu.lab.conflict.vo.DepJar;

/**
 * @author asus
 *FinalClasses is set of ClassVO,but AllCls is set of class signature.
 *FinalClasses是ClassVO的集合，但AllCls是类签名的集合。
 */
public class AllCls {
	private static AllCls instance;
	private Set<String> clses;

	public static void init(DepJars depJars) {
		if (instance == null) {
			instance = new AllCls(depJars);
		}
	}
	public static AllCls i() {
		return instance;
	}
	
	//构造函数
	private AllCls(DepJars depJars){
		clses = new HashSet<String>();
		for (DepJar depJar : depJars.getAllDepJar()) {
			if (depJar.isSelected()) {
				//得到depJar中所有的类
				clses.addAll(depJar.getAllCls(true));
			}
		}
	}
	
	public Set<String> getAllCls() {
		return clses;
	}
	
	//clses是否包含sls
	public boolean contains(String cls) {
		return clses.contains(cls);
	}
	
	//得到不在本类集合中的类
	public Set<String> getNotInClses(Collection<String> testSet){
		Set<String> notInClses = new HashSet<String>();
		for(String cls:testSet) {
			if(!this.contains(cls)) {
				notInClses.add(cls);
			}
		}
		return notInClses;
	}
}
