/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.world

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import tf.veriny.keymountain.api.util.Identifiable
import tf.veriny.keymountain.api.util.Identifier
import kotlin.math.min

// TODO: probably can have these replaced with some sort of DimensionType interface, and
//  automatically generate these for the protocol.

/**
 * Contains static information about a single dimension.
 */
public data class DimensionInfo(
    /**
     * Uniquely identifies this dimension. Example: ``minecraft:overworld``
     */
    @JsonIgnore
    override val identifier: Identifier,

    /**
     * The maximum height that blocks can be placed in this dimension. This should be a multiple of 16.
     */
    @JsonProperty("height")
    public val maxHeight: Int,

    /**
     * The minimum height that blocks can be placed in this dimension. This should be a multiple
     * of 16.
     */
    @JsonProperty("min_y")
    public val minHeight: Int,

    // == Optional properties with sane defaults == //
    /**
     * If true, then water will evaporate on placement and sponges will dry out.
     */
    @JsonProperty("ultrawarm")
    public val waterEvaporates: Boolean = false,

    /**
     * If false, then compasses will spin randomly and beds won't work.
     */
    @JsonProperty("natural")
    public val allowBedsAndCompasses: Boolean = true,

    /**
     * The multiplier applied to coordinates when leaving the dimension.
     */
    @JsonProperty("coordinate_scale")
    public val coordinateScale: Double = 1.0,

    /**
     * If true, this dimension has a skylight causing blocks to be lit up when they have a clear
     * path to the sky.
     */
    @JsonProperty("has_skylight")
    public val hasSkylight: Boolean = true,

    /**
     * If true, this dimension is marked as having a bedrock ceiling. Not sure what the actual
     * purpose of this is, as you don't have to actually generate a bedrock ceiling.
     */
    @JsonProperty("has_ceiling")
    public val hasBedrockCeiling: Boolean = false,

    /**
     * If non-null, then the dimension will not tick time and will instead be permanently set to the
     * specified time value.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("fixed_time")
    public val fixedTime: Int? = null,

    /**
     * The level of light that mobs can spawn at. Not used by Key Mountain currently.
     */
    @JsonProperty("monster_spawn_light_level")
    public val spawnMonstersAtLightLevel: Int = 0,

    // dunno
    @JsonProperty("monster_spawn_block_light_limit")
    public val monsterSpawnBlockLightLimit: Int = 0,

    /**
     * The maximum height that teleporting items and entities can put the player in this dimension.
     * Cannot be higher than maxHeight.
     */
    @JsonProperty("logical_height")
    public val maximumTeleportingHeight: Int = maxHeight - minHeight - 16,  // arbitrary

) : Identifiable {
    init {
        require(maxHeight in (minHeight + 1) until 4064 && maxHeight.rem(16) == 0) {
            "Maximum height '$maxHeight' must be: greater than minimum height ($minHeight), " +
            "smaller than 4064, and a multiple of sixteen"
        }
        require(minHeight >= -2032 && minHeight <= 2016 && minHeight.rem(16) == 0) {
            "Minimum height '$minHeight' must be: greater than -2032, smaller than 2016, and a " +
            "multiple of sixteen"
        }
    }

    public val totalHeight: Int = maxHeight - minHeight

    // automatically generated based on the other properties
    @get:JsonProperty("piglin_safe")
    public val piglinSafe: Boolean get() = true
    @get:JsonProperty("bed_works")
    public val bedWorks: Boolean get() = allowBedsAndCompasses
    @get:JsonProperty("respawn_anchor_works")
    public val respawnAnchorWorks: Boolean get() = allowBedsAndCompasses
    @get:JsonProperty("has_raids")
    public val hasRaids: Boolean get() = false
    @get:JsonProperty("infiniburn")
    public val infiniteBurnTag: String get() = "#"
    @get:JsonProperty("effects")
    public val effects: String get() = "minecraft:overworld"
    @get:JsonProperty("ambient_light")
    public val ambientLight: Float = 1.0F
}