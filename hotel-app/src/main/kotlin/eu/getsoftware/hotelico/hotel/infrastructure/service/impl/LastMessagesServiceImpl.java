package eu.getsoftware.hotelico.hotel.infrastructure.service.impl;

import static eu.getsoftware.hotelico.clients.infrastructure.utils.ControllerUtils.convertToDate;
import static eu.getsoftware.hotelico.clients.infrastructure.utils.ControllerUtils.convertToLocalDateTime;

import java.awt.geom.Point2D.Double;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;

import eu.getsoftware.hotelico.chat.domain.ChatMessage;
import eu.getsoftware.hotelico.clients.infrastructure.utils.ControllerUtils;
import eu.getsoftware.hotelico.customer.domain.CustomerRootEntity;
import eu.getsoftware.hotelico.customer.infrastructure.dto.CustomerDTO;
import eu.getsoftware.hotelico.customer.infrastructure.repository.CustomerRepository;
import eu.getsoftware.hotelico.customer.infrastructure.service.CustomerService;
import eu.getsoftware.hotelico.hotel.infrastructure.dto.CustomerNotificationDTO;
import eu.getsoftware.hotelico.hotel.infrastructure.repository.ChatRepository;
import eu.getsoftware.hotelico.hotel.infrastructure.repository.CheckinRepository;
import eu.getsoftware.hotelico.hotel.infrastructure.repository.HotelRepository;
import eu.getsoftware.hotelico.hotel.infrastructure.service.LastMessagesService;

/**
 * <br/>
 * Created by e.fanshil
 * At 16.10.2015 11:17
 */
@Service
public class LastMessagesServiceImpl implements LastMessagesService
{
	@Autowired
	private CustomerService customerService;	
	
	@Autowired
	private CheckinRepository checkinRepository;

	@Autowired
	private HotelRepository hotelRepository;
	
	@Autowired
	private ChatRepository chatRepository;

	@Autowired
	private CustomerRepository customerRepository;
	
	private Map<Long, Date> lastCustomerOnlineMap = new HashMap<>();
	private Map<Long, Long> currentConsistencyIdsMap = new HashMap<>();
	
	private Map<Long, Map<Long, List<ChatMessage>>> unreadChatsForReceiverFromSendersMap = new HashMap<>();
	
	private Map<Long, Map<Long, ChatMessage>> lastMessageBetweenCustomersMap = new HashMap<>();
	
	private Map<String, CustomerDTO> waitingSocialCustomers =  new HashMap<>();
	
	private Map<Long, CustomerNotificationDTO> lastPushNotifications =  new HashMap<>();
	
	private Map<Long, CustomerNotificationDTO> lastFullNotifications =  new HashMap<>();
	
	private long virtualHotelId = 0L;
	
	private long demoHotelId = 0L;
	
	private HashMap<Long, Long> customersToHotelIdMap =  new HashMap<>();
	
	/**
	 * anonymGuests id to GPS point
	 */
	private HashMap<Long, Double> guestToGpsPointMap = new HashMap<>();
	
	@PostConstruct
	public void init() {
		unreadChatsForReceiverFromSendersMap = new HashMap<>();
		lastMessageBetweenCustomersMap = new HashMap<>();
	}
	
	@Override
	public long getInitHotelId()
	{
		if(!ControllerUtils.ALLOW_INIT_VIRTUAL_HOTEL)
		{
			return 0;	
		}
		
		return virtualHotelId>0 ? virtualHotelId: getRepositoryVirtualHotelId();
	}
	
	@Override
	public long getDemoHotelId()
	{
		return demoHotelId>0? demoHotelId: getRepositoryDemoHotelId();
	}

	private int getRepositoryVirtualHotelId()
	{
		Integer virtualHotelId = hotelRepository.getVirtualHotelId();

		return  virtualHotelId==null? 0 : virtualHotelId;
	}
	
	private int getRepositoryDemoHotelId()
	{
		Integer demoHotelId = hotelRepository.getDemoHotelId();

		return  demoHotelId==null? 0 : demoHotelId;
	}
	
	@Override
	public Optional<Date> getLastCustomerOnlineTime(long customerId)
	{
		Optional<Date> lastCustomerOnlineTime = lastCustomerOnlineMap.entrySet().stream().filter(e -> e.getKey() == customerId).map(Entry::getValue).findFirst();
				
 		if(!lastCustomerOnlineTime.isPresent())
		{
			CustomerRootEntity customerEntity = customerService.getEntityById(customerId);
			
			if(customerEntity ==null || !customerEntity.isLogged())
			{
				return null;
			}
			
			Date lastSeenOnline = customerEntity.getLastSeenOnline();
			
			if(lastSeenOnline!=null)
			{
				lastCustomerOnlineMap.put(customerId, lastSeenOnline);
			}
			
			return Optional.of(lastSeenOnline);
		}
		
		return lastCustomerOnlineTime;
	}
	
	@Override
	public void updateCustomerConsistencyId(long customerId, long consustencyId)
	{
		currentConsistencyIdsMap.put(customerId, consustencyId);
	}
	
//	@Override
	private void updateLastOnlineTime(long customerId)
	{
		boolean updateCustomerRootEntity = true;
		
		if(lastCustomerOnlineMap.keySet().contains(customerId))
		{
			updateCustomerRootEntity = false;
			
			Date lastDate = lastCustomerOnlineMap.get(customerId);
			
			//if it was online yesterday
			if(convertToLocalDateTime(lastDate).getDayOfYear()!= LocalDateTime.now().getDayOfYear())
			{
				updateCustomerRootEntity = true;
			}
			
			lastCustomerOnlineMap.put(customerId, new Date());
		}
		
		if(updateCustomerRootEntity)
		{
			CustomerRootEntity customerEntity = customerService.getEntityById(customerId);
			
			if(customerEntity !=null)
			{
				customerEntity.updateLastSeenOnline();
				lastCustomerOnlineMap.put(customerId, new Date());
			}
		}
	}
	
	@Override
	public List<CustomerRootEntity> getOnlineCustomers()
	{
		List<CustomerRootEntity> resultList = new ArrayList<>();
		
		Set<Long> customerIds = lastCustomerOnlineMap.keySet();
		
		if(customerIds.isEmpty())
		{
			List<CustomerRootEntity> onlinein24HCustomerEntities =  customerService.getAllIn24hOnline();
			
			for (CustomerRootEntity nextOnlineIn24HCustomerRootEntity : onlinein24HCustomerEntities)
			{
				lastCustomerOnlineMap.put(nextOnlineIn24HCustomerRootEntity.getId(), nextOnlineIn24HCustomerRootEntity.getLastSeenOnline());
				
				if(isNowStillOnline(nextOnlineIn24HCustomerRootEntity.getId()))
				{
					resultList.add(nextOnlineIn24HCustomerRootEntity);
				}
			}
		}
		else {
			
			List<Long> nowOnlineCustomerIdx = new ArrayList<>();
			
			for (long nextCustomerId: customerIds)
			{
				if(isNowStillOnline(nextCustomerId))
				{
					nowOnlineCustomerIdx.add(nextCustomerId);
				}
			}
			
			if(!nowOnlineCustomerIdx.isEmpty())
			{
				resultList.addAll(customerRepository.findByIdIn(nowOnlineCustomerIdx));
			}
		}
		
		return resultList;
	}
	
	@Override
	public void checkCustomerOffline(long offlineId)	
	{
		if(lastCustomerOnlineMap.containsKey(offlineId))
		{
			lastCustomerOnlineMap.remove(offlineId);
		}
	}
	
	@Override
	public void checkCustomerOnline(long onlineId)	
	{
		updateLastOnlineTime(onlineId);
	}
	
	@Override
	public long getCustomerConsistencyId(long customerId)
	{
		if(currentConsistencyIdsMap.containsKey(customerId))
		{
			return currentConsistencyIdsMap.get(customerId);
		}
		
		CustomerRootEntity customerEntity = customerRepository.getOne(customerId);
		
		long newConsistencyId = customerEntity !=null? customerEntity.getConsistencyId(): -1;

		if(customerEntity !=null)
		{
			currentConsistencyIdsMap.put(customerId, newConsistencyId);
		}
		
		return newConsistencyId;
		
	}
	
	@Override
	public void markChatRead(long receiverId, long senderId)
	{
		Object unreadForReceiver = unreadChatsForReceiverFromSendersMap.entrySet().stream().filter(e -> e.getKey() == receiverId).map(Entry::getValue).findFirst();
		
		//empty all unread messages for this sender!
		//remove empty Lists from Map!!! sonst unread chat!!!

		if(unreadForReceiver!=null)
		{
			unreadChatsForReceiverFromSendersMap.get(receiverId).remove(senderId);
		}
	}
	
	@Override
	public void markMessageRead(ChatMessage readMessage)
	{
		Map<Long, List<ChatMessage>> unreadForeReceiver = unreadChatsForReceiverFromSendersMap.get(readMessage.getReceiver().getId());

		if(unreadForeReceiver==null)
		{
			unreadForeReceiver = new HashMap<>();

			//TODO Eugen: who is receiver hier????
			List<ChatMessage> unreadMessagesByReceiver = chatRepository.getUnreadChatMessagesForCustomer(readMessage.getReceiver().getId());
			
			for (ChatMessage nextUnreadMessageByReceiver: unreadMessagesByReceiver)
			{
				List<ChatMessage> unreadMessagesForSenderByReceiver = unreadForeReceiver.get(readMessage.getSender().getId());
				
				if(unreadMessagesForSenderByReceiver==null)
				{
					unreadMessagesForSenderByReceiver = new ArrayList<>();
				}

				unreadMessagesForSenderByReceiver.add(nextUnreadMessageByReceiver);

				unreadForeReceiver.put(readMessage.getSender().getId(), unreadMessagesForSenderByReceiver);
			}
			
			unreadChatsForReceiverFromSendersMap.put(readMessage.getReceiver().getId(), unreadForeReceiver);
		}
		
		List<ChatMessage> unreadSenderReceiver = unreadForeReceiver.get(readMessage.getSender().getId());
		
		if(unreadSenderReceiver!=null && unreadSenderReceiver.contains(readMessage))
		{
			unreadSenderReceiver.remove(readMessage);
			
			if(unreadSenderReceiver.isEmpty())
			{
				//remove empty Lists from Map!!! sonst unread chat!!!
				unreadForeReceiver.remove(readMessage.getSender().getId());
			}
			else{
				unreadForeReceiver.put(readMessage.getSender().getId(), unreadSenderReceiver);
			}
			
			unreadChatsForReceiverFromSendersMap.put(readMessage.getReceiver().getId(), unreadForeReceiver);
		}
		
	}
	
	@Override
	public void updateUnreadMessagesToCustomer(ChatMessage newUnreadMessage)
	{
		//If there is no unreadMessages for this receiver, create it from DB
		long receiverId = newUnreadMessage.getReceiver().getId();
		if(!unreadChatsForReceiverFromSendersMap.containsKey(receiverId))
		{
			Map<Long, List<ChatMessage>> unreadMessagesForReceiver = getUnreadChatsBySender(receiverId);
			
			unreadChatsForReceiverFromSendersMap.put(receiverId, unreadMessagesForReceiver);
		}
		
		long newSenderId = newUnreadMessage.getSender().getId();
		
		Map<Long, List<ChatMessage>> unreadByReceiver = unreadChatsForReceiverFromSendersMap.get(receiverId);

		List<ChatMessage> unreadForSender = new ArrayList<>();

		//ADD new ChatMessage to List on demand, if it is not constains there
		if(unreadByReceiver!=null && unreadByReceiver.containsKey(newSenderId))
		{
			unreadForSender = unreadByReceiver.get(newSenderId);
		}
		
		unreadForSender.add(newUnreadMessage);

		unreadByReceiver.put(newSenderId, unreadForSender);
		unreadChatsForReceiverFromSendersMap.put(receiverId, unreadByReceiver);
		
	}	
	
	@Override
	public Map<Long, List<ChatMessage>> getCustomerUnreadChatsBySenders(long receiverId)
	{
		//set from DB is not exists
		if(!unreadChatsForReceiverFromSendersMap.containsKey(receiverId))
		{
			Map<Long, List<ChatMessage>> unreadMessagesForReceiver = getUnreadChatsBySender(receiverId);
			
			unreadChatsForReceiverFromSendersMap.put(receiverId, unreadMessagesForReceiver);
		}
		
		return unreadChatsForReceiverFromSendersMap.get(receiverId);
	}
	
	
	private Map<Long, List<ChatMessage>> getUnreadChatsBySender(long receiverId)
	{
		List<ChatMessage> allUnreadChatsForReceiver = chatRepository.getUnreadChatMessagesForCustomer(receiverId);
		
		Map<Long, List<ChatMessage>> unreadMessagesForReceiver = new HashMap<>();
		
		if(allUnreadChatsForReceiver!=null)
		{
			for(ChatMessage nextUnreadChatmessage: allUnreadChatsForReceiver)
			{
				long nextSenderId = nextUnreadChatmessage.getSender().getId();

				List<ChatMessage> unreadFromSender = unreadMessagesForReceiver.containsKey(nextSenderId)? unreadMessagesForReceiver.get(nextSenderId) : new ArrayList<ChatMessage>();

				unreadFromSender.add(nextUnreadChatmessage);

				unreadMessagesForReceiver.put(nextSenderId, unreadFromSender);
			}
		}
		return unreadMessagesForReceiver;
	}
	
	@Override
	public List<Long> getOnlineCustomerIds()
	{
		List<Long> resultList = new ArrayList<>();
		
		Set<Long> customerIds = lastCustomerOnlineMap.keySet();
		
		if(customerIds.isEmpty())
		{
			List<CustomerRootEntity> onlinein24HCustomerEntities =  customerService.getAllIn24hOnline();
			
			for (CustomerRootEntity nextOnlineIn24HCustomerRootEntity : onlinein24HCustomerEntities)
			{
				lastCustomerOnlineMap.put(nextOnlineIn24HCustomerRootEntity.getId(), nextOnlineIn24HCustomerRootEntity.getLastSeenOnline());
				
				if(isNowStillOnline(nextOnlineIn24HCustomerRootEntity.getId()))
				{
					resultList.add(nextOnlineIn24HCustomerRootEntity.getId());
				}
			}
		}
		else {
			
			for (long nextCustomerId: customerIds)
			{
				if(isNowStillOnline(nextCustomerId))
				{
					resultList.add(nextCustomerId);
				}
			}
		}
		
		return resultList;
	}
	
	private boolean isNowStillOnline(long customerId)
	{
		Optional<Date> lastOnline = getLastCustomerOnlineTime(customerId);
		
		return lastOnline.isPresent() && lastOnline.get().after(convertToDate(LocalDateTime.now().minusMinutes(ONLINE_DELAY_MINUTES)));
	}
	
	@Override
	public void setLastMessageBetweenCustomers(ChatMessage lastMessage)
	{
		long senderId = lastMessage.getSender().getId();
		long receiverId = lastMessage.getReceiver().getId();
		
		long fromMin = senderId < receiverId? senderId : receiverId;
		long toMax = senderId > receiverId? senderId : receiverId;
		
		Map<Long, ChatMessage> newLastMsgMap = lastMessageBetweenCustomersMap.get(fromMin);
				
		if(newLastMsgMap==null)
		{
			newLastMsgMap = new HashMap<>();
		}
			
		newLastMsgMap.put(toMax, lastMessage);
		
		lastMessageBetweenCustomersMap.put(fromMin, newLastMsgMap);
	}

	@Override
	public void addWaitingSocialDto(String sessionState, CustomerDTO socialCustomer)
	{
		waitingSocialCustomers.put(sessionState, socialCustomer);
	}

	@Override
	public CustomerDTO getWaitingSocialCustomer(String sessionStateVal)
	{
		return waitingSocialCustomers.get(sessionStateVal);
	}

	@Override
	public CustomerNotificationDTO getLastPushNotifiation(long customerId)
	{
		if(customerId<=0)
		{
			return null;
		}
		
		return lastPushNotifications.get(customerId);
	}

	@Override
	public void setLastPushNotifiation(long customerId, CustomerNotificationDTO lastNotification)
	{
		lastPushNotifications.put(customerId, lastNotification);
	}
	
	@Override
	public void setLastFullNotification(long receiverId, CustomerNotificationDTO nextNotification)
	{
		lastFullNotifications.put(receiverId, nextNotification);
	}
	
	@Override
	public CustomerNotificationDTO getLastFullNotification(long receiverId)
	{
		return lastFullNotifications.get(receiverId);
	}
	
	@Override
	public boolean isNotificationDelayReady(long sessionCustomerId)
	{
		if(!lastFullNotifications.containsKey(sessionCustomerId))
		{
			return true;
		}
		
		CustomerNotificationDTO lastNotification = lastFullNotifications.get(sessionCustomerId);
		
		if(lastNotification==null)
		{
			return true;
		}
		
		long lastNotificationTime = lastNotification.getCreationTime();
		
		long diff = (new Date()).getTime() - lastNotificationTime;
		
		long diffMinutes = diff / (60 * 1000) % 60;
		
		return diffMinutes >= ControllerUtils.NOTIFICATION_MIN_DELAY_MINUTES;
	}

	@Override
	public long getCustomerHotelId(long customerId)
	{
		if(!customersToHotelIdMap.containsKey(customerId))
		{
			customersToHotelIdMap.put(customerId, checkinRepository.getCustomerHotelId(customerId, new Date()));
		}

		Long hotelId = customersToHotelIdMap.get(customerId);
		return hotelId!=null? hotelId : getInitHotelId();
	}

	@Override
	public void updateCustomerHotelId(long customerId, long hotelId)
	{
		customersToHotelIdMap.put(customerId, hotelId);
	}
	
	@Override
	public void addGuestGpsPosition(long requesterId, Double latLonPoint)
	{
		guestToGpsPointMap.put(requesterId, latLonPoint);
	}
	
	@Override
	public Double getGuestGpsPosition(long requesterId)
	{
		return guestToGpsPointMap.get(requesterId);
	}
	
	@Override
	public void markLastMessageBetweenCustomers(ChatMessage seenMessage)
	{
		long senderId = seenMessage.getSender().getId();
		long receiverId = seenMessage.getReceiver().getId();

		long fromMin = senderId < receiverId? senderId : receiverId;
		long toMax = senderId > receiverId? senderId : receiverId;

		Map<Long, ChatMessage> newLastMsgMap = lastMessageBetweenCustomersMap.get(fromMin);

		if(newLastMsgMap==null)
		{
			newLastMsgMap = new HashMap<>();
		}

		if(newLastMsgMap.get(toMax)!=null && newLastMsgMap.get(toMax).equals(seenMessage))
		{
			newLastMsgMap.put(toMax, seenMessage);
			lastMessageBetweenCustomersMap.put(fromMin, newLastMsgMap);
		}

	}
	
	@Override
	public ChatMessage getLastMessageBetweenCustomers(long senderId, long receiverId){
		
		long fromMin = senderId < receiverId? senderId : receiverId;
		long toMax = senderId > receiverId? senderId : receiverId;

		Map<Long, ChatMessage> lastMsgMap = lastMessageBetweenCustomersMap.get(fromMin);
		
		if(lastMsgMap==null || !lastMsgMap.containsKey(toMax))
		{
			ChatMessage dbLastMessage = chatRepository.getLastMessageByCustomerAndReceiverIds(fromMin, toMax);
			
			if(dbLastMessage!=null)
			{
				if(lastMsgMap==null)
				{
					lastMsgMap = new HashMap<>();
				}
				
				lastMsgMap.put(toMax, dbLastMessage);
				lastMessageBetweenCustomersMap.put(fromMin, lastMsgMap);
			}
		}
		
		return lastMsgMap!=null? lastMsgMap.get(toMax) : null;
	}
	
	
	///###############################################################
	
	private LoadingCache<String, UserStats> statsByUser = CacheBuilder.newBuilder().build(new CacheLoader<String, UserStats>() {
		
		@Override
		public UserStats load(String key) throws Exception {
			return new UserStats();
		}
		
	});
	
	public void mark(String username) {
		statsByUser.getUnchecked(username).mark();
	}
	
	public Set<String> getActiveUsers() {
		Set<String> active = Sets.newTreeSet();
		for (String user : statsByUser.asMap().keySet()) {
			// has the user checked in within the last 5 seconds?
			if ((System.currentTimeMillis() - statsByUser.getUnchecked(user).lastAccess()) < 5000) {
				active.add(user);
			}
		}
		return active;
	}
	
	private static class UserStats {
		
		private AtomicLong lastAccess = new AtomicLong(System.currentTimeMillis());
		
		private void mark() {
			lastAccess.set(System.currentTimeMillis());
		}
		
		private long lastAccess() {
			return lastAccess.get();
		}
	}
}
