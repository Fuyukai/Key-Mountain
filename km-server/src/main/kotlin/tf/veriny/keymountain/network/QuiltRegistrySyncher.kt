/*
 * This file is part of Key-Mountain Server.
 *
 * Key-Mountain Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Key-Mountain Server is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Key-Mountain Server. If not, see <http://www.gnu.org/licenses/>.
 */

package tf.veriny.keymountain.network

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap
import okio.Buffer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import tf.veriny.keymountain.api.client.ClientReference
import tf.veriny.keymountain.api.data.RegistryWithIds
import tf.veriny.keymountain.api.network.packets.S2CPing
import tf.veriny.keymountain.api.network.packets.S2CPluginMessage
import tf.veriny.keymountain.api.network.plugin.PluginPacket
import tf.veriny.keymountain.api.network.plugin.PluginPacketSerialiser
import tf.veriny.keymountain.api.util.*
import tf.veriny.keymountain.data.Data

public class S2CRegistrySyncHandshake(public val supportedVerions: IntArray) : PluginPacket {
    public companion object : PluginPacketSerialiser<S2CRegistrySyncHandshake> {
        public val id: Identifier = Identifier("qsl:registry_sync/handshake")

        override fun readIn(data: Buffer): S2CRegistrySyncHandshake {
            val count = data.readVarInt()
            val versions = IntArray(count)

            for (ver in 0 until count) {
                versions[ver] = data.readVarInt()
            }

            return S2CRegistrySyncHandshake(versions)
        }

        override fun writeOut(packet: S2CRegistrySyncHandshake, data: Buffer) {
            data.writeVarInt(packet.supportedVerions.size)
            for (ver in packet.supportedVerions) {
                data.writeVarInt(ver)
            }
        }
    }

    override val identifier: Identifier get() = id
}

public class C2SRegistrySyncHandshake(public val supportedVersion: Int) : PluginPacket {
    public companion object : PluginPacketSerialiser<C2SRegistrySyncHandshake> {
        public val id: Identifier = Identifier("qsl:registry_sync/handshake")

        override fun readIn(data: Buffer): C2SRegistrySyncHandshake {
            return C2SRegistrySyncHandshake(data.readVarInt())
        }

        override fun writeOut(packet: C2SRegistrySyncHandshake, data: Buffer) {
            data.writeVarInt(packet.supportedVersion)
        }
    }

    override val identifier: Identifier get() = id
}

public class S2CRegistrySyncStart(
    public val registryId: Identifier,
    public val entryCount: Int,
) : PluginPacket {
    public companion object : PluginPacketSerialiser<S2CRegistrySyncStart> {
        public val id: Identifier = Identifier("qsl:registry_sync/registry_start")

        override fun readIn(data: Buffer): S2CRegistrySyncStart {
            val id = data.readMcString()
            val entryCount = data.readVarInt()
            data.readByte()  // flag vars, we don't care
            return S2CRegistrySyncStart(Identifier(id), entryCount)
        }

        override fun writeOut(packet: S2CRegistrySyncStart, data: Buffer) {
            data.writeMcString(packet.registryId.full)
            data.writeVarInt(packet.entryCount)
            // no flags
            data.writeByte(0)
        }
    }

    override val identifier: Identifier get() = id
}

// Phases:
// 1) S->C Handshake with versions array
// 2) C->S Handshake with chosen version
//
// For each registry...
//    1) S->C Registry Start with name and ID count
//    2) S->C Registry Data with an array of (namespace, entry count, entries[])
//    3) S->C Registry Apply
//
// The client is submissive in this state and doesn't send anything. If everything completes fine,
// then we won't receive anything from the client; instead, we send a Ping(2) frame
// after everything to signal to the server code that the client is ready to continue logging in.

/**
 * Synchronises registries between ourselves and the Minecraft client.
 */
public class QuiltRegistrySyncher(
    private val data: Data,
    private val networker: ServerNetworker
) {
    private companion object {
        val LOGGER: Logger = LogManager.getLogger(QuiltRegistrySyncher::class.java)
        val REGISTRY_DATA_ID = Identifier("qsl:registry_sync/registry_data")
        val REGISTRY_DATA_APPLY = Identifier("qsl:registry_sync/registry_apply")

        const val version = 2

        private fun getO2IMap(): Object2IntArrayMap<String> {
            val m = Object2IntArrayMap<String>()
            return m.also { it.defaultReturnValue(-1) }
        }
    }

    private fun handleHandshake(ref: ClientReference, packet: C2SRegistrySyncHandshake) {
        LOGGER.trace("client is telling us that they support version {}", packet.supportedVersion)
        if (packet.supportedVersion != version) {
            LOGGER.error("Client {} does not speak our language?", ref.loginInfo)
            return ref.die("You should kill yourself... now!")
        }

        for (registry in data.getSynchronisedRegistries()) {
            if (!ref.stillReceivingPackets) break

            syncRegistry(ref, registry)
        }

        ref.enqueueProtocolPacket(S2CPing(S2CPing.SYNC_COMPLETED_PING))
    }

    private fun <T : Identifiable> syncRegistry(ref: ClientReference, registry: RegistryWithIds<T>) {
        val startPacket = S2CRegistrySyncStart(registry.identifier, registry.size)
        ref.enqueuePluginPacket(startPacket)

        // for simplicity we just enqueue every namespace separately
        val items = mutableMapOf<String, Object2IntArrayMap<String>>()
        for (item in registry) {
            val numeric = registry.getNumericId(item)
            var nsMap = items[item.identifier.namespace]
            if (nsMap == null) {
                nsMap = getO2IMap()
                items[item.identifier.namespace] = nsMap
            }

            nsMap.put(item.identifier.thing, numeric)
        }

        val buffer = Buffer()
        for ((namespace, things) in items) {
            // Namespace count: 1
            buffer.writeVarInt(1)
            // Common namespace (str)
            buffer.writeMcString(namespace)
            // Entry count
            buffer.writeVarInt(things.size)

            // For each entry:
            for (key in things.keys) {
                // Entry name (str)
                buffer.writeMcString(key)
                // Entry ID (int)
                buffer.writeVarInt(things.getInt(key))
                // Flags (0)
                buffer.writeByte(0)
            }
        }

        val dataPacket = S2CPluginMessage(REGISTRY_DATA_ID, buffer)
        ref.enqueueProtocolPacket(dataPacket)

        val applyPacket = S2CPluginMessage(REGISTRY_DATA_APPLY, Buffer())
        ref.enqueueProtocolPacket(applyPacket)
    }

    public fun setupPacketHandlers(packets: PacketRegistryImpl) {
        data.packets.addOutgoingPacket(S2CRegistrySyncHandshake.id, S2CRegistrySyncHandshake)
        data.packets.addIncomingPacket(C2SRegistrySyncHandshake.id, C2SRegistrySyncHandshake, ::handleHandshake)

        data.packets.addOutgoingPacket(S2CRegistrySyncStart.id, S2CRegistrySyncStart)
    }

    public fun sendHandshake(ref: ClientReference) {
        LOGGER.debug("sending qsl registry synch packet")

        val packet = S2CRegistrySyncHandshake(intArrayOf(2))
        ref.enqueuePluginPacket(packet)
    }
}