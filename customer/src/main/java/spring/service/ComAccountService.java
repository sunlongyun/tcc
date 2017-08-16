package spring.service;

import java.io.Serializable;

/**
 * 企业账户管理
 * 类描述：
 * 创建人：孙龙云
 * 创建时间：2017年8月9日 下午4:24:14
 */
public interface ComAccountService extends Serializable{
	/**
	 * 转入金额预处理
	 * 作者：孙龙云
	 * 时间：2017年8月9日 下午1:39:14
	 * @param amount
	 * @param orderNo
	 */
	public void preTransferIn(String comName, double amount, String orderNo) throws Exception;
	/**
	 * 转入金额提交
	 * 作者：孙龙云
	 * 时间：2017年8月9日 下午1:56:31
	 * @param amount
	 * @param orderNo
	 */
	public void commitTransferIn(String comName, double amount, String orderNo) throws Exception;
	/**
	 * 回滚转入预处理
	 * 作者：孙龙云
	 * 时间：2017年8月9日 下午1:58:29
	 * @param amount
	 * @param orderNo
	 */
	public void rollBackPreTransferIn(String comName, double amount, String orderNo) throws Exception;
	/**
	 * 回滚转入金额已提交
	 * 作者：孙龙云
	 * 时间：2017年8月9日 下午1:55:56
	 * @param amount
	 * @param orderNo
	 */
	public void rollBackcommitTransferIn(String comName, double amount, String orderNo) throws Exception;
	/**
	 * 获取订单状态
	 * 作者：孙龙云
	 * 时间：2017年8月9日 下午3:56:27
	 * @param orderNo
	 * @return
	 */
	public int getStatus(String orderNo) throws Exception;
}
