package eu.getsoftware.hotelico.hotel.infrastructure.service.impl;

import static eu.getsoftware.hotelico.clients.infrastructure.utils.ControllerUtils.convertToLocalDateTime;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.getsoftware.hotelico.chat.domain.ChatMessage;
import eu.getsoftware.hotelico.checkin.domain.CustomerHotelCheckin;
import eu.getsoftware.hotelico.clients.infrastructure.utils.ControllerUtils;
import eu.getsoftware.hotelico.customer.domain.CustomerRootEntity;
import eu.getsoftware.hotelico.customer.infrastructure.dto.CustomerDTO;
import eu.getsoftware.hotelico.customer.infrastructure.repository.CustomerRepository;
import eu.getsoftware.hotelico.customer.infrastructure.service.CustomerService;
import eu.getsoftware.hotelico.hotel.domain.HotelRootEntity;
import eu.getsoftware.hotelico.hotel.infrastructure.dto.WallPostDTO;
import eu.getsoftware.hotelico.hotel.infrastructure.repository.ChatRepository;
import eu.getsoftware.hotelico.hotel.infrastructure.repository.CheckinRepository;
import eu.getsoftware.hotelico.hotel.infrastructure.repository.HotelRepository;
import eu.getsoftware.hotelico.hotel.infrastructure.repository.WallPostRepository;
import eu.getsoftware.hotelico.hotel.infrastructure.service.CheckinService;
import eu.getsoftware.hotelico.hotel.infrastructure.service.HotelService;
import eu.getsoftware.hotelico.hotel.infrastructure.service.LastMessagesService;
import eu.getsoftware.hotelico.hotel.infrastructure.service.NotificationService;
import eu.getsoftware.hotelico.hotel.infrastructure.utils.HotelEvent;

/**
 * <br/>
 * Created by e.fanshil
 * At 03.02.2016 14:04
 */
@Service
public class CheckinServiceImpl implements CheckinService
{
	@Autowired
	private CustomerService customerService;		
	
	@Autowired
	private LastMessagesService lastMessagesService;	
			
	@Autowired
	private HotelService hotelService;	
		
	@Autowired
	private NotificationService notificationService;	
	
	@Autowired
	private CustomerRepository customerRepository;	
	
	@Autowired
	private HotelRepository hotelRepository;	
	
	@Autowired
	private WallPostRepository wallPostRepository;	
	
	@Autowired
	private CheckinRepository checkinRepository;	
	
	@Autowired
	private ChatRepository chatRepository;
	
	@Override
	public CustomerDTO updateCheckin(CustomerDTO customerDto) {
		
		CustomerRootEntity customerEntity = customerDto.getId()>0 ? customerRepository.getOne(customerDto.getId()) : null;
		
		customerDto = setCustomerCheckin(customerDto, customerEntity);
		
		return customerDto; //mapper.map(customerRepository.saveAndFlush(customer), CustomerDto.class);
	}
	
	@Override
	public CustomerDTO setCustomerCheckin(CustomerDTO customerDto, CustomerRootEntity customerEntity)
	{
		HotelRootEntity hotelRootEntity = null;
		
		if(customerEntity != null)
		{
			customerDto.setErrorResponse("");
			
			long virtualHotelId = lastMessagesService.getInitHotelId();
			
			String virtualHotelCode = ControllerUtils.ALLOW_INIT_VIRTUAL_HOTEL ? hotelRepository.getVirtualHotelCode() : null;
			
			boolean isFullCheckin = false;
			
			if(customerDto.getHotelCode()!=null && !(virtualHotelCode!=null && customerDto.getHotelCode().equals(virtualHotelCode) ))
			{
				hotelRootEntity = hotelRepository.findByCurrentHotelAccessCodeAndActive(customerDto.getHotelCode(), true);
				
				//Eugen: full checkin, wenn checked-in with Hotel-Code!
				isFullCheckin = (hotelRootEntity !=null);
			}
			if(hotelRootEntity ==null && customerDto.getHotelId()!=null && customerDto.getHotelId()>0 && (!ControllerUtils.ALLOW_INIT_VIRTUAL_HOTEL || customerDto.getHotelId() != virtualHotelId))
			{
				hotelRootEntity = hotelRepository.getOne(customerDto.getHotelId());
			}
			
			List<CustomerHotelCheckin> activeCustomerCheckins = checkinRepository.getActiveByCustomerId(customerEntity.getId(), new Date());
			
			boolean isDemoCheckin = hotelRootEntity != null && ControllerUtils.HOTEL_DEMO_CODE.equalsIgnoreCase(hotelRootEntity.getCurrentHotelAccessCode());
			boolean checkinDateIsValid = ControllerUtils.NO_CHECKOUT_FOR_DEMOHOTEL && isDemoCheckin || customerDto.getCheckinTo()!=null && customerDto.getCheckinTo().after(new Date());
			
			isFullCheckin = isFullCheckin || isDemoCheckin;// set id DTO getter! || ControllerUtils.CHECKIN_FULL_ALWAYS;
			
			//If checkin exists, 
			if(hotelRootEntity != null && !hotelRootEntity.isVirtual() && checkinDateIsValid)
			{

				Date lastSameHotelCheckin = checkinRepository.getLastByCustomerAndHotelId(customerEntity.getId(), hotelRootEntity.getId());

				customerDto.setHotelId(hotelRootEntity.getId());
				
				CustomerHotelCheckin nowGoodCheckin = null;
				
				//if no checkin exists, create a new one
				if(activeCustomerCheckins.isEmpty())
				{
					CustomerHotelCheckin customerHotelCheckin = new CustomerHotelCheckin();
					customerHotelCheckin.setCustomer(customerEntity);
					customerHotelCheckin.setHotel(hotelRootEntity);
					
					customerHotelCheckin.setStaffCheckin(customerEntity.isHotelStaff());
					customerHotelCheckin.setFullCheckin(isFullCheckin);
					customerDto.setFullCheckin(isFullCheckin);
					
					customerHotelCheckin.setValidFrom(customerDto.getCheckinFrom());
					customerHotelCheckin.setValidTo(customerDto.getCheckinTo());
					checkinRepository.saveAndFlush(customerHotelCheckin);
					nowGoodCheckin = customerHotelCheckin;
					
					customerDto.setErrorResponse("");
					
					//Inform others!!!
					if(!customerEntity.isHotelStaff() && !customerEntity.isAdmin())
					{
						customerDto.setHotelId(hotelRootEntity.getId());
						notificationService.notificateAboutEntityEvent(customerDto, HotelEvent.EVENT_CHECKIN, "New check-in in your hotel", hotelRootEntity.getId());
					}
					
					//sent wellcome message to new fullCheckin customers
					if(!customerEntity.isHotelStaff() && !customerEntity.isAdmin())
					{
						CustomerRootEntity staffSender = this.getStaffbyHotelId(hotelRootEntity.getId());
						
						if (staffSender!=null)
						{
							//TODO EUGEN: Check here if I already sent a message to him
							// ### SEND WELLCOME MESSAGE
							
							ChatMessage lastMessageFromStaff = chatRepository.getLastMessageByCustomerAndReceiverIds(staffSender.getId(), customerEntity.getId());
							
							//Send only first staffSender message!    
							if(lastMessageFromStaff==null)
							{
								ChatMessage hiMessage = new ChatMessage();
								
								String wellcomeMsg = "Hi, welcome to our Hotel! Please write me, if you need something";
								String wellcomeGuestMsg = "Hi, welcome to thr guest view of our Hotel! Please get the hotel-code at the reception - without the hotel-code, you are not listed as a hotel guest, and you can not view the customers in the wall... ";
								
								if("de".equalsIgnoreCase(customerEntity.getEntityAggregate().getPrefferedLanguage()))
								{
									wellcomeMsg = "Hallo, herzlich willkommen im Hotel! Bitte schreiben Sie mir, wenn Sie etwas brauchen";
									wellcomeGuestMsg = "Hallo, herzlich willkommen im Hotel Gast-Zugang! Bitte bekommen Sie den Zugang-Kode an der Rezeption. Ohne Hotel-Kode sind ihre Aktivitäten in Hotel beschränkt";
								}
								
								hiMessage.setMessage(isFullCheckin? wellcomeMsg : wellcomeGuestMsg);
								
								hiMessage.setSender(staffSender);
								hiMessage.setReceiver(customerEntity);
								hiMessage.setInitId(new Date().getTime());
								hiMessage.setTimestamp(new Timestamp(new Date().getTime()));
								chatRepository.save(hiMessage);
							}
							
							// ### Notificate Wall
							if(lastSameHotelCheckin==null || convertToLocalDateTime(lastSameHotelCheckin).getDayOfYear() != LocalDateTime.now().getDayOfYear() )
							{

								WallPostDTO checkinNotificationWallDto = new WallPostDTO();

								checkinNotificationWallDto.getSpecialContent().put("customerId", String.valueOf(customerEntity.getId()));
								checkinNotificationWallDto.setHotelId(hotelRootEntity.getId());
								checkinNotificationWallDto.setSenderId(staffSender.getId());
//								checkinNotificationWallDto.setsetValidUntil(new DateTime().plusDays(1).toDate());
								checkinNotificationWallDto.setInitId(new Date().getTime());
								checkinNotificationWallDto.setMessage("New guest at our hotel:");

								hotelService.addUpdateWallPost(checkinNotificationWallDto);
								
							}
							
						}
					}
					
				}
				else //correct actual checkin, if the same hotel
				{
					//EUGEN: only update checkin, no event
					
					for (CustomerHotelCheckin nextCheckin : activeCustomerCheckins)
					{
						if(hotelRootEntity.equals(nextCheckin.getHotel())) //if the checkin of actual hotel, NO HOTEL EVENT!
						{
							//update the values of checkin
							nextCheckin.setValidFrom(customerDto.getCheckinFrom());
							nextCheckin.setValidTo(customerDto.getCheckinTo());
							nextCheckin.setStaffCheckin(customerEntity.isHotelStaff());
							nextCheckin.setFullCheckin(isFullCheckin);
							checkinRepository.saveAndFlush(nextCheckin);
							customerDto.setErrorResponse("");
							nowGoodCheckin = nextCheckin;
							break;
						}
					}
				}
				
				long consistencyId = new Date().getTime();
				customerEntity.getEntityAggregate().setConsistencyId(consistencyId);
				
				lastMessagesService.updateCustomerConsistencyId(customerEntity.getId(), consistencyId);
				
				customerDto = customerService.convertCustomerToDto(customerRepository.saveAndFlush(customerEntity), true, nowGoodCheckin);
				
			}
			else{ //if no hotel more, maybe cancel actual checkin
				
				for (CustomerHotelCheckin nextCheckin : activeCustomerCheckins)
				{
					nextCheckin.setActive(false);
					
					//                    if(nextCheckin.getCustomer().getSeenActivities()!=null)
					//                        nextCheckin.getCustomer().getSeenActivities().clear();
					
					customerRepository.save(nextCheckin.getCustomer());
					
					notificationService.notificateAboutEntityEvent(customerDto, HotelEvent.EVENT_CHECKOUT, "Checkout from your hotel", nextCheckin.getHotel().getId());
					
					checkinRepository.saveAndFlush(nextCheckin);
				}
				
				Long wantedHotelId = customerDto.getHotelId();
				
				long consistencyId = new Date().getTime();
				customerEntity.getEntityAggregate().setConsistencyId(consistencyId);
				lastMessagesService.updateCustomerConsistencyId(customerEntity.getId(), consistencyId);
				
				customerDto = customerService.convertMyCustomerToFullDto(customerRepository.saveAndFlush(customerEntity));
				
				if(wantedHotelId>0 && (customerDto.getCheckinTo()==null || customerDto.getCheckinTo().before(new Date()))) {
					customerDto.setErrorResponse("Checkin Date is wrong or in past");
				}
				else
				{
					if(wantedHotelId>0)
					{
						customerDto.setErrorResponse("Hotel wurde nicht gefunden");
					}
					
					if(wantedHotelId == virtualHotelId && ControllerUtils.ALLOW_INIT_VIRTUAL_HOTEL)
					{
						customerDto.setHotelId(virtualHotelId);
						customerDto.setFullCheckin(true);
					}
				}
			}
			
			//Eugen: every time update customer current hotelId!!!
			lastMessagesService.updateCustomerHotelId(customerDto.getId(), customerDto.getHotelId());
			
			
			
			return customerDto;
			
		}
		
		return null;
	}
	
	@Override
	public CustomerRootEntity getStaffbyHotelId(long hotelId)
	{
		CustomerRootEntity staff = null;
	 
		List<CustomerRootEntity> hotelStaffs = checkinRepository.getStaffByHotelId(hotelId);

		Iterator<CustomerRootEntity> staffIterator = hotelStaffs.iterator();
		
		if (staffIterator.hasNext())
		{
			staff = staffIterator.next();
		}
		
		return staff;
	}
	

	@Override
	public CustomerDTO updateOwnDtoCheckinInfo(CustomerDTO dto, CustomerHotelCheckin validCheckin)
	{
		if(dto==null || dto.getId()<=0){
			return  null;
		}
		
		//Reset dto only if consistencyId too old!!!
		//        dto = synchronizeCustomerToDto(dto);
		
		List<CustomerHotelCheckin> activeCheckins = validCheckin!=null ? new ArrayList<CustomerHotelCheckin>() : checkinRepository.getActiveByCustomerId(dto.getId(), new Date());
		
		//        CustomerHotelCheckin validCheckin = null;
		
		if(!activeCheckins.isEmpty() || validCheckin!=null) //exists active checkin
		{
			Iterator<CustomerHotelCheckin> iterator = activeCheckins.iterator();
			
			while(iterator.hasNext() && validCheckin==null)
			{
				CustomerHotelCheckin customerCheckin = iterator.next();
				
				//If checkin is old, set it not active
				if(new Date().after(customerCheckin.getValidTo()))
				{
					customerCheckin.setActive(false);
					checkinRepository.saveAndFlush(customerCheckin);
				}
				else
				{
					validCheckin = customerCheckin;
					break;
				}
			}
		}
		
		if(validCheckin!=null) // if checkin exists and valid
		{
			CustomerRootEntity checkinCustomerRootEntity = validCheckin.getCustomer();
			
			//TODO Eugen: cannot set logged automaticly
			//TODO Eugen: checkinCustomerRootEntity.setActive(true)? update entity?
			//SETTING NEW CHECKIN
			//checkinCustomerRootEntity.setActive(true);
			dto.setActive(true);
			
			dto.setFullCheckin(validCheckin.isFullCheckin());
			
			dto.setCheckinFrom(validCheckin.getValidFrom());
			dto.setCheckinTo(validCheckin.getValidTo());
			
			dto = customerService.fillDtoWithHotelInfo(dto, validCheckin);
		}
		
		if(validCheckin==null)
		{
			long initHotelId = lastMessagesService.getInitHotelId();
			
			if(initHotelId>0)
			{
				dto.setHotelId(initHotelId);
				dto.setFullCheckin(true);
			}
		}
		
		return dto;
	}
}
