/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.world.biome

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import tf.veriny.keymountain.api.util.Identifiable
import tf.veriny.keymountain.api.util.Identifier

// only mandatory properties; besides, they're fucking it all up in 1.19.4.

/**
 * Contains info that is sent to the client about biome data.
 */
public data class BiomeNetworkInfo(
    @JsonIgnore
    override val identifier: Identifier,

    public val precipitation: String,
    public val temperature: Float,
    public val downfall: Float,
    public val effects: BNIEffects,
) : Identifiable {
    public data class BNIEffects(
        @JsonProperty("sky_color")
        public val skyColour: Int,
        @JsonProperty("water_fog_color")
        public val waterFogColour: Int,
        @JsonProperty("fog_color")
        public val fogColour: Int,
        @JsonProperty("water_color")
        public val waterColour: Int,
    )
}