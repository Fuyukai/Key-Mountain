/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.entity

import tf.veriny.keymountain.api.util.Identifier
import tf.veriny.keymountain.api.world.World
import tf.veriny.keymountain.api.world.block.WorldPosition

public class PlayerEntity(
    uniqueId: Int,
    world: World
) : BaseEntity<PlayerEntity.PlayerEntityData, PlayerEntity>(Companion, uniqueId, world) {
    public companion object : EntityType<PlayerEntityData, PlayerEntity> {
        override val identifier: Identifier = Identifier("minecraft:player")

        // no, players are spawned manually
        override val shouldSendSpawnEntityPacket: Boolean get() = false

        override fun create(entityId: Int, into: World, pos: WorldPosition, data: PlayerEntityData?): PlayerEntity {
            val entity = PlayerEntity(entityId, into)
            entity.position.set(pos)
            return entity
        }
    }

    public class PlayerEntityData : EntityData
}