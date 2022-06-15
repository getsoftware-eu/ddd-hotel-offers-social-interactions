package eu.getsoftware.hotelico.hotel.service;

import org.springframework.transaction.annotation.Transactional;

import eu.getsoftware.hotelico.hotel.dto.CustomerDTO;
import eu.getsoftware.hotelico.hotel.dto.ResponseDTO;
import eu.getsoftware.hotelico.hotel.dto.CustomerDTO;
import eu.getsoftware.hotelico.hotel.dto.ResponseDTO;
import eu.getsoftware.hotelico.hotel.model.Customer;
import eu.getsoftware.hotelico.hotel.model.Customer;

/**
 * <br/>
 * Created by e.fanshil
 * At 03.02.2016 13:59
 */
public interface LoginHotelicoService
{
	@Transactional CustomerDTO checkLogin(String email, String password);
	
	@Transactional
	void logoutCustomer(CustomerDTO customerDto);
	
	@Transactional CustomerDTO resetPassword(String email, String resetCode);
	
	@Transactional ResponseDTO requestPasswordReset(String email);
	
	@Transactional
	void setLogged(long customerId, boolean logged);
	
	long getCryptoHash(Customer customer, String initialPassword);
	
	@Transactional CustomerDTO checkBeforeLoginProperties(CustomerDTO loggingCustomer, CustomerDTO dbCustomer);
}