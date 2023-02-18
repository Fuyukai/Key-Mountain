/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tf.veriny.keymountain.api.entity

/**
 * An opaque type that entities can use to store data. This is used when spawning an entity and
 * when saving an entity to disk (it must be serialisable by Jackson!)
 */
public interface EntityData