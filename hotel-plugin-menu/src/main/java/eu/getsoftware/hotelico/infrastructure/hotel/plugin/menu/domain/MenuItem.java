package eu.getsoftware.hotelico.infrastructure.hotel.plugin.menu.domain;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import eu.getsoftware.hotelico.clients.infrastructure.utils.HibernateUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Eugen on 16.07.2015.
 */
@Entity
@Getter @Setter
@Table(name = "menu_item")
public class MenuItem implements Serializable
{
	private static final long serialVersionUID = -3552760230944489778L;
	
	@Id
	@Setter(AccessLevel.PROTECTED)
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private long id;
	
	//EUGEN: not RootEntity, but its id
	//	@ManyToOne
	@JoinColumn(name="hotelId")
	private long hotelRootEntityId;
	
	//EUGEN: not RootEntity, but its id
	//	@ManyToOne
	@JoinColumn(name="creatorId")
	private long creatorId;
	
	@Column
	private int cafeId;	
	
	@Column
	private int orderIndex;
	
	@ManyToOne
	@JoinColumn(name="orderId")
	private MenuOrder menuOrder;
	
	/**
	 * messageId -> creationTime
	 * consistencyId -> last update time
	 */
	@Column(name = "consistencyId", columnDefinition = HibernateUtils.ColumnDefinition.LONG_20_DEFAULT_0)
	private long consistencyId;		
	
	@Column(name = "initId", columnDefinition = HibernateUtils.ColumnDefinition.LONG_20_DEFAULT_0)
	private long initId;	
	
	@Column(name = "amount", columnDefinition = HibernateUtils.ColumnDefinition.INT_11_DEFAULT_0)
	private int amount;

	@Column(name = "active", columnDefinition = HibernateUtils.ColumnDefinition.BOOL_DEFAULT_TRUE)
	private boolean active = true;
	
	@Column(name = "delimiter", columnDefinition = HibernateUtils.ColumnDefinition.BOOL_DEFAULT_FALSE)
	private boolean delimiter = false;
	
	@Column
	private Timestamp timestamp;
	
//	@Temporal(TemporalType.DATE)
//	@Column(name = "validFrom", nullable = true, length = 10)
//	private Date validFrom;
//
//	@Temporal(TemporalType.DATE)
//	@Column(name = "validTo", nullable = true, length = 10)
//	private Date validTo;
	
	@Column
	private String title;
	
	@Column
	private String shortDescription;
	
//	@Column(name = "activityArea", nullable = false)
//	private String activityArea;
	
	@Column( columnDefinition = "LONGTEXT")
	@Type(type = "text")
	private String description;	
	
	@Column(name = "pictureUrl", length = 250)
	private String pictureUrl;	
	
	@Column(name = "price", columnDefinition= HibernateUtils.ColumnDefinition.PRICE_DEFAULT_0)
	private double price = 0.0;
	
	public static long getSerialVersionUID()
	{
		return serialVersionUID;
	}
	
	public long getId()
	{
		return id;
	}
}
