package eu.getsoftware.hotelico.domain.hotel;

/**
 * Created by Eugen on 16.07.2015.
 * Many-To-Many intersection table between Customers and Hotels
 * 
 * @Embeddable embedded by other entities:  it does not have independent existence. 
 * Thus we cannot run DB queries, without depending on other class
 */

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

import eu.getsoftware.hotelico.domain.customer.CustomerRootEntity;

@Embeddable
class CustomerHotelCheckinId implements java.io.Serializable {

	private static final long serialVersionUID = 552154191271568694L;
	private CustomerRootEntity customerEntity;
	private HotelRootEntity hotelRootEntity;

	@ManyToOne
	public CustomerRootEntity getCustomer() {
		return customerEntity;
	}

	public void setCustomer(CustomerRootEntity stock) {
		this.customerEntity = stock;
	}

	@ManyToOne
	public HotelRootEntity getHotel() {
		return hotelRootEntity;
	}

	public void setHotel(HotelRootEntity category) {
		this.hotelRootEntity = category;
	}

	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CustomerHotelCheckinId that = (CustomerHotelCheckinId) o;

		if (customerEntity != null ? !customerEntity.equals(that.customerEntity) : that.customerEntity != null) return false;
		if (hotelRootEntity != null ? !hotelRootEntity.equals(that.hotelRootEntity) : that.hotelRootEntity != null)
			return false;

		return true;
	}

	public int hashCode() {
		int result;
		result = (customerEntity != null ? customerEntity.hashCode() : 0);
		result = 31 * result + (hotelRootEntity != null ? hotelRootEntity.hashCode() : 0);
		return result;
	}

}