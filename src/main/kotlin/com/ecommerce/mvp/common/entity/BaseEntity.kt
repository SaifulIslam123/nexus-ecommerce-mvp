package com.ecommerce.mvp.common.entity

import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import lombok.Getter
import lombok.Setter
import java.io.Serializable
import java.util.*


@Getter
@Setter
@MappedSuperclass
abstract class BaseEntity : Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null


    override fun hashCode(): Int {
        return Objects.hash(id)
    }

    override fun toString(): String {
        return "BaseEntity {" +
                "id = " + id +
                "}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseEntity

        if (id != other.id) return false

        return true
    }
}
