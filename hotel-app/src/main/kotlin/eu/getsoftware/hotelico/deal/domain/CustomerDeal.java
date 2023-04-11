package eu.getsoftware.hotelico.deal.domain;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import eu.getsoftware.hotelico.checkin.domain.HotelActivity;
import eu.getsoftware.hotelico.clients.infrastructure.utils.HibernateUtils;
import eu.getsoftware.hotelico.customer.domain.CustomerRootEntity;
import eu.getsoftware.hotelico.deal.infrastructure.utils.DealStatus;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name = "customer_activity_deal")
@AssociationOverrides({
		@AssociationOverride(name = "pk.customer",
				joinColumns = @JoinColumn(name = "CUSTOMER_ID")),
		@AssociationOverride(name = "pk.activity",
				joinColumns = @JoinColumn(name = "ACTIVITY_ID")) })
public class CustomerDeal implements java.io.Serializable {

	private static final long serialVersionUID = -2949611288215768311L;
	
	@Column(name = "active", columnDefinition = HibernateUtils.ColumnDefinition.BOOL_DEFAULT_TRUE)
	private boolean active = true;

	private CustomerDealId pk = new CustomerDealId();
	
	@Column
	private String dealCode = "xxx";	
	
	@Column
	private String tablePosition = "";

	@Column(name = "initId", columnDefinition = "BIGINT(20) DEFAULT 0")
	private long initId;
	
	@Column(name = "guestCustomerId", columnDefinition = "BIGINT(20) DEFAULT 0")
	private long guestCustomerId;

	@Column(name = "totalMoney", columnDefinition = "Decimal(6,2) default 0")
	private Double totalMoney;
	
	@Enumerated(EnumType.STRING)
	private DealStatus status;

	@Column(name = "consistencyId", columnDefinition = "BIGINT(20) DEFAULT 0")
	private long consistencyId = new Date().getTime();
	
	@Temporal(TemporalType.DATE)
	@Column(name = "validFrom", nullable = false, length = 10)
	private Date validFrom = new Date();

	@Temporal(TemporalType.DATE)
	@Column(name = "validTo", nullable = false, length = 10)
	private Date validTo;
	
	public CustomerDeal() {
	}
	
	public CustomerDeal(CustomerRootEntity customerEntity, HotelActivity activity, long initId) {
		this(customerEntity, activity);
		
		this.initId = initId;
	}
	
	public CustomerDeal(CustomerRootEntity customerEntity, HotelActivity activity) {
		
		this();
		
		setCustomer(customerEntity);
		setActivity(activity);
		generateCode();

		int validDays = activity.getDealDaysDuration()>0 ? activity.getDealDaysDuration() : 1;
		this.validTo = convertToDate(LocalDateTime.now().plusDays(validDays).withHour(4));
		this.initId = ThreadLocalRandom.current().nextInt(1, 999999);
	}
	
	private Date convertToDate(LocalDateTime localDateTime)
	{
		return java.sql.Timestamp.valueOf(localDateTime);
	}
	
	@EmbeddedId
	public CustomerDealId getPk() {
		return pk;
	}

	public void setPk(CustomerDealId pk) {
		this.pk = pk;
	}

	@Transient
	public CustomerRootEntity getCustomer() {
		return getPk().getCustomer();
	}
	
	public void setCustomer(CustomerRootEntity customerEntity) {
		getPk().setCustomer(customerEntity);
	}

	@Transient
	public HotelActivity getActivity() {
		return getPk().getActivity();
	}

	public void setActivity(HotelActivity activity) {
		getPk().setActivity(activity);
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		CustomerDeal that = (CustomerDeal) o;

		if (getPk() != null ? !getPk().equals(that.getPk())
							: that.getPk() != null)
			return false;

		return true;
	}

	public int hashCode() {
		return (getPk() != null ? getPk().hashCode() : 0);
	}
	
	public void generateCode()
	{
		this.dealCode = String.valueOf(ThreadLocalRandom.current().nextInt(1, 999999));
	}

	public String getDealCode()
	{
		return dealCode;
	}

}