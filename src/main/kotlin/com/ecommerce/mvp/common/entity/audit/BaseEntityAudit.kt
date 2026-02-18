package com.ecommerce.mvp.common.entity.audit

import com.ecommerce.mvp.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import lombok.Getter
import lombok.Setter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.util.Date

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntityAudit(
) : BaseEntity(), Serializable {

    @Column(name = "created_date", nullable = false, updatable = false)
    @CreatedDate
    var createdDate: Date? = null

    @Column(name = "modified_date")
    @LastModifiedDate
    var modifiedDate: Date? = null

    @Column(name = "created_by")
    @CreatedBy
    var createdBy: String? = null

    @Column(name = "modified_by")
    @LastModifiedBy
    var modifiedBy: String? = null

}