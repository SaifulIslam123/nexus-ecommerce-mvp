package com.ecommerce.mvp.modules.shopper

import com.ecommerce.mvp.common.dto.AddressDto
import com.ecommerce.mvp.common.dto.UserDto
import com.ecommerce.mvp.common.dto.UserProfileUpdateDto
import com.ecommerce.mvp.modules.shopper.service.ShopperService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/shopper")
class ShopperController(
    private val shopperService: ShopperService
) {

    @GetMapping("/me")
    fun getCurrentUserProfile(): UserDto {
        val profile = shopperService.getCurrentUserProfile()
        return profile
    }

    @PatchMapping("/me")
    fun updateCurrentUserProfile(@Valid @RequestBody updateDto: UserProfileUpdateDto): UserDto {
        val updatedProfile = shopperService.updateCurrentUserProfile(updateDto)
        return updatedProfile
    }

    @PostMapping("/me/addresses")
    fun addAddress(@Valid @RequestBody addressRequestDto: AddressDto): UserDto {
        val savedAddress = shopperService.addAddress(addressRequestDto)
        return savedAddress
    }

    @DeleteMapping("/me/addresses/{id}")
    fun deleteAddress(@PathVariable id: Long): ResponseEntity<Unit> {
         shopperService.deleteAddress(id)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

}




