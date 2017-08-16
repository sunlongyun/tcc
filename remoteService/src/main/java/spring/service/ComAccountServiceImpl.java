package spring.service;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.stereotype.Service;

import spring.beans.DataSourceUtil;


@Service("comAccountService")
public class ComAccountServiceImpl implements ComAccountService, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5477171172824044513L;


	public  void preTransferIn(String comName, double amount, String orderNo) throws Exception{
		Connection con = getCon();
		try {
			con.setAutoCommit(false);
			/**
			 * 订单已经存在，则直接返回成功，本次操作可能是重复操作
			 */
			PreparedStatement pre =  con.prepareStatement("select 1 from order_detail where orderNo =? and direction = 1");
			pre.setString(1, orderNo);
			ResultSet res =  pre.executeQuery();
			if(!res.next()){
				pre = con.prepareStatement("insert into order_detail(orderNo, amount, status, direction) values(?, ?, ?, ?)");
				pre.setString(1, orderNo);
				pre.setDouble(2, amount);
				pre.setInt(3, 1);
				pre.setInt(4, 1);
				int count = pre.executeUpdate();
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

	public   void   commitTransferIn(String comName, double amount, String orderNo) throws Exception{
		Connection con = getCon();
		try {
			con.setAutoCommit(false);
			/**
			 * 找不到符合条件的订单，则直接返回成功(本次操作可能是重复操作，订单已经被处理)
			 */
			PreparedStatement pre = con.prepareStatement("update order_detail set status = 2 where orderNo= ? and status = 1");
			pre.setString(1, orderNo);
			int count = pre.executeUpdate();
			if(count > 0){
				pre = con.prepareStatement("update account set amount = amount + ? where companyName = ? ");
				pre.setDouble(1, amount);
				pre.setString(2, comName);
				count = pre.executeUpdate();
				if(count <= 0){
					throw new RuntimeException("操作失败!");
				}
//				DataSourceUtil.testRandomException();
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


	public  void rollBackPreTransferIn(String comName, double amount, String orderNo) throws Exception{
		Connection con = getCon();
		try {
			con.setAutoCommit(false);
			PreparedStatement pre = con.prepareStatement("update order_detail set status=0 where orderNo = ? and status = 1");
			pre.setString(1, orderNo);
			pre.executeUpdate();
			con.commit();
		} catch (Exception e) {
			try {
				con.rollback();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			throw new RuntimeException(e);
		}finally {
			DataSourceUtil.removeCon();
		}
		
	}

	public  void rollBackcommitTransferIn(String comName, double amount, String orderNo) throws Exception{
		Connection con = getCon();
		try {
			con.setAutoCommit(false);
			/**
			 * 找不到符合条件的订单，则直接返回成功。（本次操作可能是重复操作，订单已经被处理）
			 */
			PreparedStatement pre = con.prepareStatement("update order_detail set status=0 where orderNo = ?  and status =2");
			pre.setString(1, orderNo);
			int count  = pre.executeUpdate();
			if(count >0){
				pre = con.prepareStatement("update account set amount = amount - ? where companyName = ?");
				pre.setDouble(1, amount);
				pre.setString(2, comName);
				count = pre.executeUpdate();
				if(count <= 0){
					throw new RuntimeException("操作失败");
				}
			}
			con.commit();
		} catch (Exception e) {
			try {
				con.rollback();
			} catch (Exception e1) {
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
			/**
			 * 查询订单在本系统的状态
			 */
			PreparedStatement pre = con.prepareStatement("select status from  order_detail where orderNo = ?");
			pre.setString(1, orderNo);
			ResultSet res =  pre.executeQuery();
			if(res.next()){
				return res.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
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
		return DataSourceUtil.getCon("comDataSource");
	}
}
