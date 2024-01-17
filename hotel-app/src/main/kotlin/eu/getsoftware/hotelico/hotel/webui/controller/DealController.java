package eu.getsoftware.hotelico.hotel.webui.controller;

import eu.getsoftware.hotelico.clients.infrastructure.controller.BasicController;
import eu.getsoftware.hotelico.deal.infrastructure.dto.CustomerDealDTO;
import eu.getsoftware.hotelico.deal.infrastructure.utils.DealAction;
import eu.getsoftware.hotelico.hotel.application.iservice.IHotelService;
import eu.getsoftware.hotelico.hotel.infrastructure.aspects.NotifyClients;
import eu.getsoftware.hotelico.hotel.infrastructure.dto.ResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/deals")
public class DealController extends BasicController
{

  private Logger logger = LoggerFactory.getLogger(getClass());
  
  @Autowired
  private IHotelService hotelService;
	
  @RequestMapping(method = RequestMethod.GET)
  public String viewApplication() {
    return "deal";
  }

//  @MessageMapping("/activity")
//  //who should recieve result of this method
//  @SendTo("/activitytopic/message")
//  public HotelActivityDto sendActivity(HotelActivityDto activityDto) {
//    logger.info("Activity sent");
//
//    HotelActivityDto savedActivity = hotelService.addUpdateHotelActivity(activityDto.getSenderId(), activityDto);
//    
//    return activityDto;
//  }

  @RequestMapping(value = "/deals/customer/{customerId}/activity/{activityId}/hotel/{hotelId}/limitByRequester/{onlyRequesterDeals}/showClosed/{showClosed}", method = RequestMethod.GET)
  public @ResponseBody
  List<CustomerDealDTO> getDeals(@PathVariable long customerId, @PathVariable int activityId, @PathVariable long hotelId, @PathVariable boolean onlyRequesterDeals, @PathVariable boolean showClosed) {
    //TODO Eugen: socket ConnectException: Connection timed out: connect
    return hotelService.getDealsByActivityOrHotelId(customerId, hotelId, activityId, onlyRequesterDeals, showClosed);
  }
  
  @RequestMapping(value = "/action/{action}/customer/{customerId}/activityId/{activityId}/dealId/{dealId}/tablePosition/{tablePosition}/totalMoney/{totalMoney}", method = RequestMethod.GET)
  public @ResponseBody
  CustomerDealDTO addDealAction(@PathVariable String action, @PathVariable long customerId, @PathVariable long activityId, @PathVariable long dealId, @PathVariable String tablePosition, @PathVariable double totalMoney) {
    return hotelService.addDealAction(customerId, activityId, dealId, DealAction.parseByName(action), tablePosition, totalMoney);
  }
	
  
  @NotifyClients
  @RequestMapping(value = "/customer/{customerId}/activityId/{activityId}/deal/{dealId}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public @ResponseBody ResponseDTO deleteDeal(@PathVariable long customerId, @PathVariable long activityId, @PathVariable int dealId) {
    
    return hotelService.deleteDeal(customerId, activityId, dealId);
  }

  @RequestMapping(value = "/customer/{customerId}/activityId/{activityId}/deal/{dealId}", method = RequestMethod.POST)//, headers ="Accept:*/*")
  public @ResponseBody
  CustomerDealDTO addUpdateDeal(@PathVariable long guestCustomerId, @PathVariable long activityId, @PathVariable long dealId, @RequestBody CustomerDealDTO dealDto) {

//    activityDto.setInitId(activityId);
	  CustomerDealDTO out = hotelService.addUpdateDeal(guestCustomerId,activityId,dealDto);
    return out;
  }

//  @NotifyClients
//  @SendTo("/activitytopic/message")
//  @RequestMapping(value = "/customer/activity", method = RequestMethod.PUT, headers ="Accept:*/*")
//  public @ResponseBody HotelActivityDto addActivity(@RequestBody HotelActivityDto activityDto) {
////    activityDto.setInitId(activityId);
//    HotelActivityDto out = hotelService.addUpdateHotelActivity(activityDto.getSenderId(), activityDto);
//    return out;
//  }

}
