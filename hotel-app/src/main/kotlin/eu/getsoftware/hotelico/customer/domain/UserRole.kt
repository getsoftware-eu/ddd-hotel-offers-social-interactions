package eu.getsoftware.hotelico.customer.domain

import eu.getsoftware.hotelico.customer.domain.enums.UserRoleEnum
import javax.persistence.*

/**
 *
 * <br/>
 * Created by e.fanshil
 * At 06.01.2017 16:46
 */
@Entity
@Table(name = "user_roles", uniqueConstraints = [UniqueConstraint(columnNames = ["role", "user_id"])])
class UserRole{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_role_id", unique = true, nullable = false)
    var user_role_id: Long = 0

    @Column(name = "user_id")
    var user_id: Long = 0

    @Column(name = "role", nullable = true, length = 45)
    @Enumerated(EnumType.STRING)
    var role: UserRoleEnum = UserRoleEnum.ROLE_HOTELICO_VIEWER

    constructor()

    constructor(userId: Long, role: UserRoleEnum)
    {
        this.user_id = userId
        this.role = role
    }
}