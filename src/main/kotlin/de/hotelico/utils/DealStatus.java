package de.hotelico.utils;

import java.util.ArrayList;
import java.util.List;

import de.hotelico.model.ChatMessage;
import de.hotelico.model.Customer;
import de.hotelico.model.CustomerDeal;
import de.hotelico.model.Hotel;
import de.hotelico.model.HotelActivity;
import de.hotelico.model.HotelWallPost;
import lombok.Getter;
import lombok.Setter;

/**
 * <br/>
 * Created by e.fanshil
 * At 19.10.2015 17:19
 */
@Getter
public enum DealStatus
{
	ACCEPTED("ACCEPTED", 1),
	EXECUTED("EXECUTED", 2),
	REJECTED("REJECTED", 3),
	CLOSED("CLOSED", 4);
	
	private String name;
	private int value;
	
	DealStatus(String name, int value)
	{
		this.name = name;
		this.value = value;
	}
	
	public static DealStatus parseByName(String name)
	{
		if (!ObjectUtils.isEmpty(name))
		{
			for (DealStatus nextEvent : DealStatus.values())
			{
				if (nextEvent.name.equalsIgnoreCase(name))
				{
					return nextEvent;
				}
			}
		}
		return null;
	}
	
	public static List<DealStatus> getFilterStatusList(boolean closed)
	{
		List<DealStatus> filterDealStatusList =  new ArrayList<>();
		
		if(closed)
		{
			filterDealStatusList.add(DealStatus.CLOSED);
		}
		else{
			filterDealStatusList.add(DealStatus.ACCEPTED);
			filterDealStatusList.add(DealStatus.EXECUTED);
		}
		return filterDealStatusList;
	}
	
	
	
 
}
