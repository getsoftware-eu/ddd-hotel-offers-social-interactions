package eu.getsoftware.hotelico.customer.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;

import eu.getsoftware.hotelico.domain.utils.HibernateUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Root Enity, only one way to get sub Entites
 * main data, will be fetched with every query
 */
@Entity
@Getter @Setter
@Table(name = "customer")
@DynamicUpdate
public class CustomerRootEntity implements Serializable
{
    @Id
    @Setter(AccessLevel.PROTECTED)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "customer_id_generator")
    @SequenceGenerator(name="customer_id_generator", sequenceName = "customer_id_seq")
    private long id;
    
    @Column(name = "active", columnDefinition = HibernateUtils.ColumnDefinition.BOOL_DEFAULT_TRUE)
    private boolean active = true;
    
    @Column(name = "logged", columnDefinition = HibernateUtils.ColumnDefinition.BOOL_DEFAULT_FALSE)
    private boolean logged = false;
    
    @Column(name = "showAvatar", columnDefinition = HibernateUtils.ColumnDefinition.BOOL_DEFAULT_TRUE)
    private boolean showAvatar = true;
    
    @Column(name = "guestAccount", columnDefinition = HibernateUtils.ColumnDefinition.BOOL_DEFAULT_FALSE)
    private boolean guestAccount = false;
    
    @Column(name = "userNameHotelico")
    private String userNameHotelico;
    
    @Column(name = "firstName")
    private String firstName;
    
    @Column(name = "lastName")
    private String lastName;
    
    @Column(name = "sex")
    private String sex;
    
    @Column(name = "website")
    private String website;
    
    @Column(name = "education")
    private String education;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "passwordHash")
    private Long passwordHash;
    
    @Column(name = "passwordValue", nullable = true)
    private String passwordValue;
    
    @Column(name = "wrongPasswortCounter", columnDefinition = "int(11) DEFAULT 0")
    private int wrongPasswortCounter;
    
    @Column(name = "lastResetPasswordRequestTime", columnDefinition = "BIGINT(20) DEFAULT 0")
    private long lastResetPasswordRequestTime;
    
    @Column(name = "consistencyId", columnDefinition = "BIGINT(20) DEFAULT 0")
    private long consistencyId;
   
    @Column(name = "lastResetPasswordConfirmationTime")
    private Date lastResetPasswordConfirmationTime;
    
    @Column(name = "lastSeenOnline")
    private Date lastSeenOnline;
    
    @Column(name = "email", nullable = true)
    private String email;
    
    @Column(name = "pictureUrl",  length = 250)
    private String pictureUrl;

    @Column
    private String linkedInId;
    
    @Column(name = "mediaUploaded", columnDefinition = HibernateUtils.ColumnDefinition.BOOL_DEFAULT_FALSE)
    private boolean mediaUploaded = false;
    
    @Column
    private String facebookId;
   
    @Column(name = "hotelStaff", columnDefinition = HibernateUtils.ColumnDefinition.BOOL_DEFAULT_FALSE)
    private boolean hotelStaff = false;
    
    @Column(name = "admin", columnDefinition = HibernateUtils.ColumnDefinition.BOOL_DEFAULT_FALSE)
    private boolean admin = false;
    
    @Column(name = "showInGuestList", columnDefinition = HibernateUtils.ColumnDefinition.BOOL_DEFAULT_TRUE)
    private boolean showInGuestList = true;
    
    @Column(name = "pushRegistrationId")
    private String pushRegistrationId;
    
    @ManyToMany(mappedBy= "likedCustomerEntities")
    private Set<HotelActivity> likedActivities = new HashSet<HotelActivity>();
    
    @ManyToMany(mappedBy= "subscribeCustomerEntities")
    private Set<HotelActivity> subscribeActivities = new HashSet<HotelActivity>();
    
    @ManyToMany(cascade = {CascadeType.ALL})
    @JoinTable(name="INTEREST_CUSTOMER_MAP",
            joinColumns={@JoinColumn(name="CUSTOMER_ID")},
            inverseJoinColumns={@JoinColumn(name="INTEREST_ID")})
    private Set<InterestCustomer> interests = new HashSet<InterestCustomer>();

    @ManyToMany(cascade = {CascadeType.ALL})
    @JoinTable(name="LANGUAGE_CUSTOMER_MAP",
            joinColumns={@JoinColumn(name="CUSTOMER_ID")},
            inverseJoinColumns={@JoinColumn(name="LANGUAGE_ID")})
    private Set<Language> languageSet = new HashSet<Language>();
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "details_id")
    private CustomerDetails customerDetails;
   
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "position_id")
    private CustomerGPSPosition customerGPSPosition;    
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "preferences_id")
    private CustomerPreferences customerPreferences;
    
	@Column(name = "points", columnDefinition="Decimal(10,2) default '0.00'")
	private double points = 0.0;

    //####################################################
    
    public CustomerRootEntity() {
        super();
    } 
    
    public CustomerRootEntity(String _firstName, String _lastName) {
        this();
        this.firstName = _firstName;
        this.lastName = _lastName;
    }

    public long getId(){ return this.id; }
    
    public void updateLastSeenOnline()
    {
        this.lastSeenOnline = new Date();
    }
    
    //################### 1:n:1 ###################################
    
//    @Override
    public String getPlainFilePath(final int upperOrderId)
    {
//        if (getId() < upperOrderId)
//        {
//            return String.valueOf(getId());
//        }

        return "customer/" + getId() + "";
    }
    
    //@Override
    public void setMediaUploaded(boolean mediaUploaded)
    {
        this.mediaUploaded = mediaUploaded;
    }
    
	@Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        CustomerRootEntity customerRootEntity = (CustomerRootEntity) o;

        if (id != customerRootEntity.id)
        {
            return false;
        }
        if (hotelStaff != customerRootEntity.hotelStaff)
        {
            return false;
        }
        if (admin != customerRootEntity.admin)
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int result = (int)id;
        result = 31 * result + (active ? 1 : 0);
        result = 31 * result + (showAvatar ? 1 : 0);
        result = 31 * result + (int) (lastResetPasswordRequestTime ^ (lastResetPasswordRequestTime >>> 32));
        result = 31 * result + (int) (consistencyId ^ (consistencyId >>> 32));
        result = 31 * result + (linkedInId != null ? linkedInId.hashCode() : 0);
        result = 31 * result + (facebookId != null ? facebookId.hashCode() : 0);
        result = 31 * result + (admin ? 1 : 0);
        
        return result;
    }
    
    public CustomerAggregate getEntityAggregate()
    {
        return new CustomerAggregate(this);
    }
	
}
