package eu.getsoftware.hotelico.hotel.service;

import java.util.List;

import eu.getsoftware.hotelico.hotel.dto.MenuItemDto;
import eu.getsoftware.hotelico.hotel.dto.MenuOrderDTO;
import org.springframework.transaction.annotation.Transactional;

/**
 * <br/>
 * Created by e.fanshil
 * At 05.02.2016 15:30
 */
public interface MenuService
{	
	@Transactional
	List<MenuOrderDTO> getActiveMenusByCustomerId(long customerId, long hotelId, long cafeId, long orderId, boolean closed);
	
	@Transactional MenuOrderDTO addMenuAction(long customerId, long initMenuOrderId, String action);

	/**
	 * 
	 * @param requesterId - either customer or staff: staff becomes all hotel orders!!!
	 * @param hotelId
	 * @param cafeId
	 * @return
	 */
	@Transactional
	List<MenuOrderDTO> getAllHotelMenusToRoom(long requesterId, long hotelId, long cafeId);
	
	@Transactional MenuOrderDTO deleteMenuOrder(long requesterId, long initMenuOrderId);
	
	@Transactional MenuOrderDTO addUpdateMenu(long customerId, long initMenuOrderId, MenuOrderDTO menuOrderDto);
	
	@Transactional
	List<MenuItemDto> getMenuItemsByHotelId(long customerId, long hotelId, long cafeId);
	
	@Transactional
	MenuItemDto deleteMenuItem(long requesterId, long menuItemId);

	@Transactional
	MenuItemDto addUpdateMenuItem(long customerId, long hotelId, long cafeId, long itemId, MenuItemDto menuItemDto);
	
	@Transactional
	List<MenuItemDto> getReorderedMenuItems(long customerId, long hotelId, long cafeId, String reorder);
}