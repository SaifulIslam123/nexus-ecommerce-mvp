package com.ecommerce.mvp.modules.courier

import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import java.util.Random

class TrackingIdGenerator {
    companion object {
        // Custom alphabet: Uppercase + Numbers (Removed 0, O, I, L to avoid typos)
        private val ALPHABET = "123456789ABCDEFGHJKMNPQRSTUVWXYZ".toCharArray()
        private val RANDOM = Random()

        /**
         * Generates an Amazon-style Tracking ID using the correct randomNanoId method
         * Result: TRK-9B2V-X1P4
         */
        fun generateTrackingId(prefix: String = "TRK"): String {
            // Corrected: Uses randomNanoId instead of generate
            val part1 = NanoIdUtils.randomNanoId(RANDOM, ALPHABET, 4)
            val part2 = NanoIdUtils.randomNanoId(RANDOM, ALPHABET, 4)

            return "$prefix-$part1-$part2"
        }

        /**
         * Generates a single-block ID
         * Result: R2D2-X9P1
         */
        fun generateShortId(): String {
            return NanoIdUtils.randomNanoId(RANDOM, ALPHABET, 8)
                .chunked(4)
                .joinToString("-")
        }
    }
}
