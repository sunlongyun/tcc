package spring.tcc.service;

import java.io.Serializable;
import java.util.Map;


public interface Tcc extends Serializable{
	/**
	 * 预留业务资源
	 * 作者：孙龙云
	 * 时间：2017年8月8日 下午3:04:43
	 * @param params
	 */
	public void tryAction(Map<String, Object> params) throws Exception;
	/**
	 * 提交业务
	 * 作者：孙龙云
	 * 时间：2017年8月8日 下午3:05:41
	 * @param params
	 */
	public void commitAction(Map<String, Object> params) throws Exception;
	/**
	 * 回滚事务
	 * 作者：孙龙云
	 * 时间：2017年8月8日 下午3:07:06
	 * @param params
	 */
	public void rollbackAction(Map<String, Object> params) throws Exception;
	
}
