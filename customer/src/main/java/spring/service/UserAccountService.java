package spring.service;

import java.io.Serializable;

public interface UserAccountService extends Serializable{
	/**
	 * 转出金额预处理
	 * 作者：孙龙云
	 * 时间：2017年8月9日 下午1:38:24
	 * @param amount
	 * @param orderNo
	 */
	public void preTransferOut(String userName, double amount, String orderNo) throws Exception;
	/**
	 * 转出金额提交
	 * 作者：孙龙云
	 * 时间：2017年8月9日 下午1:55:56
	 * @param amount
	 * @param orderNo
	 */
	public void commitTransferOut(String userName, double amount, String orderNo) throws Exception;
	/**
	 * 回滚转出预处理
	 * 作者：孙龙云
	 * 时间：2017年8月9日 下午1:58:29
	 * @param amount
	 * @param orderNo
	 */
	public void rollBackPreTransferOut(String userName, double amount, String orderNo) throws Exception;
	/**
	 * 回滚转出金额已提交
	 * 作者：孙龙云
	 * 时间：2017年8月9日 下午1:55:56
	 * @param amount
	 * @param orderNo
	 */
	public void rollBackcommitTransferOut(String userName, double amount, String orderNo) throws Exception;
	/**
	 * 获取订单状态
	 * 作者：孙龙云
	 * 时间：2017年8月9日 下午3:56:27
	 * @param orderNo
	 * @return
	 */
	public int getStatus(String orderNo) throws Exception;
}
