package com.ecommerce.mvp.modules.role.model.entity

import com.practice.ecommerce.ecommerce.common.entity.audit.BaseEntityAudit
import com.practice.ecommerce.ecommerce.modules.user.model.entity.User
import jakarta.persistence.*


@Entity
@Table(name = "roles")
class Role : BaseEntityAudit() {

    @Enumerated(EnumType.STRING)
    @Column(length = 20, unique = true)
    var name: ERole? = null

    @ManyToMany(mappedBy = "userRoles")
    private val users: MutableSet<User> = mutableSetOf()




}

enum class ERole {
    USER,
    ADMIN,
    MODERATOR,
    COURIER
}