package spring.tcc.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import spring.service.ComAccountService;
import spring.service.UserAccountService;

@Service
public class TccServiceImpl implements Tcc {
	@Autowired
	private ComAccountService comAccountService;
	@Autowired
	private UserAccountService userAccountService;
	private static final long serialVersionUID = -3628880011929124453L;

	@Override
	public void tryAction(Map<String, Object> params) throws Exception {
		String orderNo = (String) params.get("orderNo");
		String comName = (String) params.get("comName");
		String userName = (String) params.get("userName");
		Double amount = (Double) params.get("amount");
		try{
			comAccountService.preTransferIn(comName, amount, orderNo);
			userAccountService.preTransferOut(userName, amount, orderNo);
			
		}catch (Exception e) {
			System.out.println("tryAction---------"+e.getMessage());
			throw e;
		}
	}
	
	@Override
	public void rollbackAction(Map<String, Object> params) throws Exception {
		String orderNo = (String) params.get("orderNo");
		String comName = (String) params.get("comName");
		String userName = (String) params.get("userName");
		Double amount = (Double) params.get("amount");
		try{
			int comOrderStatus = comAccountService.getStatus(orderNo);
			if(1 == comOrderStatus){
				comAccountService.rollBackPreTransferIn(comName, amount, orderNo);
			}else if(2 == comOrderStatus){
				comAccountService.rollBackcommitTransferIn(comName, amount, orderNo);
			}
			int userOrderStatus = userAccountService.getStatus(orderNo);
			if(1 == userOrderStatus){
				userAccountService.rollBackPreTransferOut(userName, amount, orderNo);
			}else if(2 == userOrderStatus){
				userAccountService.rollBackcommitTransferOut(userName, amount, orderNo);
			}
		}catch(Exception e){
			System.out.println("rollbackAction---------"+e.getMessage());
			throw e;
		}
		
	}
	
	@Override
	public void commitAction(Map<String, Object> params) throws Exception {
		String orderNo = (String) params.get("orderNo");
		String comName = (String) params.get("comName");
		String userName = (String) params.get("userName");
		Double amount = (Double) params.get("amount");
		try{
			comAccountService.commitTransferIn(comName, amount, orderNo);
			userAccountService.commitTransferOut(userName, amount, orderNo);
		}catch (Exception e) {
			System.out.println("commitAction-----------"+e.getMessage());
			throw e;
		}
	}

}
