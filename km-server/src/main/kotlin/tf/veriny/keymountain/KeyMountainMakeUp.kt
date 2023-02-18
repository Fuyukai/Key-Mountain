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

package tf.veriny.keymountain

import org.apache.logging.log4j.LogManager
import tf.veriny.keymountain.api.mod.ModKickstarter
import tf.veriny.keymountain.data.Data
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

/**
 * Loads and configures a Key Mountain server.
 */
@OptIn(ExperimentalTime::class)
public class KeyMountainMakeUp {
    private companion object {
        val LOGGER = LogManager.getLogger(KeyMountainMakeUp::class.java)
    }

    private val kickstarters = mutableListOf<ModKickstarter<*>>()

    /**
     * Adds a mod to be loaded when the server starts.
     */
    public fun addMod(kickstarter: ModKickstarter<*>) {
        kickstarters.add(kickstarter)
    }

    /**
     * Starts the Key Mountain server.
     */
    public fun start() {
        LOGGER.info("Loading the Key Mountain server...")

        val data = Data()
        for (mod in kickstarters) {
            val klass = mod.createModKlass(data)
            LOGGER.debug("Created mod: {} ", klass)
            klass.setup()
            data.addMod(klass::class, klass)
        }

        for (mod in data.getAllMods()) {
            mod.postSetup()
        }

        LOGGER.debug("Mod setup complete, generating extra data...")

        val bsTime = measureTime { data.generateBlockStates() }
        LOGGER.debug("Generated all known blockstate IDs in ${bsTime.inWholeMilliseconds}ms.")

        val server = KeyMountainServer(data)
        server.run()
    }
}