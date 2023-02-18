/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.network.packets

import com.dyescape.dataformat.nbt.databind.NBTMapper
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import okio.Buffer
import tf.veriny.keymountain.api.data.Registry
import tf.veriny.keymountain.api.data.RegistryWithIds
import tf.veriny.keymountain.api.data.VanillaSynchronisableRegistry
import tf.veriny.keymountain.api.network.ProtocolPacket
import tf.veriny.keymountain.api.network.ProtocolPacketSerialiser
import tf.veriny.keymountain.api.util.Identifiable
import tf.veriny.keymountain.api.util.Identifier
import tf.veriny.keymountain.api.util.writeMcString
import tf.veriny.keymountain.api.util.writeVarInt
import tf.veriny.keymountain.api.world.DimensionInfo
import tf.veriny.keymountain.api.world.GameMode
import tf.veriny.keymountain.api.world.biome.BiomeNetworkInfo

// login (play) on the wiki page, but this kinda sucks as a name
// also, we omit some fields and hardcode them for now.

/**
 * Sent to the client to start playing the game.
 */
public class S2CStartPlaying(
    public val entityId: Int,
    public val isHardcore: Boolean,
    public val gameMode: GameMode,
    // not sure how this differs from the registry...
    public val dimensionIds: List<Identifier>,
    public val synchronisedRegisteries: List<VanillaSynchronisableRegistry<Identifiable>>,
    // the client expects a few more but we don't send them!
    // the one being spawned into
    public val dimensionType: String,

    public val viewDistance: Int,
    public val clientRenderDistance: Int,
    public val enableRespawn: Boolean,
    public val isFlat: Boolean,
) : ProtocolPacket {
    private data class RegistryEntries(
        val type: String,
        val value: List<RegistryEntry>,
    ) {
        data class RegistryEntry(val name: String, val id: Int, val element: Any)
    }

    public companion object : ProtocolPacketSerialiser<S2CStartPlaying> {
        private val nbtMapper = NBTMapper().registerKotlinModule()

        public const val PACKET_ID: Int = 0x24

        override fun readIn(data: Buffer): S2CStartPlaying {
            TODO("too complicated")
        }

        override fun writeOut(packet: S2CStartPlaying, data: Buffer) {
            data.writeInt(packet.entityId)
            data.writeByte(if (packet.isHardcore) 1 else 0)
            data.writeByte(packet.gameMode.ordinal)
            // hardcoded: previous gamemode
            data.writeByte(-1)

            data.writeVarInt(packet.dimensionIds.size)
            for (id in packet.dimensionIds) {
                data.writeMcString(id.full)
            }

            val registryData: MutableMap<String, RegistryEntries> = object : HashMap<String, RegistryEntries>() {}

            for (registry in packet.synchronisedRegisteries) {
                val entries = mutableListOf<RegistryEntries.RegistryEntry>()
                for (thing in registry.getAllEntries()) {
                    val id = registry.getNumericId(thing)
                    entries.add(RegistryEntries.RegistryEntry(thing.identifier.full, id, thing))
                }
                registryData[registry.identifier.full] = RegistryEntries(registry.identifier.full, entries)
            }

            val rawNbt = nbtMapper.writeValueAsBytes(registryData)
            data.write(rawNbt)

            // dimension type and name, but we use the same value for both
            data.writeMcString(packet.dimensionType)
            data.writeMcString(packet.dimensionType)

            // hardcoded: "hashed seed"
            data.writeLong(0L)
            // hardcoded: max players (ignored)
            data.writeVarInt(0x39)
            data.writeVarInt(packet.viewDistance)
            data.writeVarInt(packet.clientRenderDistance)
            // hardcoded: reduced debug info
            data.writeByte(0)
            data.writeByte(if (packet.enableRespawn) 1 else 0)
            // hardcoded: debug mode world
            data.writeByte(0)
            data.writeByte(if (packet.isFlat) 1 else 0)
            // hardcoded: has death location
            data.writeByte(0)
        }
    }

    override val id: Int = PACKET_ID
}