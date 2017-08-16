package spring.tcc.beans;

import java.io.Serializable;
import java.util.Map;

import spring.tcc.service.Tcc;

public class OrderItem implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3994168703063589675L;
	private Map<String, Object> params;
	private Class<? extends Tcc> className;
	
	
	public Map<String, Object> getParams() {
		return params;
	}
	public void setParams(Map<String, Object> params) {
		this.params = params;
	}
	public Class<? extends Tcc> getClassName() {
		return className;
	}
	public void setClassName(Class<? extends Tcc> className) {
		this.className = className;
	}
	
	public OrderItem(Map<String, Object> params, Class<? extends Tcc> className) {
		this.params = params;
		this.className = className;
	}
}
