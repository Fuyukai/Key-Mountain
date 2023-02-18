package tf.veriny.keymountain.api.entity

import tf.veriny.keymountain.api.util.Identifier
import tf.veriny.keymountain.api.util.Vector3
import tf.veriny.keymountain.api.world.block.WorldPosition

public class PlayerEntity(
    override val uniqueId: Int,
) : Entity<PlayerEntity.PlayerEntityData, PlayerEntity> {
    public companion object : EntityType<PlayerEntityData, PlayerEntity> {
        override val identifier: Identifier = Identifier("minecraft:player")

        // no, players are spawned manually
        override val shouldSendSpawnEntityPacket: Boolean get() = false

        override fun create(entityId: Int, pos: WorldPosition, data: PlayerEntityData?): PlayerEntity {
            val entity = PlayerEntity(entityId)
            entity.position.set(pos)
            return entity
        }
    }

    public class PlayerEntityData : EntityData

    override val type: Companion get() = Companion

    override val position: Vector3 = Vector3(0.0, 0.0, 0.0)
}