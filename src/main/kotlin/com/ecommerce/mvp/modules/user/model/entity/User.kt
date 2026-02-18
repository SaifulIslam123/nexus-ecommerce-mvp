package com.ecommerce.mvp.modules.user.model.entity

import com.practice.ecommerce.ecommerce.common.entity.audit.BaseEntityAudit
import com.practice.ecommerce.ecommerce.modules.role.model.entity.Role
import com.practice.ecommerce.ecommerce.modules.order.model.entity.Order
import com.practice.ecommerce.ecommerce.modules.cart.model.entity.Cart
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank


@Entity
@Table(name = "users")
class User : BaseEntityAudit() {

    @Column(nullable = false)
    var name: String? = null

    @Column(nullable = false, unique = true)
    var email: String? = null

    @Column(nullable = false)
    var password: String? = null

    @Column
    var phone: String? = null

    @ManyToMany(cascade = [CascadeType.ALL])
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    var userRoles: MutableSet<Role> = mutableSetOf()

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var orders: MutableSet<Order> = mutableSetOf()

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var addresses: MutableSet<Address> = mutableSetOf()

    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var cart: Cart? = null

}

data class UserCreateRequest(
    @field:NotBlank(message = "Name is required")
    val name: String,
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email should be valid")
    val email: String,
    @field:NotBlank(message = "Password is required")
    val password: String,
    val phone: String? = null

)