package com.ecommerce.mvp.modules.role.controller

import com.practice.ecommerce.ecommerce.modules.role.model.entity.Role
import com.practice.ecommerce.ecommerce.modules.role.service.RoleService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/roles")
class RoleController(private val roleService: RoleService) {

    @PostMapping("/create")
    fun createUser(@RequestBody role: Role): ResponseEntity<Any> {
        val savedRole = roleService.save(role)
        return ResponseEntity.status(HttpStatus.CREATED).body(role)
    }

}