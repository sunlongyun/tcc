package spring.service;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.stereotype.Service;

import spring.beans.DataSourceUtil;

@Service("userAccountService")
public class UserAccountServiceImpl implements UserAccountService,Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8917070261651528411L;
	public  void preTransferOut(String userName, double amount, String orderNo) throws Exception{
		Connection con = getCon();
		try {
			con.setAutoCommit(false);
			/**
			 * 如果该订单已经存在，则说明本次操作为重复操作，后面逻辑无需处理，直接返回操作成功
			 */
			PreparedStatement pre =  con.prepareStatement("select 1 from order_detail where orderNo = ? and direction = 2");
			pre.setString(1, orderNo);
			ResultSet res =  pre.executeQuery();
			if(!res.next()){
				pre = con.prepareStatement("insert into order_detail(orderNo, amount, status, direction) values(?, ?, ?, ?)");
				pre.setString(1, orderNo);
				pre.setDouble(2, amount);
				pre.setInt(3, 1);
				pre.setInt(4, 2);
				pre.executeUpdate();

				pre = con.prepareStatement("update account set amount = amount - ? ,freezeAmount = freezeAmount + ? where userName = ? and amount >= ?");
				pre.setDouble(1, amount);
				pre.setDouble(2, amount);
				pre.setString(3, userName);
				pre.setDouble(4, amount);
				int count = pre.executeUpdate();
				
				if(count <= 0 ){
					throw new RuntimeException("账户冻结失败");
				}
			}
			con.commit();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				con.rollback();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			throw new RuntimeException(e);
		}finally {
			DataSourceUtil.removeCon();
		}
		
	}


	public  void commitTransferOut(String userName, double amount, String orderNo) throws Exception{
		Connection con = getCon();
		try {
			con.setAutoCommit(false);
			/**
			 * 找不到符合条件的订单，则直接返回成功。（本次操作可能是重复操作，订单已经被处理过）
			 */
			PreparedStatement pre = con.prepareStatement("update order_detail set status = 2 where orderNo= ? and status = 1");
			pre.setString(1, orderNo);
			int count = pre.executeUpdate();
			if(count > 0){
				pre = con.prepareStatement("update account set freezeAmount = freezeAmount - ? where userName = ? and freezeAmount >= ?");
				pre.setDouble(1, amount);
				pre.setString(2, userName);
				pre.setDouble(3, amount);
				count = pre.executeUpdate();
				if(count <= 0){
					throw new RuntimeException("操作失败!");
				}
				pre = con.prepareStatement("select freezeAmount from account where userName = ?");
				pre.setString(1, userName);
				ResultSet res =  pre.executeQuery();
				if(!res.next()){
					throw new RuntimeException("操作失败!");
				}
				DataSourceUtil.testException();
				DataSourceUtil.testRandomException();
				con.commit();
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				con.rollback();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			throw new RuntimeException(e);
		}finally {
			DataSourceUtil.removeCon();
		}
	}

	

	public  void rollBackPreTransferOut(String userName, double amount, String orderNo) throws Exception{
		Connection con = getCon();
		try {
			con.setAutoCommit(false);
			/**
			 * 找不到符合条件的订单，则直接返回成功。（本次操作可能是重复操作，订单已经被处理过）
			 */
			PreparedStatement pre = con.prepareStatement("update order_detail set status=0 where orderNo = ? and status = 1 ");
			pre.setString(1, orderNo);
			int count  = pre.executeUpdate();
			if(count > 0){
				pre = con.prepareStatement("update account set freezeAmount = freezeAmount - ?, amount = amount + ? where userName = ? and freezeAmount >= ?");
				pre.setDouble(1, amount);
				pre.setDouble(2, amount);
				pre.setString(3, userName);
				pre.setDouble(4, amount);
				count = pre.executeUpdate();
				if(count <= 0){
					throw new RuntimeException("操作失败");
				}
			}
			con.commit();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				con.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw new RuntimeException(e);
		}finally {
			DataSourceUtil.removeCon();
		}
	}


	public  void rollBackcommitTransferOut(String userName, double amount, String orderNo) throws Exception{
		Connection con = getCon();
		try {
			con.setAutoCommit(false);
			/**
			 * 找不到符合条件的订单，则直接返回成功。（本次操作可能是重复操作，订单已经被处理）
			 */
			PreparedStatement pre = con.prepareStatement("update order_detail set status=0 where orderNo = ? and status = 2 ");
			pre.setString(1, orderNo);
			int count  = pre.executeUpdate();
			if(count >0){
				pre = con.prepareStatement("update account amount+= ? where userName=? ");
				pre.setDouble(1, amount);
				pre.setString(2, userName);
				count = pre.executeUpdate();
				if(count <= 0){
					throw new RuntimeException("操作失败");
				}
			}
			con.commit();
		} catch (Exception e) {
			try {
				con.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			throw new RuntimeException(e);
		}finally {
			DataSourceUtil.removeCon();
		}
	}

	
	public  int getStatus(String orderNo) throws Exception{
		Connection con = getCon();
		try {
			PreparedStatement pre = con.prepareStatement("select status from  order_detail where orderNo = ?");
			pre.setString(1, orderNo);
			ResultSet res =  pre.executeQuery();
			if(res.next()){
				return res.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			DataSourceUtil.removeCon();
		}
		return -1;
	}
	/**
	 * 获取数据库连接
	 * @return
	 */
	private Connection getCon(){
		return DataSourceUtil.getCon("userDataSource");
	}
}
